package com.solusi.erp.accountspayable.vendorbill.web.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class VendorBillSaveRequest extends BaseAuditResponse {
    @NotNull
    private Long vendorId;
    @NotBlank
    private String vendorInvoiceNumber;
    @NotNull
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate billDate;
    @NotNull
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueDate;
    @NotNull
    private Long currencyId;
    @NotNull
    private BigDecimal exchangeRate = BigDecimal.ONE;
    private String notes;
    private List<Long> grIds = new ArrayList<>();
    @Valid
    private List<VendorBillLineRequest> lines = new ArrayList<>();
}
