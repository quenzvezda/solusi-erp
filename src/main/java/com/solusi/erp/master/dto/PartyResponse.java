package com.solusi.erp.master.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import com.solusi.erp.master.model.PartyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PartyResponse extends BaseAuditResponse {
    private String salutation;
    private String code;
    private String name;
    private PartyType type;
    private String notes;
    private Boolean isActive;
    private String email;
    private String phone;

    private Set<String> roleCodes;
    private Set<String> roleNames;

    private List<PartyIdentificationResponse> identifications;
    private List<PartyAddressResponse> addresses;
    private List<PartyContactResponse> contacts;
}

