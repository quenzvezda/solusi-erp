package com.solusi.erp.accounting.journal.web.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class JournalEntrySaveRequest extends BaseAuditResponse {
    @NotNull
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate postingDate;
    private Long currencyId;
    private BigDecimal exchangeRate;
    private String referenceNo;
    private String description;
    @Valid
    private List<JournalLineSaveRequest> lines = new ArrayList<>();
}
