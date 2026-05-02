package com.solusi.erp.accounting.schema.web.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SchemaSaveRequest extends BaseAuditResponse {

    @NotNull(message = "{label.schema.event.type} {validation.notnull.suffix}")
    private String eventType;

    private String description;

    @NotNull(message = "{label.schema.debit.account} {validation.notnull.suffix}")
    private Long debitAccountId;
    private String debitAccountName;

    @NotNull(message = "{label.schema.credit.account} {validation.notnull.suffix}")
    private Long creditAccountId;
    private String creditAccountName;

    private Long taxAccountId;
    private String taxAccountName;

    private Boolean isActive;
}
