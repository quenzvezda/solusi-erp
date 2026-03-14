package com.solusi.erp.master.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PartyContactResponse extends BaseAuditResponse {
    private String label;
    private String mobile;
    private String phone;
    private String email;
    private Boolean isActive;
    private Boolean isDefault;
}

