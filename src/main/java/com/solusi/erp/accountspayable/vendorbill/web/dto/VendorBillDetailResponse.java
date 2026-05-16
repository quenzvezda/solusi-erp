package com.solusi.erp.accountspayable.vendorbill.web.dto;

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
public class VendorBillDetailResponse extends BaseAuditResponse {
    private String code;
    private Long vendorId;
    private String vendorInvoiceNumber;
    private LocalDate billDate;
    private LocalDate dueDate;
    private Long currencyId;
    private BigDecimal exchangeRate;
    private String status;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal outstandingAmount;
    private String notes;
    private List<Long> grIds;
    private List<VendorBillLineResponse> lines;
}
