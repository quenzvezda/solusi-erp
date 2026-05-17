package com.solusi.erp.accountspayable.vendorpayment.web.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class VendorPaymentSaveRequest extends BaseAuditResponse {

    @NotNull
    private Long vendorId;

    @NotNull
    private Long currencyId;

    @NotNull
    private Long bankAccountId;

    @NotNull
    private LocalDate paymentDate;

    @NotNull
    private BigDecimal exchangeRate;

    @NotNull
    private BigDecimal paymentAmount;

    private String reference;
    private String notes;

    @Valid
    private List<VendorPaymentLineRequest> lines;
}
