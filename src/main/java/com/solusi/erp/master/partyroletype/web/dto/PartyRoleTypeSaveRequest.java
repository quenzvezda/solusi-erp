package com.solusi.erp.master.partyroletype.web.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PartyRoleTypeSaveRequest extends BaseAuditResponse {

    @Size(max = 50, message = "{label.party-role-type.code} {validation.size.suffix}")
    private String code;

    @NotBlank(message = "{label.party-role-type.name} {validation.notblank.suffix}")
    @Size(max = 100, message = "{label.party-role-type.name} {validation.size.suffix}")
    private String name;

    private String note;
    private Boolean isActive;
}
