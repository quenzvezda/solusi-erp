package com.solusi.erp.accountspayable.debitmemoallocation.web.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DebitMemoAllocationSaveRequest extends BaseAuditResponse {

    @NotNull
    private Long debitMemoId;

    private String debitMemoCode;

    @NotNull
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate allocationDate;

    private String notes;

    @Valid
    private List<DebitMemoAllocationLineRequest> lines = new ArrayList<>();
}
