package com.solusi.erp.inventory.uomconversion.domain;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.inventory.uomconversion.domain.model.UomConversion;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class UomConversionDomainTest {

    @Test
    void createNew_shouldSetAllFields() {
        UomConversion conv = UomConversion.createNew(
            1L, "PRD-001", "Product 1",
            2L, "Box",
            3L, "Pcs",
            new BigDecimal("12.00")
        );

        assertThat(conv.getProductId()).isEqualTo(1L);
        assertThat(conv.getProductCode()).isEqualTo("PRD-001");
        assertThat(conv.getProductName()).isEqualTo("Product 1");
        assertThat(conv.getFromUomId()).isEqualTo(2L);
        assertThat(conv.getFromUomName()).isEqualTo("Box");
        assertThat(conv.getToUomId()).isEqualTo(3L);
        assertThat(conv.getToUomName()).isEqualTo("Pcs");
        assertThat(conv.getConversionFactor()).isEqualByComparingTo(new BigDecimal("12.00"));
        assertThat(conv.getId()).isNull();
        assertThat(conv.getMetadata().id()).isNull();
    }

    @Test
    void createNew_shouldScaleFactorTo2Decimals() {
        UomConversion conv = UomConversion.createNew(
            1L, "P", "P", 2L, "Box", 3L, "Pcs", new BigDecimal("12.555")
        );
        assertThat(conv.getConversionFactor().scale()).isEqualTo(2);
        assertThat(conv.getConversionFactor()).isEqualByComparingTo(
            new BigDecimal("12.555").setScale(2, RoundingMode.HALF_UP));
    }

    @Test
    void createNew_withZeroFactor_shouldThrow() {
        assertThatThrownBy(() -> UomConversion.createNew(
            1L, "P", "P", 2L, "Box", 3L, "Pcs", BigDecimal.ZERO))
            .isInstanceOf(DomainException.class)
            .hasMessageContaining("invalid-factor");
    }

    @Test
    void createNew_withNegativeFactor_shouldThrow() {
        assertThatThrownBy(() -> UomConversion.createNew(
            1L, "P", "P", 2L, "Box", 3L, "Pcs", new BigDecimal("-1")))
            .isInstanceOf(DomainException.class);
    }

    @Test
    void createNew_withNullFactor_shouldThrow() {
        assertThatThrownBy(() -> UomConversion.createNew(
            1L, "P", "P", 2L, "Box", 3L, "Pcs", null))
            .isInstanceOf(DomainException.class);
    }

    @Test
    void update_shouldChangeFromUomAndFactor() {
        UomConversion conv = UomConversion.createNew(
            1L, "P", "P", 2L, "Box", 3L, "Pcs", new BigDecimal("12.00"));

        conv.update(4L, "Carton", new BigDecimal("24.00"));

        assertThat(conv.getFromUomId()).isEqualTo(4L);
        assertThat(conv.getFromUomName()).isEqualTo("Carton");
        assertThat(conv.getConversionFactor()).isEqualByComparingTo(new BigDecimal("24.00"));
    }

    @Test
    void update_withInvalidFactor_shouldThrow() {
        UomConversion conv = UomConversion.createNew(
            1L, "P", "P", 2L, "Box", 3L, "Pcs", new BigDecimal("12.00"));

        assertThatThrownBy(() -> conv.update(4L, "Carton", BigDecimal.ZERO))
            .isInstanceOf(DomainException.class);
    }

    @Test
    void reconstructFromPersistence_shouldPreserveId() {
        AuditMetadata metadata = new AuditMetadata(10L, 1L, null, null, null, null);
        UomConversion conv = new UomConversion(
            metadata, 1L, "P", "Product",
            2L, "Box", 3L, "Pcs", new BigDecimal("12.00"));

        assertThat(conv.getId()).isEqualTo(10L);
        assertThat(conv.getMetadata().version()).isEqualTo(1L);
    }
}
