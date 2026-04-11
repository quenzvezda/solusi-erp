# Accounting Foundation — Test Suite Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement comprehensive unit + integration tests for the Sprint 1 Accounting Foundation (COA, Accounting Schema, Fiscal Year/Period), covering domain model tests, controller unit tests, static template checks, and integration template tests with sec:authorize visibility.

**Architecture:** Three test patterns are followed: (1) Controller Unit Tests with plain Mockito — no Spring context, (2) Static Template Checks — pure string scan, (3) Integration Template Tests — Thymeleaf + SpringSecurityDialect via `TemplateTestUtils.renderWithSecurity()`. Domain model and use case tests add pure Java coverage for business logic.

**Tech Stack:** JUnit 5, Mockito (plain `mock()` — NO `@MockBean`), AssertJ, Thymeleaf, SpringSecurityDialect, `TemplateTestUtils`, `TestDtoFactory`, `TestPageBuilder`

---

## Important Constraints

### Spring Boot 4.x Restrictions (MUST follow)
- **FORBIDDEN:** `@WebMvcTest` (removed in Spring Boot 4.0)
- **FORBIDDEN:** `@MockBean` (removed — use plain `Mockito.mock()` or `@MockitoBean`)
- **FORBIDDEN:** `@EnableWebSecurity` in test class (causes `UnreachableFilterChainException`)
- **USE:** `TestFilterInvocationExpressionHandler` via `TemplateTestUtils` for `sec:authorize` tests

### JaCoCo Notes
JaCoCo already configured in `pom.xml` with thresholds at 60% line / 50% branch. Current excludes: controllers, templates, DTOs, mappers, JPA entities, infrastructure adapters. Only domain models + use case impls contribute to coverage. Current thresholds: 60% LINE, 50% BRANCH. Target: raise to 80% LINE, 75% BRANCH after all tests pass.

### Reference Tests (golden pattern)
- `BrandControllerTest.java` — Pattern 1 reference
- `BrandTemplateTest.java` — Pattern 2 reference
- `BrandListIntegrationTest.java` — Pattern 3 (list) reference
- `BrandFormIntegrationTest.java` — Pattern 3 (form) reference

### Permission Names
- COA: `ACCOUNTING-COA_READ`, `ACCOUNTING-COA_CREATE`, `ACCOUNTING-COA_UPDATE`, `ACCOUNTING-COA_DELETE`
- Schema: `ACCOUNTING-SCHEMA_READ`, `ACCOUNTING-SCHEMA_CREATE`, `ACCOUNTING-SCHEMA_UPDATE`, `ACCOUNTING-SCHEMA_DELETE`
- Period: `ACCOUNTING-PERIOD_READ`, `ACCOUNTING-PERIOD_CREATE`, `ACCOUNTING-PERIOD_UPDATE`, `ACCOUNTING-PERIOD_DELETE`

---

## File Structure

```
src/test/java/com/solusi/erp/
├── testutils/
│   ├── TestDtoFactory.java                             # MODIFY — add accounting factory methods
│   └── (TemplateTestUtils.java, TestPageBuilder.java)  # unchanged
├── accounting/
│   ├── coa/
│   │   ├── domain/model/
│   │   │   └── ChartOfAccountTest.java                 # CREATE — domain model tests
│   │   ├── application/usecase/command/
│   │   │   ├── CreateCoaUseCaseTest.java               # CREATE — use case tests
│   │   │   ├── UpdateCoaUseCaseTest.java               # CREATE
│   │   │   └── DeleteCoaUseCaseTest.java               # CREATE
│   │   ├── application/usecase/query/
│   │   │   └── FindCoaUseCaseTest.java                 # CREATE
│   │   └── web/
│   │       ├── controller/
│   │       │   └── CoaControllerTest.java              # CREATE — Pattern 1
│   │       └── template/
│   │           ├── CoaTemplateTest.java                 # CREATE — Pattern 2
│   │           └── integration/
│   │               ├── CoaListIntegrationTest.java      # CREATE — Pattern 3
│   │               └── CoaFormIntegrationTest.java      # CREATE — Pattern 3
│   ├── schema/
│   │   ├── domain/model/
│   │   │   └── AccountingSchemaTest.java               # CREATE — domain model tests
│   │   ├── application/usecase/command/
│   │   │   ├── CreateSchemaUseCaseTest.java            # CREATE
│   │   │   ├── UpdateSchemaUseCaseTest.java            # CREATE
│   │   │   └── DeleteSchemaUseCaseTest.java            # CREATE
│   │   ├── application/usecase/query/
│   │   │   └── FindSchemasUseCaseTest.java             # CREATE
│   │   └── web/
│   │       ├── controller/
│   │       │   └── SchemaControllerTest.java           # CREATE — Pattern 1
│   │       └── template/
│   │           ├── SchemaTemplateTest.java              # CREATE — Pattern 2
│   │           └── integration/
│   │               ├── SchemaListIntegrationTest.java   # CREATE — Pattern 3
│   │               └── SchemaFormIntegrationTest.java   # CREATE — Pattern 3
│   └── period/
│       ├── domain/model/
│       │   ├── FiscalYearTest.java                     # CREATE — aggregate root tests
│       │   └── AccountingPeriodTest.java               # CREATE — entity tests
│       ├── application/usecase/command/
│       │   ├── CreateFiscalYearUseCaseTest.java        # CREATE
│       │   ├── ClosePeriodUseCaseTest.java             # CREATE
│       │   └── ReopenPeriodUseCaseTest.java            # CREATE
│       ├── application/usecase/query/
│       │   └── FindFiscalYearsUseCaseTest.java         # CREATE
│       └── web/
│           ├── controller/
│           │   └── PeriodControllerTest.java           # CREATE — Pattern 1
│           └── template/
│               ├── PeriodTemplateTest.java              # CREATE — Pattern 2
│               └── integration/
│                   ├── PeriodListIntegrationTest.java   # CREATE — Pattern 3
│                   ├── PeriodDetailIntegrationTest.java # CREATE — Pattern 3
│                   └── PeriodFormIntegrationTest.java   # CREATE — Pattern 3
```

Total: **28 new test files** + 1 modification to TestDtoFactory.

---

## Task 1: TestDtoFactory Extensions

**Files:**
- Modify: `src/test/java/com/solusi/erp/testutils/TestDtoFactory.java`

**Goal:** Add factory methods for all accounting DTOs so test classes can build sample data without duplicating setup logic.

- [ ] **Step 1: Add COA factory methods**

Add these methods to `TestDtoFactory`:

```java
// === Chart of Accounts ===

public static CoaSummaryResponse sampleCoaSummaryResponse() {
    CoaSummaryResponse dto = new CoaSummaryResponse();
    dto.setId(1L);
    dto.setCode("1000");
    dto.setName("Cash");
    dto.setAccountType("ASSET");
    dto.setNormalBalance("DEBIT");
    dto.setLevel(1);
    dto.setIsHeader(false);
    dto.setIsActive(true);
    return dto;
}

public static CoaSummaryResponse sampleCoaSummaryResponse(Long id, String code, String name, String accountType) {
    CoaSummaryResponse dto = new CoaSummaryResponse();
    dto.setId(id);
    dto.setCode(code);
    dto.setName(name);
    dto.setAccountType(accountType);
    dto.setNormalBalance(accountType.equals("ASSET") || accountType.equals("EXPENSE") ? "DEBIT" : "CREDIT");
    dto.setLevel(1);
    dto.setIsHeader(false);
    dto.setIsActive(true);
    return dto;
}

public static CoaSaveRequest sampleCoaSaveRequest() {
    CoaSaveRequest req = new CoaSaveRequest();
    req.setCode("1000");
    req.setName("Cash");
    req.setAccountType("ASSET");
    req.setLevel(1);
    req.setIsHeader(false);
    req.setNote("Cash account");
    req.setIsActive(true);
    return req;
}

public static CoaDetailResponse sampleCoaDetailResponse() {
    CoaDetailResponse dto = new CoaDetailResponse();
    dto.setId(1L);
    dto.setCode("1000");
    dto.setName("Cash");
    dto.setAccountType("ASSET");
    dto.setNormalBalance("DEBIT");
    dto.setLevel(1);
    dto.setIsHeader(false);
    dto.setNote("Cash account");
    dto.setIsActive(true);
    return dto;
}
```

- [ ] **Step 2: Add Schema factory methods**

```java
// === Accounting Schema ===

public static SchemaSummaryResponse sampleSchemaSummaryResponse() {
    SchemaSummaryResponse dto = new SchemaSummaryResponse();
    dto.setId(1L);
    dto.setEventType("GOODS_RECEIPT");
    dto.setDescription("Goods receipt schema");
    dto.setDebitAccountId(1L);
    dto.setDebitAccountName("1000 - Cash");
    dto.setCreditAccountId(2L);
    dto.setCreditAccountName("2000 - Accounts Payable");
    dto.setIsActive(true);
    return dto;
}

public static SchemaSaveRequest sampleSchemaSaveRequest() {
    SchemaSaveRequest req = new SchemaSaveRequest();
    req.setEventType("GOODS_RECEIPT");
    req.setDescription("Goods receipt schema");
    req.setDebitAccountId(1L);
    req.setCreditAccountId(2L);
    req.setIsActive(true);
    return req;
}

public static SchemaDetailResponse sampleSchemaDetailResponse() {
    SchemaDetailResponse dto = new SchemaDetailResponse();
    dto.setId(1L);
    dto.setEventType("GOODS_RECEIPT");
    dto.setDescription("Goods receipt schema");
    dto.setDebitAccountId(1L);
    dto.setDebitAccountName("1000 - Cash");
    dto.setCreditAccountId(2L);
    dto.setCreditAccountName("2000 - Accounts Payable");
    dto.setIsActive(true);
    return dto;
}
```

- [ ] **Step 3: Add Period/FiscalYear factory methods**

```java
// === Fiscal Year & Period ===

public static FiscalYearSummaryResponse sampleFiscalYearSummaryResponse() {
    FiscalYearSummaryResponse dto = new FiscalYearSummaryResponse();
    dto.setId(1L);
    dto.setCode("FY-0001");
    dto.setName("Fiscal Year 2026");
    dto.setStartDate(java.time.LocalDate.of(2026, 1, 1));
    dto.setEndDate(java.time.LocalDate.of(2026, 12, 31));
    dto.setIsActive(true);
    dto.setPeriodCount(12);
    return dto;
}

public static FiscalYearSaveRequest sampleFiscalYearSaveRequest() {
    FiscalYearSaveRequest req = new FiscalYearSaveRequest();
    req.setName("Fiscal Year 2026");
    req.setStartDate(java.time.LocalDate.of(2026, 1, 1));
    req.setEndDate(java.time.LocalDate.of(2026, 12, 31));
    req.setIsActive(true);
    return req;
}

public static FiscalYearDetailResponse sampleFiscalYearDetailResponse() {
    FiscalYearDetailResponse dto = new FiscalYearDetailResponse();
    dto.setId(1L);
    dto.setCode("FY-0001");
    dto.setName("Fiscal Year 2026");
    dto.setStartDate(java.time.LocalDate.of(2026, 1, 1));
    dto.setEndDate(java.time.LocalDate.of(2026, 12, 31));
    dto.setIsActive(true);
    dto.setPeriods(java.util.List.of(samplePeriodResponse(1L, "FY-0001-01", "Jan 2026", 1, "NEVER_OPENED")));
    return dto;
}

public static PeriodResponse samplePeriodResponse(Long id, String code, String name, int periodNumber, String status) {
    PeriodResponse dto = new PeriodResponse();
    dto.setId(id);
    dto.setCode(code);
    dto.setName(name);
    dto.setPeriodNumber(periodNumber);
    dto.setStartDate(java.time.LocalDate.of(2026, periodNumber, 1));
    dto.setEndDate(java.time.LocalDate.of(2026, periodNumber, 1).plusMonths(1).minusDays(1));
    dto.setStatus(status);
    return dto;
}
```

- [ ] **Step 4: Add imports for new DTO types**

Add these imports at the top of `TestDtoFactory.java`:

```java
import com.solusi.erp.accounting.coa.web.dto.CoaSummaryResponse;
import com.solusi.erp.accounting.coa.web.dto.CoaSaveRequest;
import com.solusi.erp.accounting.coa.web.dto.CoaDetailResponse;
import com.solusi.erp.accounting.schema.web.dto.SchemaSummaryResponse;
import com.solusi.erp.accounting.schema.web.dto.SchemaSaveRequest;
import com.solusi.erp.accounting.schema.web.dto.SchemaDetailResponse;
import com.solusi.erp.accounting.period.web.dto.FiscalYearSummaryResponse;
import com.solusi.erp.accounting.period.web.dto.FiscalYearSaveRequest;
import com.solusi.erp.accounting.period.web.dto.FiscalYearDetailResponse;
import com.solusi.erp.accounting.period.web.dto.PeriodResponse;
```

- [ ] **Step 5: Verify compilation**

Run: `mvn compile -pl . -q -DskipTests 2>&1 | head -20`
Expected: BUILD SUCCESS

- [ ] **Step 6: Commit**

```bash
git add src/test/java/com/solusi/erp/testutils/TestDtoFactory.java
git commit -m "test: add accounting DTO factory methods to TestDtoFactory"
```

---

## Task 2: COA Domain + Use Case Tests

**Files:**
- Create: `src/test/java/com/solusi/erp/accounting/coa/domain/model/ChartOfAccountTest.java`
- Create: `src/test/java/com/solusi/erp/accounting/coa/application/usecase/command/CreateCoaUseCaseTest.java`
- Create: `src/test/java/com/solusi/erp/accounting/coa/application/usecase/command/UpdateCoaUseCaseTest.java`
- Create: `src/test/java/com/solusi/erp/accounting/coa/application/usecase/command/DeleteCoaUseCaseTest.java`
- Create: `src/test/java/com/solusi/erp/accounting/coa/application/usecase/query/FindCoaUseCaseTest.java`

**Goal:** Full domain model test coverage for `ChartOfAccount` + use case tests for all COA operations. Pure Java tests, no Spring context.

### Domain Model: ChartOfAccountTest.java

```java
package com.solusi.erp.accounting.coa.domain.model;

import com.solusi.erp.core.domain.model.AuditMetadata;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ChartOfAccount Domain Model Tests")
class ChartOfAccountTest {

    @Test
    @DisplayName("createNew sets all fields with empty metadata (id == null)")
    void createNew_setsAllFieldsWithEmptyMetadata() {
        ChartOfAccount coa = ChartOfAccount.createNew(
                "1000", "Cash", AccountType.ASSET, null, 1, false, "Note", true);

        assertThat(coa.getId()).isNull();
        assertThat(coa.getCode()).isEqualTo("1000");
        assertThat(coa.getName()).isEqualTo("Cash");
        assertThat(coa.getAccountType()).isEqualTo(AccountType.ASSET);
        assertThat(coa.getNormalBalance()).isEqualTo(NormalBalance.DEBIT);
        assertThat(coa.getParentId()).isNull();
        assertThat(coa.getLevel()).isEqualTo(1);
        assertThat(coa.getIsHeader()).isFalse();
        assertThat(coa.getNote()).isEqualTo("Note");
        assertThat(coa.getIsActive()).isTrue();
    }

    @Test
    @DisplayName("createNew derives CREDIT normal balance for LIABILITY")
    void createNew_derivesNormalBalanceForLiability() {
        ChartOfAccount coa = ChartOfAccount.createNew(
                "2000", "Payable", AccountType.LIABILITY, null, 1, false, null, true);

        assertThat(coa.getNormalBalance()).isEqualTo(NormalBalance.CREDIT);
    }

    @Test
    @DisplayName("createNew defaults level=1 when null")
    void createNew_defaultsLevelWhenNull() {
        ChartOfAccount coa = ChartOfAccount.createNew(
                "1000", "Cash", AccountType.ASSET, null, null, null, null, null);

        assertThat(coa.getLevel()).isEqualTo(1);
        assertThat(coa.getIsHeader()).isFalse();
        assertThat(coa.getIsActive()).isTrue();
    }

    @Test
    @DisplayName("createNew with parent sets parentId")
    void createNew_withParent_setsParentId() {
        ChartOfAccount coa = ChartOfAccount.createNew(
                "1010", "Cash in Bank", AccountType.ASSET, 1L, 2, false, null, true);

        assertThat(coa.getParentId()).isEqualTo(1L);
        assertThat(coa.getLevel()).isEqualTo(2);
    }

    @Test
    @DisplayName("full constructor preserves all fields including metadata")
    void constructor_preservesAllFields() {
        AuditMetadata metadata = new AuditMetadata(1L, 1L, null, null, null, null);
        ChartOfAccount coa = new ChartOfAccount(metadata, "1000", "Cash",
                AccountType.ASSET, NormalBalance.DEBIT, null, 1, false, "Note", true);

        assertThat(coa.getId()).isEqualTo(1L);
        assertThat(coa.getMetadata()).isEqualTo(metadata);
        assertThat(coa.getCode()).isEqualTo("1000");
    }

    @Test
    @DisplayName("update changes name, accountType, and recalculates normalBalance")
    void update_changesFieldsAndRecalculatesNormalBalance() {
        ChartOfAccount coa = ChartOfAccount.createNew(
                "1000", "Cash", AccountType.ASSET, null, 1, false, null, true);

        coa.update("Revenue Account", AccountType.REVENUE, null, 1, false, "Updated", true);

        assertThat(coa.getName()).isEqualTo("Revenue Account");
        assertThat(coa.getAccountType()).isEqualTo(AccountType.REVENUE);
        assertThat(coa.getNormalBalance()).isEqualTo(NormalBalance.CREDIT);
        assertThat(coa.getNote()).isEqualTo("Updated");
        assertThat(coa.getCode()).isEqualTo("1000"); // code unchanged
    }

    @Test
    @DisplayName("softDelete sets isActive to false")
    void softDelete_setsIsActiveFalse() {
        ChartOfAccount coa = ChartOfAccount.createNew(
                "1000", "Cash", AccountType.ASSET, null, 1, false, null, true);

        coa.softDelete();

        assertThat(coa.getIsActive()).isFalse();
    }
}
```

### Use Case: CreateCoaUseCaseTest.java

```java
package com.solusi.erp.accounting.coa.application.usecase.command;

import com.solusi.erp.accounting.coa.domain.model.AccountType;
import com.solusi.erp.accounting.coa.domain.model.ChartOfAccount;
import com.solusi.erp.accounting.coa.domain.repository.CoaRepository;
import com.solusi.erp.core.exception.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateCoaUseCase Tests")
class CreateCoaUseCaseTest {

    @Mock private CoaRepository repository;
    private CreateCoaUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateCoaUseCaseImpl(repository);
    }

    @Test
    @DisplayName("execute saves and returns ChartOfAccount")
    void execute_savesAndReturnsCoa() {
        when(repository.existsByCode("1000")).thenReturn(false);
        when(repository.save(any(ChartOfAccount.class))).thenAnswer(i -> i.getArgument(0));

        ChartOfAccount result = useCase.execute("1000", "Cash", AccountType.ASSET,
                null, 1, false, "Note", true);

        assertThat(result.getCode()).isEqualTo("1000");
        assertThat(result.getName()).isEqualTo("Cash");
        verify(repository).save(any(ChartOfAccount.class));
    }

    @Test
    @DisplayName("execute throws DomainException when code already exists")
    void execute_throwsWhenDuplicateCode() {
        when(repository.existsByCode("1000")).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute("1000", "Cash", AccountType.ASSET,
                null, 1, false, null, true))
                .isInstanceOf(DomainException.class);

        verify(repository, never()).save(any());
    }
}
```

### Use Case: UpdateCoaUseCaseTest.java

Read `UpdateCoaUseCaseImpl.java` first to understand its exact signature and dependencies. The implementer must read the file before writing the test.

**Source file:** `src/main/java/com/solusi/erp/accounting/coa/application/usecase/command/UpdateCoaUseCaseImpl.java`

Test cases:
- `execute_updatesAndReturnsCoa` — finds by ID, calls domain `update()`, saves, returns
- `execute_throwsWhenNotFound` — repository returns empty Optional → DomainException

### Use Case: DeleteCoaUseCaseTest.java

**Source file:** `src/main/java/com/solusi/erp/accounting/coa/application/usecase/command/DeleteCoaUseCaseImpl.java`

Dependencies: `CoaRepository`, `CoaInUseChecker`

Test cases:
- `execute_hardDeletesWhenNotInUse` — `inUseChecker.isInUse()` returns false → `repository.delete()` called → returns `HARD_DELETED`
- `execute_softDeletesWhenInUse` — `inUseChecker.isInUse()` returns true → `coa.softDelete()` + `save()` → returns `SOFT_DELETED`
- `execute_throwsWhenNotFound` — repository returns empty Optional → DomainException

### Use Case: FindCoaUseCaseTest.java

**Source file:** `src/main/java/com/solusi/erp/accounting/coa/application/usecase/query/FindCoaUseCaseImpl.java`

Test case:
- `execute_delegatesToRepository` — verifies keyword and pageable are forwarded, returns the domain Page

- [ ] **Step 1: Write all test files as shown above**
- [ ] **Step 2: Run tests**

Run: `mvn test -pl . -Dtest="com.solusi.erp.accounting.coa.**" -q 2>&1 | tail -20`
Expected: All tests PASS

- [ ] **Step 3: Commit**

```bash
git add src/test/java/com/solusi/erp/accounting/coa/domain/ src/test/java/com/solusi/erp/accounting/coa/application/
git commit -m "test: add COA domain model and use case unit tests"
```

---

## Task 3: Schema Domain + Use Case Tests

**Files:**
- Create: `src/test/java/com/solusi/erp/accounting/schema/domain/model/AccountingSchemaTest.java`
- Create: `src/test/java/com/solusi/erp/accounting/schema/application/usecase/command/CreateSchemaUseCaseTest.java`
- Create: `src/test/java/com/solusi/erp/accounting/schema/application/usecase/command/UpdateSchemaUseCaseTest.java`
- Create: `src/test/java/com/solusi/erp/accounting/schema/application/usecase/command/DeleteSchemaUseCaseTest.java`
- Create: `src/test/java/com/solusi/erp/accounting/schema/application/usecase/query/FindSchemasUseCaseTest.java`

**Goal:** Full domain model test coverage for `AccountingSchema` + use case tests.

### Domain Model: AccountingSchemaTest.java

```java
package com.solusi.erp.accounting.schema.domain.model;

import com.solusi.erp.core.domain.model.AuditMetadata;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AccountingSchema Domain Model Tests")
class AccountingSchemaTest {

    @Test
    @DisplayName("createNew sets all fields with empty metadata")
    void createNew_setsAllFields() {
        AccountingSchema schema = AccountingSchema.createNew(
                SchemaEventType.GOODS_RECEIPT, "GR schema", 1L, 2L, true);

        assertThat(schema.getId()).isNull();
        assertThat(schema.getEventType()).isEqualTo(SchemaEventType.GOODS_RECEIPT);
        assertThat(schema.getDescription()).isEqualTo("GR schema");
        assertThat(schema.getDebitAccountId()).isEqualTo(1L);
        assertThat(schema.getCreditAccountId()).isEqualTo(2L);
        assertThat(schema.getIsActive()).isTrue();
    }

    @Test
    @DisplayName("createNew defaults isActive to true when null")
    void createNew_defaultsIsActiveWhenNull() {
        AccountingSchema schema = AccountingSchema.createNew(
                SchemaEventType.VENDOR_BILL, "desc", 1L, 2L, null);

        assertThat(schema.getIsActive()).isTrue();
    }

    @Test
    @DisplayName("full constructor preserves metadata")
    void constructor_preservesMetadata() {
        AuditMetadata meta = new AuditMetadata(5L, 2L, null, null, null, null);
        AccountingSchema schema = new AccountingSchema(meta, SchemaEventType.VENDOR_PAYMENT,
                "desc", 10L, 20L, true);

        assertThat(schema.getId()).isEqualTo(5L);
        assertThat(schema.getMetadata()).isEqualTo(meta);
    }

    @Test
    @DisplayName("update changes description, accounts, and isActive")
    void update_changesFields() {
        AccountingSchema schema = AccountingSchema.createNew(
                SchemaEventType.GOODS_RECEIPT, "old", 1L, 2L, true);

        schema.update("new desc", 10L, 20L, false);

        assertThat(schema.getDescription()).isEqualTo("new desc");
        assertThat(schema.getDebitAccountId()).isEqualTo(10L);
        assertThat(schema.getCreditAccountId()).isEqualTo(20L);
        assertThat(schema.getIsActive()).isFalse();
        assertThat(schema.getEventType()).isEqualTo(SchemaEventType.GOODS_RECEIPT); // unchanged
    }

    @Test
    @DisplayName("softDelete sets isActive to false")
    void softDelete_setsIsActiveFalse() {
        AccountingSchema schema = AccountingSchema.createNew(
                SchemaEventType.GOODS_RECEIPT, "desc", 1L, 2L, true);

        schema.softDelete();

        assertThat(schema.getIsActive()).isFalse();
    }
}
```

### Use Case Tests

Follow the same pattern as Task 2:

**CreateSchemaUseCaseTest.java** — Read `CreateSchemaUseCaseImpl.java` for exact dependencies. Tests:
- `execute_savesAndReturnsSchema`
- `execute_throwsWhenDuplicateActiveEvent` (if impl checks for this)

**UpdateSchemaUseCaseTest.java** — Read `UpdateSchemaUseCaseImpl.java`. Tests:
- `execute_updatesAndReturnsSchema`
- `execute_throwsWhenNotFound`

**DeleteSchemaUseCaseTest.java** — Read `DeleteSchemaUseCaseImpl.java`. Uses `SchemaInUseChecker`. Tests:
- `execute_hardDeletesWhenNotInUse`
- `execute_softDeletesWhenInUse`
- `execute_throwsWhenNotFound`

**FindSchemasUseCaseTest.java** — Tests:
- `execute_delegatesToRepository`

- [ ] **Step 1: Read all UseCase impl files for Schema to understand exact constructor arguments and logic**
- [ ] **Step 2: Write all test files**
- [ ] **Step 3: Run tests**

Run: `mvn test -pl . -Dtest="com.solusi.erp.accounting.schema.**" -q 2>&1 | tail -20`
Expected: All tests PASS

- [ ] **Step 4: Commit**

```bash
git add src/test/java/com/solusi/erp/accounting/schema/
git commit -m "test: add Schema domain model and use case unit tests"
```

---

## Task 4: FiscalYear + Period Domain + Use Case Tests

**Files:**
- Create: `src/test/java/com/solusi/erp/accounting/period/domain/model/FiscalYearTest.java`
- Create: `src/test/java/com/solusi/erp/accounting/period/domain/model/AccountingPeriodTest.java`
- Create: `src/test/java/com/solusi/erp/accounting/period/application/usecase/command/CreateFiscalYearUseCaseTest.java`
- Create: `src/test/java/com/solusi/erp/accounting/period/application/usecase/command/ClosePeriodUseCaseTest.java`
- Create: `src/test/java/com/solusi/erp/accounting/period/application/usecase/command/ReopenPeriodUseCaseTest.java`
- Create: `src/test/java/com/solusi/erp/accounting/period/application/usecase/query/FindFiscalYearsUseCaseTest.java`

**Goal:** Test the FiscalYear aggregate root including period generation, period state machine (NEVER_OPENED→OPEN→CLOSED), and use case operations. This is the most complex domain model with the most edge cases.

### FiscalYearTest.java (key tests)

```java
package com.solusi.erp.accounting.period.domain.model;

import com.solusi.erp.core.domain.model.AuditMetadata;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("FiscalYear Domain Model Tests")
class FiscalYearTest {

    @Test
    @DisplayName("createNew sets fields with empty periods")
    void createNew_setsFieldsWithEmptyPeriods() {
        FiscalYear fy = FiscalYear.createNew("FY-0001", "FY 2026",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), true);

        assertThat(fy.getId()).isNull();
        assertThat(fy.getCode()).isEqualTo("FY-0001");
        assertThat(fy.getName()).isEqualTo("FY 2026");
        assertThat(fy.getStartDate()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(fy.getEndDate()).isEqualTo(LocalDate.of(2026, 12, 31));
        assertThat(fy.getIsActive()).isTrue();
        assertThat(fy.getPeriods()).isEmpty();
    }

    @Test
    @DisplayName("generateMonthlyPeriods creates 12 periods for full calendar year")
    void generateMonthlyPeriods_creates12PeriodsForFullYear() {
        FiscalYear fy = FiscalYear.createNew("FY-0001", "FY 2026",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), true);

        List<AccountingPeriod> periods = fy.generateMonthlyPeriods(1L);

        assertThat(periods).hasSize(12);
        assertThat(periods.get(0).getCode()).isEqualTo("FY-0001-01");
        assertThat(periods.get(0).getPeriodNumber()).isEqualTo(1);
        assertThat(periods.get(0).getStartDate()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(periods.get(0).getEndDate()).isEqualTo(LocalDate.of(2026, 1, 31));
        assertThat(periods.get(0).getStatus()).isEqualTo(PeriodStatus.NEVER_OPENED);
        assertThat(periods.get(11).getCode()).isEqualTo("FY-0001-12");
        assertThat(periods.get(11).getEndDate()).isEqualTo(LocalDate.of(2026, 12, 31));
    }

    @Test
    @DisplayName("closePeriod transitions OPEN period to CLOSED")
    void closePeriod_transitionsOpenToClosed() {
        FiscalYear fy = buildFyWithOpenPeriod(1L, 10L);

        AccountingPeriod closed = fy.closePeriod(10L);

        assertThat(closed.getStatus()).isEqualTo(PeriodStatus.CLOSED);
    }

    @Test
    @DisplayName("closePeriod throws when period is NEVER_OPENED")
    void closePeriod_throwsWhenNeverOpened() {
        FiscalYear fy = buildFyWithNeverOpenedPeriod(1L, 10L);

        assertThatThrownBy(() -> fy.closePeriod(10L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only OPEN periods can be closed");
    }

    @Test
    @DisplayName("closePeriod throws when period not found in fiscal year")
    void closePeriod_throwsWhenPeriodNotFound() {
        FiscalYear fy = buildFyWithOpenPeriod(1L, 10L);

        assertThatThrownBy(() -> fy.closePeriod(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Period not found");
    }

    @Test
    @DisplayName("reopenPeriod transitions CLOSED to OPEN")
    void reopenPeriod_transitionsClosedToOpen() {
        FiscalYear fy = buildFyWithClosedPeriod(1L, 10L);

        AccountingPeriod reopened = fy.reopenPeriod(10L);

        assertThat(reopened.getStatus()).isEqualTo(PeriodStatus.OPEN);
    }

    @Test
    @DisplayName("reopenPeriod transitions NEVER_OPENED to OPEN")
    void reopenPeriod_transitionsNeverOpenedToOpen() {
        FiscalYear fy = buildFyWithNeverOpenedPeriod(1L, 10L);

        AccountingPeriod reopened = fy.reopenPeriod(10L);

        assertThat(reopened.getStatus()).isEqualTo(PeriodStatus.OPEN);
    }

    @Test
    @DisplayName("reopenPeriod throws when period is already OPEN")
    void reopenPeriod_throwsWhenAlreadyOpen() {
        FiscalYear fy = buildFyWithOpenPeriod(1L, 10L);

        assertThatThrownBy(() -> fy.reopenPeriod(10L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already open");
    }

    @Test
    @DisplayName("getOpenPeriods returns only OPEN status periods")
    void getOpenPeriods_returnsOnlyOpenPeriods() {
        AuditMetadata meta = new AuditMetadata(1L, 1L, null, null, null, null);
        AuditMetadata pMeta1 = new AuditMetadata(10L, 1L, null, null, null, null);
        AuditMetadata pMeta2 = new AuditMetadata(11L, 1L, null, null, null, null);

        AccountingPeriod open = new AccountingPeriod(pMeta1, "P01", "Jan", 1, 1L,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), PeriodStatus.OPEN);
        AccountingPeriod closed = new AccountingPeriod(pMeta2, "P02", "Feb", 2, 1L,
                LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 28), PeriodStatus.CLOSED);

        FiscalYear fy = new FiscalYear(meta, "FY-0001", "FY 2026",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), true,
                new ArrayList<>(List.of(open, closed)));

        assertThat(fy.getOpenPeriods()).hasSize(1);
        assertThat(fy.getOpenPeriods().get(0).getCode()).isEqualTo("P01");
    }

    @Test
    @DisplayName("update changes name and isActive only")
    void update_changesNameAndIsActive() {
        FiscalYear fy = FiscalYear.createNew("FY-0001", "Old", LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31), true);

        fy.update("New Name", false);

        assertThat(fy.getName()).isEqualTo("New Name");
        assertThat(fy.getIsActive()).isFalse();
        assertThat(fy.getCode()).isEqualTo("FY-0001"); // code unchanged
    }

    @Test
    @DisplayName("softDelete sets isActive to false")
    void softDelete_setsIsActiveFalse() {
        FiscalYear fy = FiscalYear.createNew("FY-0001", "FY", LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31), true);

        fy.softDelete();

        assertThat(fy.getIsActive()).isFalse();
    }

    // --- helpers ---

    private FiscalYear buildFyWithOpenPeriod(Long fyId, Long periodId) {
        AuditMetadata fyMeta = new AuditMetadata(fyId, 1L, null, null, null, null);
        AuditMetadata pMeta = new AuditMetadata(periodId, 1L, null, null, null, null);
        AccountingPeriod period = new AccountingPeriod(pMeta, "P01", "Jan", 1, fyId,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), PeriodStatus.OPEN);
        return new FiscalYear(fyMeta, "FY-0001", "FY 2026",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), true,
                new ArrayList<>(List.of(period)));
    }

    private FiscalYear buildFyWithNeverOpenedPeriod(Long fyId, Long periodId) {
        AuditMetadata fyMeta = new AuditMetadata(fyId, 1L, null, null, null, null);
        AuditMetadata pMeta = new AuditMetadata(periodId, 1L, null, null, null, null);
        AccountingPeriod period = new AccountingPeriod(pMeta, "P01", "Jan", 1, fyId,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), PeriodStatus.NEVER_OPENED);
        return new FiscalYear(fyMeta, "FY-0001", "FY 2026",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), true,
                new ArrayList<>(List.of(period)));
    }

    private FiscalYear buildFyWithClosedPeriod(Long fyId, Long periodId) {
        AuditMetadata fyMeta = new AuditMetadata(fyId, 1L, null, null, null, null);
        AuditMetadata pMeta = new AuditMetadata(periodId, 1L, null, null, null, null);
        AccountingPeriod period = new AccountingPeriod(pMeta, "P01", "Jan", 1, fyId,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), PeriodStatus.CLOSED);
        return new FiscalYear(fyMeta, "FY-0001", "FY 2026",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), true,
                new ArrayList<>(List.of(period)));
    }
}
```

### AccountingPeriodTest.java

```java
package com.solusi.erp.accounting.period.domain.model;

import com.solusi.erp.core.domain.model.AuditMetadata;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AccountingPeriod Domain Model Tests")
class AccountingPeriodTest {

    @Test
    @DisplayName("createNew defaults to NEVER_OPENED status")
    void createNew_defaultsToNeverOpened() {
        AccountingPeriod period = AccountingPeriod.createNew(
                "P01", "Jan 2026", 1, 1L,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));

        assertThat(period.getStatus()).isEqualTo(PeriodStatus.NEVER_OPENED);
        assertThat(period.isOpen()).isFalse();
    }

    @Test
    @DisplayName("open sets status to OPEN")
    void open_setsStatusToOpen() {
        AccountingPeriod period = AccountingPeriod.createNew(
                "P01", "Jan", 1, 1L,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));

        period.open();

        assertThat(period.getStatus()).isEqualTo(PeriodStatus.OPEN);
        assertThat(period.isOpen()).isTrue();
    }

    @Test
    @DisplayName("close sets status to CLOSED")
    void close_setsStatusToClosed() {
        AccountingPeriod period = AccountingPeriod.createNew(
                "P01", "Jan", 1, 1L,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));
        period.open();

        period.close();

        assertThat(period.getStatus()).isEqualTo(PeriodStatus.CLOSED);
        assertThat(period.isOpen()).isFalse();
    }

    @Test
    @DisplayName("reopen sets status back to OPEN")
    void reopen_setsStatusToOpen() {
        AccountingPeriod period = AccountingPeriod.createNew(
                "P01", "Jan", 1, 1L,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));
        period.open();
        period.close();

        period.reopen();

        assertThat(period.getStatus()).isEqualTo(PeriodStatus.OPEN);
    }

    @Test
    @DisplayName("all getters return correct values")
    void getters_returnCorrectValues() {
        AuditMetadata meta = new AuditMetadata(5L, 1L, null, null, null, null);
        AccountingPeriod period = new AccountingPeriod(meta, "FY-0001-03", "Mar 2026", 3, 1L,
                LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31), PeriodStatus.OPEN);

        assertThat(period.getId()).isEqualTo(5L);
        assertThat(period.getCode()).isEqualTo("FY-0001-03");
        assertThat(period.getName()).isEqualTo("Mar 2026");
        assertThat(period.getPeriodNumber()).isEqualTo(3);
        assertThat(period.getFiscalYearId()).isEqualTo(1L);
    }
}
```

### Use Case Tests

**ClosePeriodUseCaseTest.java** — Read `ClosePeriodUseCaseImpl.java`. Dependencies: `FiscalYearRepository`. Tests:
- `execute_closesOpenPeriod` — repo returns FY with OPEN period → closePeriod → savePeriod
- `execute_throwsWhenPeriodNotFound` — repo returns empty → DomainException

**ReopenPeriodUseCaseTest.java** — Read `ReopenPeriodUseCaseImpl.java`. Tests:
- `execute_reopensClosedPeriod` — repo returns FY with CLOSED period → reopenPeriod → savePeriod
- `execute_reopensNeverOpenedPeriod`
- `execute_throwsWhenPeriodNotFound`

**CreateFiscalYearUseCaseTest.java** — Read `CreateFiscalYearUseCaseImpl.java`. Dependencies: `FiscalYearRepository`, `SequenceGeneratorService`. Tests:
- `execute_generatesCodeAndSavesFiscalYear`
- `execute_generatesMonthlyPeriods`

**FindFiscalYearsUseCaseTest.java** — Tests:
- `execute_delegatesToRepository`

- [ ] **Step 1: Read all UseCase impl files for Period to understand exact constructor arguments**
- [ ] **Step 2: Write all test files**
- [ ] **Step 3: Run tests**

Run: `mvn test -pl . -Dtest="com.solusi.erp.accounting.period.**" -q 2>&1 | tail -20`
Expected: All tests PASS

- [ ] **Step 4: Commit**

```bash
git add src/test/java/com/solusi/erp/accounting/period/
git commit -m "test: add FiscalYear/Period domain model and use case unit tests"
```

---

## Task 5: COA Web Layer Tests (Controller + Template Static + Integration)

**Files:**
- Create: `src/test/java/com/solusi/erp/accounting/coa/web/controller/CoaControllerTest.java`
- Create: `src/test/java/com/solusi/erp/accounting/coa/web/template/CoaTemplateTest.java`
- Create: `src/test/java/com/solusi/erp/accounting/coa/web/template/integration/CoaListIntegrationTest.java`
- Create: `src/test/java/com/solusi/erp/accounting/coa/web/template/integration/CoaFormIntegrationTest.java`

**Goal:** Complete web-layer test coverage for COA following all 3 patterns.

### Pattern 1: CoaControllerTest.java

```java
package com.solusi.erp.accounting.coa.web.controller;

import com.solusi.erp.accounting.coa.application.usecase.command.CreateCoaUseCase;
import com.solusi.erp.accounting.coa.application.usecase.command.DeleteCoaUseCase;
import com.solusi.erp.accounting.coa.application.usecase.command.UpdateCoaUseCase;
import com.solusi.erp.accounting.coa.application.usecase.query.FindCoaUseCase;
import com.solusi.erp.accounting.coa.application.usecase.query.GetCoaEditViewUseCase;
import com.solusi.erp.accounting.coa.domain.model.AccountType;
import com.solusi.erp.accounting.coa.domain.model.ChartOfAccount;
import com.solusi.erp.accounting.coa.web.dto.CoaDetailResponse;
import com.solusi.erp.accounting.coa.web.dto.CoaSaveRequest;
import com.solusi.erp.accounting.coa.web.dto.CoaSummaryResponse;
import com.solusi.erp.accounting.coa.web.mapper.CoaWebMapper;
import com.solusi.erp.core.domain.model.DeleteResult;
import com.solusi.erp.core.dto.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("CoaController Unit Tests")
class CoaControllerTest {

    private CreateCoaUseCase createCoaUseCase;
    private UpdateCoaUseCase updateCoaUseCase;
    private DeleteCoaUseCase deleteCoaUseCase;
    private FindCoaUseCase findCoaUseCase;
    private GetCoaEditViewUseCase getCoaEditViewUseCase;
    private CoaWebMapper webMapper;
    private MessageSource messageSource;
    private CoaController controller;

    @BeforeEach
    void setUp() {
        createCoaUseCase = mock(CreateCoaUseCase.class);
        updateCoaUseCase = mock(UpdateCoaUseCase.class);
        deleteCoaUseCase = mock(DeleteCoaUseCase.class);
        findCoaUseCase = mock(FindCoaUseCase.class);
        getCoaEditViewUseCase = mock(GetCoaEditViewUseCase.class);
        webMapper = mock(CoaWebMapper.class);
        messageSource = mock(MessageSource.class);
        controller = new CoaController(createCoaUseCase, updateCoaUseCase, deleteCoaUseCase,
                findCoaUseCase, getCoaEditViewUseCase, webMapper, messageSource);
    }

    @Test
    @DisplayName("list returns correct view name and model attributes")
    void list_returnsCorrectViewAndModel() {
        ChartOfAccount domain = ChartOfAccount.createNew("1000", "Cash", AccountType.ASSET,
                null, 1, false, null, true);
        com.solusi.erp.core.domain.model.Page<ChartOfAccount> domainPage =
                new com.solusi.erp.core.domain.model.Page<>(List.of(domain), 0, 20, 1L);
        when(findCoaUseCase.execute(any(), any())).thenReturn(domainPage);

        CoaSummaryResponse summary = new CoaSummaryResponse();
        summary.setId(1L);
        summary.setCode("1000");
        summary.setName("Cash");
        when(webMapper.toSummaryResponse(any(ChartOfAccount.class))).thenReturn(summary);

        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 20);
        Model model = new ExtendedModelMap();

        String view = controller.list(null, pageable, model);

        assertThat(view).isEqualTo("accounting/coa/list");
        assertThat(model.getAttribute("page")).isNotNull();
        assertThat(model.getAttribute("keyword")).isNull();
        assertThat(model.getAttribute("accountTypes")).isEqualTo(AccountType.values());
    }

    @Test
    @DisplayName("showCreateForm returns form view with empty request")
    void showCreateForm_returnsFormViewWithDefaults() {
        Model model = new ExtendedModelMap();

        String view = controller.showCreateForm(model);

        assertThat(view).isEqualTo("accounting/coa/form");
        CoaSaveRequest req = (CoaSaveRequest) model.getAttribute("coaRequest");
        assertThat(req).isNotNull();
        assertThat(req.getIsActive()).isTrue();
        assertThat(req.getIsHeader()).isFalse();
    }

    @Test
    @DisplayName("create returns CREATED with ApiResponse on valid request")
    void create_returnsCreatedResponse() {
        ChartOfAccount domain = ChartOfAccount.createNew("1000", "Cash", AccountType.ASSET,
                null, 1, false, null, true);
        when(createCoaUseCase.execute(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(domain);
        CoaDetailResponse detail = new CoaDetailResponse();
        detail.setId(1L);
        when(webMapper.toDetailResponse(any())).thenReturn(detail);
        when(messageSource.getMessage(eq("msg.success.create"), any(), any())).thenReturn("Created");

        CoaSaveRequest request = new CoaSaveRequest();
        request.setCode("1000");
        request.setName("Cash");
        request.setAccountType("ASSET");

        ResponseEntity<ApiResponse<CoaDetailResponse>> response = controller.create(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getData().getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("create returns BAD_REQUEST for invalid account type")
    void create_returnsBadRequestForInvalidAccountType() {
        CoaSaveRequest request = new CoaSaveRequest();
        request.setAccountType("INVALID_TYPE");

        ResponseEntity<ApiResponse<CoaDetailResponse>> response = controller.create(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("showEditForm populates model with existing COA data")
    void showEditForm_populatesModel() {
        ChartOfAccount domain = ChartOfAccount.createNew("1000", "Cash", AccountType.ASSET,
                null, 1, false, null, true);
        when(getCoaEditViewUseCase.execute(1L)).thenReturn(Optional.of(domain));
        when(webMapper.toSaveRequest(any())).thenReturn(new CoaSaveRequest());
        when(webMapper.toDetailResponse(any())).thenReturn(new CoaDetailResponse());

        Model model = new ExtendedModelMap();
        String view = controller.showEditForm(1L, model);

        assertThat(view).isEqualTo("accounting/coa/form");
        assertThat(model.getAttribute("coaRequest")).isNotNull();
        assertThat(model.getAttribute("auditInfo")).isNotNull();
    }

    @Test
    @DisplayName("showEditForm throws when COA not found")
    void showEditForm_throwsWhenNotFound() {
        when(getCoaEditViewUseCase.execute(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.showEditForm(999L, new ExtendedModelMap()))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("delete returns OK with hard delete result")
    void delete_returnsOkWithHardDelete() {
        when(deleteCoaUseCase.execute(1L)).thenReturn(DeleteResult.HARD_DELETED);
        when(messageSource.getMessage(eq("msg.success.delete"), any(), any())).thenReturn("Deleted");

        ResponseEntity<Void> response = controller.delete(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("delete returns OK with soft delete result (deactivated)")
    void delete_returnsOkWithSoftDelete() {
        when(deleteCoaUseCase.execute(1L)).thenReturn(DeleteResult.SOFT_DELETED);
        when(messageSource.getMessage(eq("msg.success.deactivated"), any(), any())).thenReturn("Deactivated");

        ResponseEntity<Void> response = controller.delete(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
```

### Pattern 2: CoaTemplateTest.java

```java
package com.solusi.erp.accounting.coa.web.template;

import com.solusi.erp.accounting.coa.web.dto.CoaSummaryResponse;
import com.solusi.erp.accounting.coa.web.dto.CoaSaveRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("COA Template Static Tests")
class CoaTemplateTest {

    @Test
    @DisplayName("list template contains expected fragment ID and DTO properties")
    void listTemplate_containsExpectedFragmentAndProperties() throws Exception {
        String html = readTemplate("templates/accounting/coa/list.html");

        assertThat(html).contains("coa-table-container");
        assertThat(html).contains("${item.code}");
        assertThat(html).contains("${item.name}");
        assertThat(html).contains("${item.accountType}");
        assertThat(html).contains("${item.normalBalance}");
        assertThat(html).contains("${item.level}");
        assertThat(html).contains("${item.isHeader}");
        assertThat(html).contains("${item.isActive}");
    }

    @Test
    @DisplayName("list template references valid CoaSummaryResponse properties")
    void listTemplate_referencesValidDtoProperties() throws Exception {
        String html = readTemplate("templates/accounting/coa/list.html");
        assertAllItemPropertiesExistOn(html, CoaSummaryResponse.class);
    }

    @Test
    @DisplayName("form template uses coaRequest model attribute")
    void formTemplate_usesCorrectModelAttribute() throws Exception {
        String html = readTemplate("templates/accounting/coa/form.html");

        assertThat(html).contains("coaRequest");
        assertThat(html).contains("field='code'");
        assertThat(html).contains("field='name'");
        assertThat(html).contains("*{accountType}");
        assertThat(html).contains("*{isActive}");
        assertThat(html).contains("*{isHeader}");
    }

    @Test
    @DisplayName("list template has HTMX search trigger")
    void listTemplate_hasHtmxSearchTrigger() throws Exception {
        String html = readTemplate("templates/accounting/coa/list.html");

        assertThat(html).contains("hx-get=\"/accounting/coa\"");
        assertThat(html).contains("hx-target=\"#coa-table-container\"");
    }

    @Test
    @DisplayName("list template has sec:authorize for CREATE, UPDATE, DELETE")
    void listTemplate_hasSecAuthorizeExpressions() throws Exception {
        String html = readTemplate("templates/accounting/coa/list.html");

        assertThat(html).contains("hasAuthority('ACCOUNTING-COA_CREATE')");
        assertThat(html).contains("hasAuthority('ACCOUNTING-COA_UPDATE')");
        assertThat(html).contains("hasAuthority('ACCOUNTING-COA_DELETE')");
    }

    private String readTemplate(String path) throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream(path);
        assertThat(is).as("Template not found: " + path).isNotNull();
        return new String(is.readAllBytes(), StandardCharsets.UTF_8);
    }

    private void assertAllItemPropertiesExistOn(String html, Class<?> dtoClass) {
        Pattern p = Pattern.compile("\\$\\{item\\.([a-zA-Z0-9_]+)\\}");
        Matcher m = p.matcher(html);
        while (m.find()) {
            String prop = m.group(1);
            String getter = "get" + prop.substring(0, 1).toUpperCase() + prop.substring(1);
            String isGetter = "is" + prop.substring(0, 1).toUpperCase() + prop.substring(1);
            boolean found = false;
            for (java.lang.reflect.Method method : dtoClass.getMethods()) {
                if ((method.getName().equals(getter) || method.getName().equals(isGetter))
                        && method.getParameterCount() == 0) {
                    found = true;
                    break;
                }
            }
            assertThat(found).withFailMessage(
                    "Property '%s' in template but no getter on %s", prop, dtoClass.getSimpleName()
            ).isTrue();
        }
    }
}
```

### Pattern 3: CoaListIntegrationTest.java

Follow BrandListIntegrationTest structure exactly:

```java
package com.solusi.erp.accounting.coa.web.template.integration;

import com.solusi.erp.testutils.TemplateTestUtils;
import com.solusi.erp.testutils.TestDtoFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("COA List — Template Integration Test (sec:authorize visibility)")
@Tag("integration-template")
class CoaListIntegrationTest {

    private static final String TEMPLATE = "accounting/coa/list";

    private Authentication auth(String... authorities) {
        TestingAuthenticationToken token = new TestingAuthenticationToken("testuser", "n/a", authorities);
        token.setAuthenticated(true);
        return token;
    }

    private Map<String, Object> modelWithEmptyPage() {
        var page = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        return Map.of("page", page, "keyword", "",
                "accountTypes", com.solusi.erp.accounting.coa.domain.model.AccountType.values());
    }

    private Map<String, Object> modelWithOneCoa() {
        var dto = TestDtoFactory.sampleCoaSummaryResponse();
        var page = new PageImpl<>(List.of(dto), PageRequest.of(0, 20), 1);
        return Map.of("page", page, "keyword", "",
                "accountTypes", com.solusi.erp.accounting.coa.domain.model.AccountType.values());
    }

    @Test
    @DisplayName("Template renders without error for ACCOUNTING-COA_READ user")
    void withCoaRead_templateRendersSuccessfully() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithEmptyPage(), auth("ACCOUNTING-COA_READ"));
        assertThat(html).isNotBlank();
        assertThat(html).contains("coa-table-container");
    }

    @Test
    @DisplayName("sec:authorize — ACCOUNTING-COA_CREATE shows Add button")
    void withCoaCreate_addButtonIsVisible() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithEmptyPage(), auth("ACCOUNTING-COA_READ", "ACCOUNTING-COA_CREATE"));
        assertThat(html).contains("/accounting/coa/create");
    }

    @Test
    @DisplayName("sec:authorize — missing ACCOUNTING-COA_CREATE hides Add button")
    void withoutCoaCreate_addButtonIsHidden() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithEmptyPage(), auth("ACCOUNTING-COA_READ"));
        assertThat(html).doesNotContain("/accounting/coa/create");
    }

    @Test
    @DisplayName("sec:authorize — ACCOUNTING-COA_UPDATE shows Edit button")
    void withCoaUpdate_editButtonIsVisible() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithOneCoa(), auth("ACCOUNTING-COA_READ", "ACCOUNTING-COA_UPDATE"));
        assertThat(html).contains("Cash");
        assertThat(html).contains("/accounting/coa/edit");
    }

    @Test
    @DisplayName("sec:authorize — missing ACCOUNTING-COA_UPDATE hides Edit button")
    void withoutCoaUpdate_editButtonIsHidden() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithOneCoa(), auth("ACCOUNTING-COA_READ"));
        assertThat(html).contains("Cash");
        assertThat(html).doesNotContain("/accounting/coa/edit");
    }

    @Test
    @DisplayName("sec:authorize — ACCOUNTING-COA_DELETE shows Delete button")
    void withCoaDelete_deleteButtonIsVisible() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithOneCoa(), auth("ACCOUNTING-COA_READ", "ACCOUNTING-COA_DELETE"));
        assertThat(html).contains("modal-delete-1");
    }

    @Test
    @DisplayName("sec:authorize — missing ACCOUNTING-COA_DELETE hides Delete button")
    void withoutCoaDelete_deleteButtonIsHidden() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithOneCoa(), auth("ACCOUNTING-COA_READ"));
        assertThat(html).doesNotContain("modal-delete-1");
    }
}
```

### Pattern 3: CoaFormIntegrationTest.java

Follow BrandFormIntegrationTest structure. Test `coaRequest` model binding:

- `createForm_rendersSuccessfully` — `coaRequest` with no id
- `editForm_rendersSuccessfully` — `coaRequest` with id set
- `formContainsExpectedStructure` — raw scan for `coaRequest`, `field='code'`, `field='name'`
- `coaSaveRequestHasAllFormBindingProperties` — verify properties: code, name, accountType, parentId, level, isHeader, note, isActive, version

- [ ] **Step 1: Write all 4 test files**
- [ ] **Step 2: Run tests**

Run: `mvn test -pl . -Dtest="com.solusi.erp.accounting.coa.web.**" -q 2>&1 | tail -20`
Expected: All tests PASS

- [ ] **Step 3: Commit**

```bash
git add src/test/java/com/solusi/erp/accounting/coa/web/
git commit -m "test: add COA web-layer tests (controller, template static, integration)"
```

---

## Task 6: Schema Web Layer Tests (Controller + Template Static + Integration)

**Files:**
- Create: `src/test/java/com/solusi/erp/accounting/schema/web/controller/SchemaControllerTest.java`
- Create: `src/test/java/com/solusi/erp/accounting/schema/web/template/SchemaTemplateTest.java`
- Create: `src/test/java/com/solusi/erp/accounting/schema/web/template/integration/SchemaListIntegrationTest.java`
- Create: `src/test/java/com/solusi/erp/accounting/schema/web/template/integration/SchemaFormIntegrationTest.java`

**Goal:** Complete web-layer tests for Accounting Schema. Same 3-pattern approach as COA.

### SchemaControllerTest.java key tests:

Follow same structure as CoaControllerTest. Mock all 5 use cases + mapper + messageSource. Constructor: `new SchemaController(createSchemaUseCase, updateSchemaUseCase, deleteSchemaUseCase, findSchemasUseCase, getSchemaEditViewUseCase, webMapper, messageSource)`.

Tests:
- `list_returnsCorrectViewAndModel` — view = `"accounting/schema/list"`, model has `page`, `keyword`, `eventTypes`
- `showCreateForm_returnsFormView` — view = `"accounting/schema/form"`, model has `schemaRequest` with isActive=true, `eventTypes`
- `create_returnsCreatedResponse` — valid request with `eventType="GOODS_RECEIPT"`
- `create_returnsBadRequestForInvalidEventType` — request with `eventType="INVALID"`
- `showEditForm_populatesModel` — finds schema, maps to SaveRequest and DetailResponse
- `showEditForm_throwsWhenNotFound` — returns empty Optional
- `update_returnsOkResponse`
- `delete_hardDeleteReturnsOk`
- `delete_softDeleteReturnsOk`

### SchemaTemplateTest.java key assertions:

- list template: `"schema-table-container"`, `${item.eventType}`, `${item.description}`, `${item.debitAccountName}`, `${item.creditAccountName}`, `${item.isActive}`
- list template references valid `SchemaSummaryResponse` properties
- form template: `"schemaRequest"`, `*{eventType}`, `*{debitAccountId}`, `*{creditAccountId}`, `*{description}`, `*{isActive}`
- list template has sec:authorize for `ACCOUNTING-SCHEMA_CREATE`, `ACCOUNTING-SCHEMA_UPDATE`, `ACCOUNTING-SCHEMA_DELETE`

### SchemaListIntegrationTest.java key tests:

- `withSchemaRead_templateRendersSuccessfully`
- `withSchemaCreate_addButtonIsVisible` — contains `"/accounting/schemas/create"`
- `withoutSchemaCreate_addButtonIsHidden` — doesNotContain `"/accounting/schemas/create"`
- `withSchemaUpdate_editButtonIsVisible` — contains `"/accounting/schemas/edit"`
- `withoutSchemaUpdate_editButtonIsHidden`
- `withSchemaDelete_deleteButtonIsVisible` — contains `"modal-delete-1"`
- `withoutSchemaDelete_deleteButtonIsHidden`

Model: `Map.of("page", page, "keyword", "", "eventTypes", SchemaEventType.values())`

### SchemaFormIntegrationTest.java:

- `createForm_rendersSuccessfully`
- `editForm_rendersSuccessfully`
- `schemaSaveRequestHasAllFormBindingProperties` — verify: eventType, description, debitAccountId, creditAccountId, isActive, version

- [ ] **Step 1: Write all 4 test files**
- [ ] **Step 2: Run tests**

Run: `mvn test -pl . -Dtest="com.solusi.erp.accounting.schema.web.**" -q 2>&1 | tail -20`
Expected: All tests PASS

- [ ] **Step 3: Commit**

```bash
git add src/test/java/com/solusi/erp/accounting/schema/web/
git commit -m "test: add Schema web-layer tests (controller, template static, integration)"
```

---

## Task 7: Period Web Layer Tests (Controller + Template Static + Integration)

**Files:**
- Create: `src/test/java/com/solusi/erp/accounting/period/web/controller/PeriodControllerTest.java`
- Create: `src/test/java/com/solusi/erp/accounting/period/web/template/PeriodTemplateTest.java`
- Create: `src/test/java/com/solusi/erp/accounting/period/web/template/integration/PeriodListIntegrationTest.java`
- Create: `src/test/java/com/solusi/erp/accounting/period/web/template/integration/PeriodDetailIntegrationTest.java`
- Create: `src/test/java/com/solusi/erp/accounting/period/web/template/integration/PeriodFormIntegrationTest.java`

**Goal:** Complete web-layer tests for the most complex module (FiscalYear/Period). Includes the detail page with period state machine buttons.

### PeriodControllerTest.java — Most complex controller (8 endpoints)

Mock all 7 use cases + mapper + messageSource. Constructor: `new PeriodController(createFiscalYearUseCase, updateFiscalYearUseCase, deleteFiscalYearUseCase, closePeriodUseCase, reopenPeriodUseCase, findFiscalYearsUseCase, getFiscalYearDetailUseCase, webMapper, messageSource)`.

Tests:
- `list_returnsCorrectViewAndModel` — view = `"accounting/period/list"`, model has `page`, `keyword`
- `showCreateForm_returnsFormView` — view = `"accounting/period/form"`, model has `fyRequest` with isActive=true
- `create_returnsCreatedResponse` — valid request with name, startDate, endDate
- `detail_returnsDetailViewWithFiscalYear` — view = `"accounting/period/detail"`, model has `fy`
- `detail_throwsWhenNotFound`
- `showEditForm_populatesModel`
- `update_returnsOkResponse`
- `delete_hardDeleteReturnsOk`
- `delete_softDeleteReturnsOk`
- `closePeriod_returnsOkWithHxRefresh` — verify response has `"HX-Refresh"` header = `"true"`
- `reopenPeriod_returnsOkWithHxRefresh`

### PeriodTemplateTest.java key assertions:

- list template: `"period-table-container"`, `${item.code}`, `${item.name}`, `${item.startDate}`, `${item.endDate}`, `${item.periodCount}`, `${item.isActive}`
- list template references valid `FiscalYearSummaryResponse` properties
- detail template: `${fy.code}`, `${fy.name}`, `${fy.startDate}`, `${fy.endDate}`, `${fy.isActive}`, `${period.code}`, `${period.name}`, `${period.status}`
- detail template has status badges: `NEVER_OPENED`, `OPEN`, `CLOSED`
- form template: `"fyRequest"`, `*{name}`, `*{startDate}`, `*{endDate}`, `*{isActive}`
- list template has sec:authorize for `ACCOUNTING-PERIOD_CREATE`, `ACCOUNTING-PERIOD_UPDATE`, `ACCOUNTING-PERIOD_DELETE`
- detail template has sec:authorize for `ACCOUNTING-PERIOD_UPDATE` (close/reopen buttons)

### PeriodListIntegrationTest.java:

- `withPeriodRead_templateRendersSuccessfully`
- `withPeriodCreate_addButtonIsVisible` — contains `"/accounting/periods/create"`
- `withoutPeriodCreate_addButtonIsHidden`
- `withPeriodUpdate_editButtonIsVisible` — contains `"/accounting/periods/edit"`
- `withoutPeriodUpdate_editButtonIsHidden`
- `withPeriodDelete_deleteButtonIsVisible` — contains `"modal-delete-1"`
- `withoutPeriodDelete_deleteButtonIsHidden`

Model: `Map.of("page", page, "keyword", "")`

### PeriodDetailIntegrationTest.java (NEW — no equivalent in brand):

This is a unique template because it renders FiscalYear detail with period rows and close/reopen buttons.

```java
package com.solusi.erp.accounting.period.web.template.integration;

import com.solusi.erp.accounting.period.web.dto.FiscalYearDetailResponse;
import com.solusi.erp.accounting.period.web.dto.PeriodResponse;
import com.solusi.erp.testutils.TemplateTestUtils;
import com.solusi.erp.testutils.TestDtoFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Period Detail — Template Integration Test (sec:authorize visibility)")
@Tag("integration-template")
class PeriodDetailIntegrationTest {

    private static final String TEMPLATE = "accounting/period/detail";

    private Authentication auth(String... authorities) {
        TestingAuthenticationToken token = new TestingAuthenticationToken("testuser", "n/a", authorities);
        token.setAuthenticated(true);
        return token;
    }

    private FiscalYearDetailResponse buildFyWithPeriods(String... statuses) {
        FiscalYearDetailResponse fy = new FiscalYearDetailResponse();
        fy.setId(1L);
        fy.setCode("FY-0001");
        fy.setName("Fiscal Year 2026");
        fy.setStartDate(LocalDate.of(2026, 1, 1));
        fy.setEndDate(LocalDate.of(2026, 12, 31));
        fy.setIsActive(true);
        List<PeriodResponse> periods = new java.util.ArrayList<>();
        for (int i = 0; i < statuses.length; i++) {
            periods.add(TestDtoFactory.samplePeriodResponse(
                    (long)(i + 1), "FY-0001-" + String.format("%02d", i + 1),
                    "Period " + (i + 1), i + 1, statuses[i]));
        }
        fy.setPeriods(periods);
        return fy;
    }

    @Test
    @DisplayName("Detail renders without error for READ user")
    void withPeriodRead_templateRendersSuccessfully() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, Map.of("fy", buildFyWithPeriods("NEVER_OPENED")),
                auth("ACCOUNTING-PERIOD_READ"));
        assertThat(html).isNotBlank();
        assertThat(html).contains("FY-0001");
        assertThat(html).contains("Fiscal Year 2026");
    }

    @Test
    @DisplayName("sec:authorize — ACCOUNTING-PERIOD_UPDATE shows action buttons column")
    void withPeriodUpdate_actionButtonsVisible() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, Map.of("fy", buildFyWithPeriods("OPEN")),
                auth("ACCOUNTING-PERIOD_READ", "ACCOUNTING-PERIOD_UPDATE"));
        // Close button for OPEN period should be visible
        assertThat(html).contains("/close");
    }

    @Test
    @DisplayName("sec:authorize — missing ACCOUNTING-PERIOD_UPDATE hides action buttons")
    void withoutPeriodUpdate_actionButtonsHidden() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, Map.of("fy", buildFyWithPeriods("OPEN")),
                auth("ACCOUNTING-PERIOD_READ"));
        assertThat(html).doesNotContain("/close");
        assertThat(html).doesNotContain("/reopen");
    }

    @Test
    @DisplayName("NEVER_OPENED period shows Open button")
    void neverOpenedPeriod_showsOpenButton() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, Map.of("fy", buildFyWithPeriods("NEVER_OPENED")),
                auth("ACCOUNTING-PERIOD_READ", "ACCOUNTING-PERIOD_UPDATE"));
        assertThat(html).contains("/reopen");
    }

    @Test
    @DisplayName("CLOSED period shows Reopen button")
    void closedPeriod_showsReopenButton() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, Map.of("fy", buildFyWithPeriods("CLOSED")),
                auth("ACCOUNTING-PERIOD_READ", "ACCOUNTING-PERIOD_UPDATE"));
        assertThat(html).contains("/reopen");
    }
}
```

### PeriodFormIntegrationTest.java:

- `createForm_rendersSuccessfully`
- `editForm_rendersSuccessfully`
- `fyRequestHasAllFormBindingProperties` — verify: name, startDate, endDate, isActive, version

- [ ] **Step 1: Write all 5 test files**
- [ ] **Step 2: Run tests**

Run: `mvn test -pl . -Dtest="com.solusi.erp.accounting.period.web.**" -q 2>&1 | tail -20`
Expected: All tests PASS

- [ ] **Step 3: Commit**

```bash
git add src/test/java/com/solusi/erp/accounting/period/web/
git commit -m "test: add Period web-layer tests (controller, template static, integration)"
```

---

## Task 8: JaCoCo Threshold Bump + Full Test Suite Verification

**Files:**
- Modify: `pom.xml` (JaCoCo check thresholds)

**Goal:** Raise JaCoCo thresholds to 80% line / 75% branch. Run full test suite. Fix any failures.

- [ ] **Step 1: Update JaCoCo thresholds in pom.xml**

In the JaCoCo `check` execution, change:

```xml
<!-- OLD -->
<minimum>0.60</minimum>  <!-- LINE -->
<minimum>0.50</minimum>  <!-- BRANCH -->

<!-- NEW -->
<minimum>0.80</minimum>  <!-- LINE -->
<minimum>0.75</minimum>  <!-- BRANCH -->
```

- [ ] **Step 2: Run full test suite**

Run: `mvn clean test -q 2>&1 | tail -30`
Expected: All tests PASS, JaCoCo check PASS with new thresholds

- [ ] **Step 3: If JaCoCo fails, check the report**

Run: `mvn jacoco:report -q && type target\site\jacoco\index.html | findstr "Total"`

If coverage is below threshold, identify untested code and add targeted tests.

- [ ] **Step 4: Commit**

```bash
git add pom.xml
git commit -m "chore: raise JaCoCo coverage thresholds to 80% line / 75% branch"
```

- [ ] **Step 5: Final verification — run full build**

Run: `mvn clean verify -q 2>&1 | tail -10`
Expected: BUILD SUCCESS

---

## Summary

| Task | Scope | Files |
|------|-------|-------|
| 1 | TestDtoFactory extensions | 1 modify |
| 2 | COA domain + use case tests | 5 create |
| 3 | Schema domain + use case tests | 5 create |
| 4 | Period domain + use case tests | 6 create |
| 5 | COA web-layer tests (3 patterns) | 4 create |
| 6 | Schema web-layer tests (3 patterns) | 4 create |
| 7 | Period web-layer tests (3 patterns) | 5 create |
| 8 | JaCoCo threshold + verification | 1 modify |

**Total: 28 new files + 2 modifications, 8 commits**
