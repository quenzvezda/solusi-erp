package com.solusi.erp.accounting.coa.web.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CoaSaveRequest extends BaseAuditResponse {

    @NotBlank(message = "{label.coa.code} {validation.notblank.suffix}")
    @Size(max = 20, message = "{label.coa.code} {validation.size.suffix}")
    private String code;

    @NotBlank(message = "{label.coa.name} {validation.notblank.suffix}")
    @Size(max = 150, message = "{label.coa.name} {validation.size.suffix}")
    private String name;

    @NotNull(message = "{label.coa.account.type} {validation.notnull.suffix}")
    private String accountType;

    private Long parentId;
    private String parentName;
    private String parentCode;
    private Integer level;
    private Boolean isHeader;
    private String note;
    private Boolean isActive;
}
