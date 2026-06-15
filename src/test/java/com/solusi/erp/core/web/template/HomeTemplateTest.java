package com.solusi.erp.core.web.template;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Static template check for home.html.
 * Verifies key sections and elements exist without Spring context.
 */
@DisplayName("Home — Static Template Check")
@Tag("template")
class HomeTemplateTest {

    private static final String TEMPLATE = "home";
    private static final List<String> LANDING_KEYS = List.of(
            "landing.welcome",
            "landing.hero.title",
            "landing.hero.subtitle",
            "landing.btn.workflow",
            "landing.btn.start",
            "landing.login",
            "landing.image.open",
            "landing.image.close",
            "landing.preview.label",
            "landing.preview.title",
            "landing.preview.subtitle",
            "landing.metric.modules",
            "landing.metric.modules.label",
            "landing.metric.migrations",
            "landing.metric.migrations.label",
            "landing.metric.stack",
            "landing.metric.stack.label",
            "landing.flow.title",
            "landing.flow.subtitle",
            "landing.flow.po.title",
            "landing.flow.po.desc",
            "landing.flow.gr.title",
            "landing.flow.gr.desc",
            "landing.flow.stock.title",
            "landing.flow.stock.desc",
            "landing.flow.bill.title",
            "landing.flow.bill.desc",
            "landing.flow.payment.title",
            "landing.flow.payment.desc",
            "landing.flow.journal.title",
            "landing.flow.journal.desc",
            "landing.showcase.title",
            "landing.showcase.subtitle",
            "landing.showcase.procurement.title",
            "landing.showcase.procurement.desc",
            "landing.showcase.inventory.title",
            "landing.showcase.inventory.desc",
            "landing.showcase.ap.title",
            "landing.showcase.ap.desc",
            "landing.showcase.payment.title",
            "landing.showcase.payment.desc",
            "landing.showcase.accounting.title",
            "landing.showcase.accounting.desc",
            "landing.features.title",
            "landing.features.subtitle",
            "landing.feature.approval.title",
            "landing.feature.approval.desc",
            "landing.feature.rbac.title",
            "landing.feature.rbac.desc",
            "landing.feature.audit.title",
            "landing.feature.audit.desc",
            "landing.tech.title",
            "landing.tech.subtitle",
            "landing.tech.backend.title",
            "landing.tech.backend.stack",
            "landing.tech.backend.desc",
            "landing.tech.db.title",
            "landing.tech.db.stack",
            "landing.tech.db.desc",
            "landing.tech.security.title",
            "landing.tech.security.stack",
            "landing.tech.security.desc",
            "landing.tech.ui.title",
            "landing.tech.ui.stack",
            "landing.tech.ui.desc",
            "landing.tech.reporting.title",
            "landing.tech.reporting.stack",
            "landing.tech.reporting.desc",
            "landing.tech.visual.title",
            "landing.tech.visual.stack",
            "landing.tech.visual.desc",
            "landing.footer"
    );

    @Test
    @DisplayName("Template file exists and contains hero section")
    void templateContainsHeroSection() {
        String raw = readTemplate();
        assertThat(raw).contains("hero-section");
    }

    @Test
    @DisplayName("Template contains tech-stack section")
    void templateContainsTechStack() {
        String raw = readTemplate();
        assertThat(raw).contains("tech-stack");
    }

    @Test
    @DisplayName("Template contains login link")
    void templateContainsLoginLink() {
        String raw = readTemplate();
        assertThat(raw).contains("/login");
    }

    @Test
    @DisplayName("Template contains hybrid portfolio and ERP business sections")
    void templateContainsHybridLandingSections() {
        String raw = readTemplate();

        assertThat(raw)
                .doesNotContain("hero-kicker")
                .doesNotContain("landing.hero.badge")
                .contains("product-preview")
                .contains("business-flow")
                .contains("product-showcase")
                .contains("engineering-highlights")
                .contains("landingImageModal")
                .contains("image-trigger")
                .contains("/img/landing/purchase-order.png")
                .contains("/img/landing/goods-receipt.png")
                .contains("/img/landing/on-hand-quantity.png")
                .contains("/img/landing/vendor-bill.png")
                .contains("/img/landing/vendor-payment.png")
                .contains("/img/landing/journal-entry.png");
    }

    @Test
    @DisplayName("Landing showcase uses full-size clickable images")
    void landingShowcaseUsesClickableFullSizeImages() {
        String raw = readTemplate();

        assertThat(raw)
                .contains("object-fit: contain")
                .contains("data-landing-image-src")
                .contains("landing.showcase.payment.title")
                .doesNotContain("showcase-duo");
    }

    @Test
    @DisplayName("Landing page i18n keys exist in English and Indonesian bundles")
    void landingMessageBundlesContainRequiredKeys() throws Exception {
        assertLandingKeys(load("messages.properties"));
        assertLandingKeys(load("messages_id.properties"));
    }

    private void assertLandingKeys(Properties props) {
        for (String key : LANDING_KEYS) {
            assertThat(props.getProperty(key)).as("missing landing message key %s", key).isNotBlank();
        }
    }

    private String readTemplate() {
        String resourcePath = "templates/" + TEMPLATE + ".html";
        InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath);
        assertThat(is).as("Template file must exist: %s", resourcePath).isNotNull();
        try (InputStream in = is) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Properties load(String resource) throws Exception {
        InputStream stream = getClass().getClassLoader().getResourceAsStream(resource);
        assertThat(stream).as("Message bundle not found: %s", resource).isNotNull();

        Properties props = new Properties();
        try (InputStream in = stream; InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
            props.load(reader);
        }
        return props;
    }
}
