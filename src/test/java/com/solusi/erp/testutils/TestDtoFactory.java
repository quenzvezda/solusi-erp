package com.solusi.erp.testutils;

import com.solusi.erp.inventory.brand.web.dto.BrandSummaryResponse;
import com.solusi.erp.inventory.product.web.dto.ProductSummaryResponse;
import com.solusi.erp.master.geographic.web.dto.GeographicSummaryResponse;
import com.solusi.erp.master.model.GeographicType;

public class TestDtoFactory {

    public static BrandSummaryResponse sampleBrandSummaryResponse() {
        BrandSummaryResponse dto = new BrandSummaryResponse();
        dto.setId(1L);
        dto.setCode("BR1");
        dto.setName("Brand 1");
        dto.setNote("Note 1");
        return dto;
    }

    public static ProductSummaryResponse sampleProductSummaryResponse() {
        ProductSummaryResponse dto = new ProductSummaryResponse();
        dto.setId(1L);
        dto.setCode("PR1");
        dto.setName("Product 1");
        dto.setBarcode("12345");
        dto.setNote("Note 1");
        dto.setCategoryId(1L);
        dto.setCategoryName("Category 1");
        dto.setCategoryType("STOCK");
        dto.setUomCode("PCS");
        dto.setBrandName("Brand 1");
        dto.setIsActive(Boolean.TRUE);
        dto.setIsSerialized(Boolean.FALSE);
        return dto;
    }

    public static GeographicSummaryResponse sampleGeographicSummaryResponse() {
        GeographicSummaryResponse dto = new GeographicSummaryResponse();
        dto.setId(1L);
        dto.setCode("ID");
        dto.setName("Indonesia");
        dto.setType(GeographicType.COUNTRY);
        dto.setIsActive(Boolean.TRUE);
        return dto;
    }
}
