package com.solusi.erp.inventory.product.domain.model;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class ProductTest {

    @Test
    void createNew_ShouldInitializeCorrectly() {
        Product product = Product.createNew(
            "P001", "Test Product", "123456", "Note", 1L, 2L, 3L, "HS001",
            true, false, BigDecimal.ONE, BigDecimal.TEN,
            new BigDecimal("0.5"), new BigDecimal("0.6"), 4L,
            BigDecimal.valueOf(10), BigDecimal.valueOf(20), BigDecimal.valueOf(30), 5L
        );

        assertNotNull(product);
        assertEquals("P001", product.getCode());
        assertEquals("Test Product", product.getName());
        assertEquals("123456", product.getBarcode());
        assertTrue(product.isActive());
        assertFalse(product.isSerialized());
        assertEquals(BigDecimal.ONE, product.getMinStock());
        assertEquals(BigDecimal.valueOf(10), product.getLength());
    }

    @Test
    void updateInfo_ShouldUpdateFields() {
        Product product = Product.createNew("P001", "Old Name", null, null, 1L, 1L, null, null, true, false, null, null, null, null, null, null, null, null, null);
        
        product.updateInfo("New Name", "BAR001", "New Note", 2L, 2L, 2L, "HS002");

        assertEquals("New Name", product.getName());
        assertEquals("BAR001", product.getBarcode());
        assertEquals("New Note", product.getNote());
        assertEquals(2L, product.getCategoryId());
        assertEquals(2L, product.getUomId());
        assertEquals(2L, product.getBrandId());
        assertEquals("HS002", product.getHscode());
    }

    @Test
    void updateStatus_ShouldToggleActive() {
        Product product = Product.createNew("P001", "Name", null, null, 1L, 1L, null, null, true, false, null, null, null, null, null, null, null, null, null);
        
        product.updateStatus(false);
        assertFalse(product.isActive());

        product.updateStatus(true);
        assertTrue(product.isActive());
    }
}
