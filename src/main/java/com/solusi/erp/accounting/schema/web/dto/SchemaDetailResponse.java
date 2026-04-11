package com.solusi.erp.accounting.schema.web.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SchemaDetailResponse extends BaseAuditResponse {
    private String eventType;
    private String description;
    private Long debitAccountId;
    private String debitAccountName;
    private Long creditAccountId;
    private String creditAccountName;
    private Boolean isActive;
}
