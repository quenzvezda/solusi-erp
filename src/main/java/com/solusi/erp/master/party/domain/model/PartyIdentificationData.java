package com.solusi.erp.master.party.domain.model;

import java.time.LocalDate;

public record PartyIdentificationData(
        Long id,
        Long typeId,
        String idNumber,
        LocalDate issuedDate,
        LocalDate expiryDate,
        Boolean isActive,
        Boolean isDefault
) {}
