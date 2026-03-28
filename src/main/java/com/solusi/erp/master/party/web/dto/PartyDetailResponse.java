package com.solusi.erp.master.party.web.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import com.solusi.erp.master.model.PartyType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PartyDetailResponse extends BaseAuditResponse {
    private String code;
    private String salutation;
    private String name;
    private PartyType type;
    private String notes;
    private Boolean isActive;
    private String email;
    private String phone;
    private Set<Long> roleIds;
    private List<String> roleNames;
    private List<PartyContactRequest> contacts;
    private List<PartyAddressRequest> addresses;
    private List<PartyIdentificationRequest> identifications;
}
