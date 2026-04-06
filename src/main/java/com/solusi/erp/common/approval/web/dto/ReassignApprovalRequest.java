package com.solusi.erp.common.approval.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReassignApprovalRequest {

    @NotNull(message = "{label.approval.assigned-to} {validation.notnull.suffix}")
    private Long targetApproverId;

    @NotBlank(message = "{label.approval.notes} {validation.notnull.suffix}")
    private String notes;
}
