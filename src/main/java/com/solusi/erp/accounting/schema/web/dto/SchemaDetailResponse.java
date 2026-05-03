package com.solusi.erp.accounting.schema.web.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class SchemaDetailResponse extends BaseAuditResponse {
    private String eventType;
    private String description;
    private Boolean isActive;
    private List<SchemaSaveRequest.SchemaLineRequest> lines;
}