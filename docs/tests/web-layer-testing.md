# Web-layer Testing Guidelines

Describes the standardised approach for testing the web layer (Controller + Thymeleaf templates) in modules following the Clean Architecture DDD+CQRS pattern.

---

## Goals

| Goal | Covered by |
|------|-----------|
| Fast, isolated controller tests (no Spring context) | Controller Unit Tests |
| Validate DTO ↔ template property binding | Static Template Checks |
| Validate `sec:authorize` show/hide per authority | Integration Template Tests |
| Shared infrastructure, no duplication | `testutils` package |

---

## Dependency Versions (Stable Baseline)

The following versions are the tested-stable baseline for the current web-layer test suite. **Do not bump these without running the full test suite first** — the compatibility issues described below are version-sensitive.

| Dependency | Version | Scope | Notes |
|---|---|---|---|
| `spring-boot-starter-parent` (BOM) | **4.0.3** | — | Manages Spring and BOM versions |
| `spring-framework` (spring-test, spring-webmvc) | **7.0.5** | test | Test infra changes in Spring 7 |
| `spring-security` family | **7.0.3** | compile/test | Spring Security 7 introduces new expression handler types |
| `thymeleaf-extras-springsecurity6` | **3.1.3.RELEASE** | compile | Pinned due to generic-type lookup in dialect (see note) |
| `ognl` | **3.2.21** | **test** | Required by Thymeleaf StandardDialect in some test contexts |
| `spring-security-test` | Managed by BOM (7.x) | test | Provides TestingAuthenticationToken, WithMockUser, helpers |

Notes:
- `thymeleaf-extras-springsecurity6` should be pinned to 3.1.3.RELEASE in `pom.xml` when using Spring Boot 4.x / Spring Security 7.x. The dialect expects a bean of type `SecurityExpressionHandler<FilterInvocation>`; Spring Security 7's `DefaultHttpSecurityExpressionHandler` implements a different generic parameter, causing the dialect to fail to locate a handler. Pinning + adding a minimal `TestFilterInvocationExpressionHandler` (see `TemplateTestUtils`) avoids the issue.
- `ognl:3.2.21` is added as test-scoped dependency because the Thymeleaf `StandardDialect` initialises OGNL-based expression support and tests often initialise a `TemplateEngine` without pulling OGNL transitively into the test classpath; this causes `NoClassDefFoundError` unless present.
- Java 21 (Temurin 21) is the supported JDK for running the full suite (pom property `<java.version>` is set to 21). Ensure CI uses JDK 21.

Should we lock more dependency versions?

Lock only if a specific version is known to be problematic (thymeleaf-extras is the main candidate). Avoid over-pinning; let the BOM manage routine updates.

---

## Spring Boot 4.0 Breaking Changes (Critical)

These were discovered during implementation and directly shaped the testing approach.

### `@WebMvcTest` removed
`spring-boot-test-autoconfigure:4.0.3` no longer contains the `web/servlet` package.
`@WebMvcTest` and related test-slice annotations do not exist. **Do not use them.**

### `@MockBean` removed
Replaced by `@MockitoBean` from `org.springframework.test.context.bean.override.mockito.MockitoBean` (Spring Framework 7). For tests not starting a Spring context (our Controller Unit Tests), use plain `Mockito.mock()` instead.

### `DefaultWebSecurityExpressionHandler` removed
`spring-security-web:7.0.3` removed `DefaultWebSecurityExpressionHandler` (which implemented `SecurityExpressionHandler<FilterInvocation>`). Its replacement, `DefaultHttpSecurityExpressionHandler`, implements `SecurityExpressionHandler<RequestAuthorizationContext>`.

`thymeleaf-extras-springsecurity6` v3.1.x uses `GenericTypeResolver.resolveTypeArgument()` to find a bean typed **exactly** `SecurityExpressionHandler<FilterInvocation>`. Providing `DefaultHttpSecurityExpressionHandler` (wrong generic) causes:
```
No visible SecurityExpressionHandler instance could be found
```

**Solution:** `TestFilterInvocationExpressionHandler` in `TemplateTestUtils` — a concrete subclass of `AbstractSecurityExpressionHandler<FilterInvocation>` that creates `WebSecurityExpressionRoot` (still public in spring-security-web 7). No `@EnableWebSecurity` context needed.

### `@EnableWebSecurity` in test class causes `UnreachableFilterChainException`
If a `@Configuration @EnableWebSecurity` class is placed in the test classpath with a `SecurityFilterChain` that matches any request, Spring Boot's `@SpringBootTest` component-scanner will pick it up and register it alongside the application's own `SecurityConfig`, triggering:
```
UnreachableFilterChainException: A filter chain that matches any request has already been configured
```
`TestFilterInvocationExpressionHandler` carries **no stereotype annotations** (`@Configuration`, `@Component`, etc.) and is therefore invisible to component scan.

---

## Test Patterns

### 1 — Controller Unit Test

```java
@ExtendWith(MockitoExtension.class)
class BrandControllerTest {
    @Mock BrandQueryUseCase queryUseCase;
    @Mock BrandCommandUseCase commandUseCase;
    // ...

    private BrandController controller;

    @BeforeEach
    void setUp() {
        controller = new BrandController(queryUseCase, commandUseCase, ...);
    }

    @Test
    void listReturnsCorrectViewAndModel() {
        // given
        Page<BrandSummaryResponse> page = new PageImpl<>(List.of(...));
        given(queryUseCase.findAll(any())).willReturn(page);

        // when
        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.list(model, PageRequest.of(0, 20), "");

        // then
        assertThat(view).isEqualTo("inventory/brands/list");
        assertThat(model.get("page")).isEqualTo(page);
    }
}
```

**What it covers:** view name, model population, usecase delegation. No Spring context, no DB, runs in < 200 ms.

### 2 — Static Template Check

```java
class BrandTemplateTest {
    @Test
    void listTemplateContainsExpectedFragment() throws Exception {
        String html = Files.readString(Path.of("src/main/resources/templates/inventory/brands/list.html"));
        assertThat(html).contains("brand-table-container");
        assertThat(html).contains("${item.name}");
        assertThat(html).contains("${item.code}");
    }
}
```

**What it covers:** fragment IDs referenced by HTMX/AJAX calls exist; DTO property names match template placeholders. Catches typos at near-zero cost (pure string scan, no Thymeleaf engine).

### 3 — Integration Template Test (`sec:authorize` visibility)

```java
class BrandListIntegrationTest {

    @Test
    void withBrandCreate_addButtonIsVisible() {
        String html = TemplateTestUtils.renderWithSecurity(
            "inventory/brands/list",
            Map.of("page", emptyPage(), "keyword", ""),
            auth("BRAND_READ", "BRAND_CREATE"));

        assertThat(html).contains("/inventory/brands/create");
    }

    @Test
    void withoutBrandCreate_addButtonIsHidden() {
        String html = TemplateTestUtils.renderWithSecurity(
            "inventory/brands/list",
            Map.of("page", emptyPage(), "keyword", ""),
            auth("BRAND_READ"));

        assertThat(html).doesNotContain("/inventory/brands/create");
    }
}
```

**What it covers:** `sec:authorize` show/hide per granted authority. Thymeleaf renders the full template at runtime with `SpringSecurityDialect` active. Layout (`th:replace`) is stripped during pre-processing so no servlet container or DB is needed.

---

## Test Utilities (`com.solusi.erp.testutils`)

### `TemplateTestUtils`

Central helper. Two rendering modes:

| Method | How it works | Use for |
|--------|-------------|---------|
| `renderFragment(template, fragmentId, context)` | Reads raw HTML, strips `th:replace` + `sec:authorize`, replaces `@{...}` links, returns string containing fragment | Static placeholder checks |
| `renderWithSecurity(template, variables, auth)` | Full Thymeleaf render with `SpringSecurityDialect` + `SecurityContextHolder`; auth visibility is evaluated | `sec:authorize` show/hide tests |

**Template pre-processing applied in both modes:**
- `@{/path}` → `'/path'` (literal string, avoids `IWebExchange` requirement)
- `@{/path/{id}(id=${item.id})}` → `'/path/'` (path-variable + query-param forms handled)
- `th:replace="..."` → removed (layout fragments not resolved)
- `sec:authorize="..."` → removed in `renderFragment` only; **kept** in `renderWithSecurity`

**Why `StringTemplateResolver` for `renderWithSecurity`:** After stripping `th:replace`, no external fragments are referenced. A `StringTemplateResolver` processes the pre-processed HTML directly without any classpath lookups, keeping the test self-contained.

### `TestDtoFactory`
Builds representative sample DTOs for Brand, Product, and ProductCategory.

### `TestPageBuilder`
Creates `PageImpl<T>` instances for templates expecting a `page` model attribute.

---

## File Layout

```
src/test/java/com/solusi/erp/
├── testutils/
│   ├── TemplateTestUtils.java         # shared rendering utilities
│   ├── TestDtoFactory.java            # sample DTO builders
│   └── TestPageBuilder.java           # Page<T> builder
└── inventory/
    ├── brand/
    │   └── web/
    │       ├── controller/
    │       │   └── BrandControllerTest.java       # Pattern 1
    │       └── template/
    │           ├── BrandTemplateTest.java         # Pattern 2
    │           └── integration/
    │               └── BrandListIntegrationTest.java  # Pattern 3
    ├── product/
    │   └── web/
    │       ├── controller/
    │       │   └── ProductControllerTest.java
    │       └── template/
    │           ├── ProductTemplateTest.java
    │           └── integration/
    │               └── ProductListIntegrationTest.java
    └── productcategory/
        └── web/
            ├── controller/
            │   └── ProductCategoryControllerTest.java
            └── template/
                ├── ProductCategoryTemplateTest.java
                └── integration/
                    └── ProductCategoryListIntegrationTest.java
```

> `BrandListIntegrationTest` is the **golden reference** for Pattern 3. When adding
> integration template tests for ProductCategory and Product, follow its structure.
