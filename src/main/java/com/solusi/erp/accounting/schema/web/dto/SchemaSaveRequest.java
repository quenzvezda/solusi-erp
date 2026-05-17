package com.solusi.erp.accounting.schema.web.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SchemaSaveRequest extends BaseAuditResponse {

    @NotBlank(message = "{label.schema.event.type} {validation.notblank.suffix}")
    private String eventType;

    private String description;

    @NotNull(message = "{label.schema.column.status} {validation.notnull.suffix}")
    private Boolean isActive;

    @NotEmpty(message = "{validation.notempty.suffix}")
    private List<SchemaLineRequest> lines = new ArrayList<>();

    @Data
    public static class SchemaLineRequest {
        private Long id;
        @NotNull private String variable;
        @NotNull private Long accountId;
        private String accountCode;
        private String accountName;
        @NotNull private String position;
    }
}