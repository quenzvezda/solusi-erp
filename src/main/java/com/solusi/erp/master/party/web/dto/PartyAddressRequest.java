package com.solusi.erp.master.party.web.dto;

import com.solusi.erp.master.shared.model.AddressType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PartyAddressRequest {
    private Long id;
    private Set<AddressType> types = new HashSet<>();
    private String addressLine1;
    private Long cityId;
    private String postalCode;
    private Boolean isActive = true;
    private Boolean isDefault = false;
}

