package com.solusi.erp.testutils;

import com.solusi.erp.inventory.brand.web.dto.BrandSummaryResponse;
import com.solusi.erp.inventory.product.web.dto.ProductSummaryResponse;
import com.solusi.erp.master.geographic.web.dto.GeographicSummaryResponse;
import com.solusi.erp.master.shared.model.GeographicType;
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

    public static SchemaSummaryResponse sampleSchemaSummaryResponse() {
        SchemaSummaryResponse dto = new SchemaSummaryResponse();
        dto.setId(1L);
        dto.setEventType("GOODS_RECEIPT");
        dto.setDescription("Goods receipt schema");
        dto.setLines(new java.util.ArrayList<>());
        dto.setIsActive(true);
        return dto;
    }

    public static SchemaSaveRequest sampleSchemaSaveRequest() {
        SchemaSaveRequest req = new SchemaSaveRequest();
        req.setEventType("GOODS_RECEIPT");
        req.setDescription("Goods receipt schema");
        req.setLines(new java.util.ArrayList<>());
        req.setIsActive(true);
        return req;
    }

    public static SchemaDetailResponse sampleSchemaDetailResponse() {
        SchemaDetailResponse dto = new SchemaDetailResponse();
        dto.setId(1L);
        dto.setEventType("GOODS_RECEIPT");
        dto.setDescription("Goods receipt schema");
        dto.setLines(new java.util.ArrayList<>());
        dto.setIsActive(true);
        return dto;
    }

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
}