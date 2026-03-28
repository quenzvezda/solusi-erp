package com.solusi.erp.master.party.domain.model;

import com.solusi.erp.master.model.AddressType;
import java.util.Set;

public record PartyAddressData(
        Long id,
        Set<AddressType> types,
        String addressLine1,
        Long cityId,
        String postalCode,
        Boolean isActive,
        Boolean isDefault
) {}
