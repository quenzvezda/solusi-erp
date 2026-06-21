package com.solusi.erp.common.approval.application.service;

import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.master.party.domain.port.PartyLookupProvider;
import com.solusi.erp.security.user.domain.repository.UserRepository;

import java.util.Objects;

public class ApprovalNotificationTargetResolver {

    private final UserRepository userRepository;
    private final PartyLookupProvider partyLookupProvider;

    public ApprovalNotificationTargetResolver(
            UserRepository userRepository,
            PartyLookupProvider partyLookupProvider) {
        this.userRepository = Objects.requireNonNull(userRepository, "userRepository must not be null");
        this.partyLookupProvider = Objects.requireNonNull(partyLookupProvider, "partyLookupProvider must not be null");
    }

    public ApprovalNotificationTarget resolve(String role, Long partyId) {
        if (partyId == null) {
            return new ApprovalNotificationTarget(role, null, null, null);
        }

        return new ApprovalNotificationTarget(
                role,
                partyId,
                resolveName(partyId),
                resolveEmail(partyId));
    }

    private String resolveName(Long partyId) {
        LookupDto party = partyLookupProvider.resolve(partyId);
        if (party != null && hasText(party.name())) {
            return party.name();
        }
        return "Party " + partyId;
    }

    private String resolveEmail(Long partyId) {
        return userRepository.findByPartyId(partyId)
                .map(user -> hasText(user.getEmail()) ? user.getEmail() : null)
                .orElse(null);
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
