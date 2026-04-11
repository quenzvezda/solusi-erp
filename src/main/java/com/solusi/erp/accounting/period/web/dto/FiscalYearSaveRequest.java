package com.solusi.erp.accounting.period.web.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FiscalYearSaveRequest extends BaseAuditResponse {

    @NotBlank(message = "{label.period.fy.name} {validation.notblank.suffix}")
    @Size(max = 100, message = "{label.period.fy.name} {validation.size.suffix}")
    private String name;

    @NotNull(message = "{label.period.fy.start} {validation.notnull.suffix}")
    private LocalDate startDate;

    @NotNull(message = "{label.period.fy.end} {validation.notnull.suffix}")
    private LocalDate endDate;

    private Boolean isActive;
}
