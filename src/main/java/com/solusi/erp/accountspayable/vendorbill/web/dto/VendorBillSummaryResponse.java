package com.solusi.erp.accountspayable.vendorbill.web.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class VendorBillSummaryResponse extends BaseAuditResponse {
    private String code;
    private Long vendorId;
    private String vendorInvoiceNumber;
    private LocalDate billDate;
    private LocalDate dueDate;
    private String status;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal outstandingAmount;
}
