package com.solusi.erp.inventory.goodsreceipt.web.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class GoodsReceiptDetailResponse extends BaseAuditResponse {
    private String code;
    private LocalDate receiptDate;
    private String referenceType;
    private Long referenceId;
    private String referenceCode;
    private Long supplierId;
    private String supplierName;
    private Long facilityId;
    private String facilityName;
    private Long currencyId;
    private String currencyCode;
    private BigDecimal exchangeRate;
    private String status;
    private String notes;
    private List<GoodsReceiptLineDetailResponse> lines;
}
