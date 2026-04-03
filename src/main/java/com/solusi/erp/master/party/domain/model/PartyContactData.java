package com.solusi.erp.master.party.domain.model;

public record PartyContactData(
        Long id,
        String label,
        String mobile,
        String phone,
        String email,
        Boolean isActive,
        Boolean isDefault
) {}
