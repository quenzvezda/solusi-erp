package com.solusi.erp.security.user.application.port;

import java.util.Optional;

public interface PartyReferenceGateway {

    Optional<PartyReferenceData> findById(Long partyId);

    record PartyReferenceData(Long id, String code, String name) {}
}
