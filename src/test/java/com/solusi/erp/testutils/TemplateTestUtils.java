package com.solusi.erp.testutils;

import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.security.access.expression.AbstractSecurityExpressionHandler;
import org.springframework.security.access.expression.SecurityExpressionOperations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.expression.WebSecurityExpressionRoot;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.IContext;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.extras.springsecurity6.dialect.SpringSecurityDialect;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.StringTemplateResolver;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import java.util.Locale;
import java.util.Map;

/**
 * Lightweight Thymeleaf helper for tests.
 *
 * <p>Two rendering modes:</p>
 * <ul>
 *   <li>{@link #renderFragment} — static pre-processing: strips th:replace and sec:authorize.
 *       Use for asserting DTO property placeholders exist.</li>
 *   <li>{@link #renderWithSecurity} — runtime Thymeleaf + SpringSecurityDialect:
 *       evaluates sec:authorize against a supplied Authentication.
 *       Use for authority-based visibility tests.</li>
 * </ul>
 */
public class TemplateTestUtils {

    private static final TemplateEngine ENGINE = createTemplateEngine();

    /**
     * Cached security WebApplicationContext. Shared across all tests to avoid
     * paying the context startup cost repeatedly. The context only provides
     * the SecurityExpressionHandler infrastructure — it carries no per-test state.
     */
    private static volatile WebApplicationContext SECURITY_WEB_CTX;

    private static TemplateEngine createTemplateEngine() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCacheable(false);

        TemplateEngine engine = new TemplateEngine();
        engine.setTemplateResolver(resolver);
        return engine;
    }

    public static String render(String templateName, IContext context) {
        return ENGINE.process(templateName, context);
    }

    public static String renderFragment(String templateName, String fragmentName, IContext context) {
        String resourcePath = "templates/" + templateName + ".html";
        java.io.InputStream is = TemplateTestUtils.class.getClassLoader().getResourceAsStream(resourcePath);
        if (is == null) {
            String fullFallback = ENGINE.process(templateName, context);
            return extractById(fullFallback, fragmentName);
        }
        String raw;
        try (java.io.InputStream in = is) {
            raw = new String(in.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }

        // Replace @{/...} occurrences with literal paths '/...'
        // Handles simple @{/path}, parameterised @{/path/{var}(var=${val})},
        // and query-param @{/path(param=${val})} forms.
        String preprocessed = raw.replaceAll(
                "@\\{(/[^}{(]*)(?:\\{[^}]*\\})?[^){(]*(?:\\([^)]*\\))?\\}", "'$1'");
        // Remove th:replace fragment includes
        preprocessed = preprocessed.replaceAll("th:replace\\s*=\\s*\"[^\\\"]*\"", "");
        // Remove sec:authorize so the static check doesn't need Spring Security dialect
        preprocessed = preprocessed.replaceAll("sec:authorize\\s*=\\s*\"[^\\\"]*\"", "");
        preprocessed = preprocessed.replaceAll("th:replace\\s*=\\s*'[^']*'", "");

        return extractById(preprocessed, fragmentName);
    }

    private static String extractById(String full, String fragmentName) {
        String regex = "(?s)<([a-zA-Z0-9\\-]+)[^>]*id\\s*=\\s*\"" + fragmentName + "\"[^>]*>.*?</\\1>";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(regex);
        java.util.regex.Matcher m = p.matcher(full);
        if (m.find()) {
            return m.group(0);
        }
        return full;
    }

    /**
     * Renders a template with an active Spring Security context so that
     * {@code sec:authorize} expressions are evaluated at runtime.
     *
     * <p>Template pre-processing applied before rendering:</p>
     * <ul>
     *   <li>{@code @{/...}} link expressions → literal {@code '/...'} strings.</li>
     *   <li>{@code th:replace} directives removed (no layout resolution needed).</li>
     *   <li>{@code sec:authorize} kept for runtime evaluation by SpringSecurityDialect.</li>
     * </ul>
     *
     * <p>A minimal {@link GenericWebApplicationContext} containing a single
     * {@link TestFilterInvocationExpressionHandler} bean is created (and cached statically)
     * so that {@code thymeleaf-extras-springsecurity6} can resolve a
     * {@code SecurityExpressionHandler&lt;FilterInvocation&gt;} without starting a full
     * Spring Security context or triggering component-scan conflicts.</p>
     *
     * @param templateName logical template path relative to {@code templates/} (without .html)
     * @param variables    model variables to expose to the template
     * @param auth         authentication to place in {@link SecurityContextHolder}
     * @return rendered HTML string
     */
    public static String renderWithSecurity(String templateName,
                                            Map<String, Object> variables,
                                            Authentication auth) {
        org.springframework.security.core.context.SecurityContext secCtx =
                SecurityContextHolder.createEmptyContext();
        secCtx.setAuthentication(auth);
        SecurityContextHolder.setContext(secCtx);

        try {
            // Read and pre-process template source
            String resourcePath = "templates/" + templateName + ".html";
            java.io.InputStream is =
                    TemplateTestUtils.class.getClassLoader().getResourceAsStream(resourcePath);
            if (is == null) {
                throw new RuntimeException("Template not found on classpath: " + resourcePath);
            }
            String raw;
            try (java.io.InputStream in = is) {
                raw = new String(in.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            } catch (java.io.IOException e) {
                throw new RuntimeException(e);
            }

            String preprocessed = raw.replaceAll(
                    "@\\{(/[^}{(]*)(?:\\{[^}]*\\})?[^){(]*(?:\\([^)]*\\))?\\}", "'$1'");
            preprocessed = preprocessed.replaceAll("th:replace\\s*=\\s*\"[^\"]*\"", "");
            preprocessed = preprocessed.replaceAll("th:replace\\s*=\\s*'[^']*'", "");
            // Strip th:insert (same as th:replace for our purposes)
            preprocessed = preprocessed.replaceAll("th:insert\\s*=\\s*\"[^\"]*\"", "");
            preprocessed = preprocessed.replaceAll("th:insert\\s*=\\s*'[^']*'", "");
            // Strip CSRF token attributes — _csrf is null in the test WebContext (no Spring Security
            // RequestDataValueProcessor wired). Without stripping, OGNL throws on _csrf.parameterName.
            preprocessed = preprocessed.replaceAll("th:name\\s*=\\s*\"\\$\\{_csrf[^\"]*\\}\"", "");
            preprocessed = preprocessed.replaceAll("th:value\\s*=\\s*\"\\$\\{_csrf[^\"]*\\}\"", "");
            // Strip Spring MVC binding error expressions (th:if with #fields, th:errors)
            // These require Spring BindingResult which is unavailable in test context
            preprocessed = preprocessed.replaceAll("th:if\\s*=\\s*\"\\$\\{#fields\\.hasErrors[^\"]*\\}\"", "");
            preprocessed = preprocessed.replaceAll("th:errors\\s*=\\s*\"[^\"]*\"", "");
            // sec:authorize intentionally kept for runtime evaluation

            // Build engine with SpringSecurityDialect + StringTemplateResolver
            StringTemplateResolver stringResolver = new StringTemplateResolver();
            stringResolver.setTemplateMode(TemplateMode.HTML);
            stringResolver.setCacheable(false);

            TemplateEngine engine = new TemplateEngine();
            engine.setTemplateResolver(stringResolver);
            engine.addDialect(new SpringSecurityDialect());

            // Build a minimal web exchange (required by SpringSecurityDialect for IWebContext)
            org.springframework.mock.web.MockServletContext servletCtx =
                    new org.springframework.mock.web.MockServletContext();
            org.springframework.mock.web.MockHttpServletRequest request =
                    new org.springframework.mock.web.MockHttpServletRequest(servletCtx);
            request.setMethod("GET");
            org.springframework.mock.web.MockHttpServletResponse response =
                    new org.springframework.mock.web.MockHttpServletResponse();

            // thymeleaf-extras-springsecurity6 looks up SecurityExpressionHandler<FilterInvocation>
            // from the WebApplicationContext via WebApplicationContextUtils.
            // @EnableWebSecurity auto-registers this bean via WebSecurityConfiguration.
            WebApplicationContext secCtxApp = getOrCreateSecurityWebContext(servletCtx);
            servletCtx.setAttribute(
                    WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE,
                    secCtxApp);

            JakartaServletWebApplication webApp =
                    JakartaServletWebApplication.buildApplication(servletCtx);
            org.thymeleaf.web.IWebExchange exchange = webApp.buildExchange(request, response);

            WebContext ctx = new WebContext(exchange, Locale.getDefault());
            ctx.setVariables(variables);

            return engine.process(preprocessed, ctx);

        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    /**
     * Lazily creates and caches a minimal WebApplicationContext containing a single
     * {@code SecurityExpressionHandler<FilterInvocation>} bean.
     *
     * <p>{@code thymeleaf-extras-springsecurity6} v3.1.x calls
     * {@code AuthUtils.getExpressionHandler()} which looks up the bean via
     * {@code ctx.getBeansOfType(SecurityExpressionHandler.class)} and checks the
     * resolved generic type argument equals {@code FilterInvocation.class}.
     * {@link TestFilterInvocationExpressionHandler} satisfies this contract without
     * starting a full {@code @EnableWebSecurity} context — avoiding collisions with
     * the application's own {@code SecurityConfig} when {@code @SpringBootTest} scans
     * the test classpath.</p>
     */
    private static WebApplicationContext getOrCreateSecurityWebContext(
            org.springframework.mock.web.MockServletContext servletCtx) {
        if (SECURITY_WEB_CTX == null) {
            synchronized (TemplateTestUtils.class) {
                if (SECURITY_WEB_CTX == null) {
                    DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
                    beanFactory.registerSingleton(
                            "webSecurityExpressionHandler",
                            new TestFilterInvocationExpressionHandler());
                    GenericWebApplicationContext ctx =
                            new GenericWebApplicationContext(beanFactory);
                    ctx.setServletContext(servletCtx);
                    ctx.refresh(); // triggers ApplicationContextAware injection
                    SECURITY_WEB_CTX = ctx;
                }
            }
        }
        return SECURITY_WEB_CTX;
    }

    /**
     * Minimal {@code SecurityExpressionHandler<FilterInvocation>} for test rendering.
     *
     * <p>Extends {@link AbstractSecurityExpressionHandler} with the {@code FilterInvocation}
     * type parameter, which is what {@code thymeleaf-extras-springsecurity6} resolves via
     * {@code GenericTypeResolver}. Uses {@link WebSecurityExpressionRoot} (public API in
     * spring-security-web) to evaluate {@code hasAuthority()}, {@code hasRole()}, etc.
     * against the current {@link SecurityContextHolder}.</p>
     *
     * <p>This class intentionally carries <em>no Spring stereotype annotations</em>
     * ({@code @Configuration}, {@code @Component}, etc.) so that Spring Boot's component
     * scanner does not pick it up during {@code @SpringBootTest} test runs.</p>
     */
    static final class TestFilterInvocationExpressionHandler
            extends AbstractSecurityExpressionHandler<FilterInvocation> {

        @Override
        protected SecurityExpressionOperations createSecurityExpressionRoot(
                Authentication auth, FilterInvocation filterInvocation) {
            WebSecurityExpressionRoot root = new WebSecurityExpressionRoot(auth, filterInvocation);
            root.setPermissionEvaluator(getPermissionEvaluator());
            root.setRoleHierarchy(getRoleHierarchy());
            return root;
        }
    }
}

