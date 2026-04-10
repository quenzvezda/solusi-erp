package com.solusi.erp.accounting.coa.web.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CoaDetailResponse extends BaseAuditResponse {
    private String code;
    private String name;
    private String accountType;
    private String normalBalance;
    private Long parentId;
    private String parentName;
    private Integer level;
    private Boolean isHeader;
    private String note;
    private Boolean isActive;
}
