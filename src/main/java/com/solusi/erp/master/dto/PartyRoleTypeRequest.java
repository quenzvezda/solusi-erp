package com.solusi.erp.master.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * DTO for creating or updating a Party Role Type.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PartyRoleTypeRequest extends BaseAuditResponse {
    @Size(max = 50, message = "{label.party-role-type.code} {validation.size.suffix}")
    private String code;

    @NotBlank(message = "{label.party-role-type.name} {validation.notblank.suffix}")
    @Size(max = 100, message = "{label.party-role-type.name} {validation.size.suffix}")
    private String name;
}
