package com.solusi.erp.master.partyroletype.web.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PartyRoleTypeDetailResponse extends BaseAuditResponse {
    private String code;
    private String name;
    private String note;
    private Boolean isActive;
}
