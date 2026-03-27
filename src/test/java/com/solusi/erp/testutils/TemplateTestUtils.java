package com.solusi.erp.testutils;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.IContext;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templatemode.TemplateMode;

/**
 * Lightweight Thymeleaf helper for tests. Renders templates from classpath:/templates/.
 * Use renderFragment(templateName, fragmentName, context) to render a specific fragment.
 */
public class TemplateTestUtils {

    private static final TemplateEngine ENGINE = createTemplateEngine();

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
        String fragmentExpression = templateName + " :: " + fragmentName;
        return ENGINE.process(fragmentExpression, context);
    }
}
