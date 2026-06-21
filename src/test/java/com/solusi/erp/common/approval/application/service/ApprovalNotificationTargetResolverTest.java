package com.solusi.erp.common.approval.application.service;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.master.party.domain.port.PartyLookupProvider;
import com.solusi.erp.security.user.domain.model.User;
import com.solusi.erp.security.user.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ApprovalNotificationTargetResolverTest {

    private UserRepository userRepository;
    private PartyLookupProvider partyLookupProvider;
    private ApprovalNotificationTargetResolver resolver;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        partyLookupProvider = mock(PartyLookupProvider.class);
        resolver = new ApprovalNotificationTargetResolver(userRepository, partyLookupProvider);
    }

    @Test
    void partyLookupNameShouldWinAndUserEmailShouldBeUsed() {
        when(partyLookupProvider.resolve(10L)).thenReturn(new LookupDto(10L, "Bpk. Requester", "REQ"));
        when(userRepository.findByPartyId(10L)).thenReturn(Optional.of(user(7L, "requester@example.com", 10L)));

        ApprovalNotificationTarget target = resolver.resolve("REQUESTER", 10L);

        assertThat(target.role()).isEqualTo("REQUESTER");
        assertThat(target.partyId()).isEqualTo(10L);
        assertThat(target.name()).isEqualTo("Bpk. Requester");
        assertThat(target.email()).isEqualTo("requester@example.com");
    }

    @Test
    void missingEmailShouldReturnNullWithoutThrowing() {
        when(partyLookupProvider.resolve(11L)).thenReturn(new LookupDto(11L, "Bpk. Approver", "APR"));
        when(userRepository.findByPartyId(11L)).thenReturn(Optional.empty());

        ApprovalNotificationTarget target = resolver.resolve("TARGET_APPROVER", 11L);

        assertThat(target.name()).isEqualTo("Bpk. Approver");
        assertThat(target.email()).isNull();
    }

    @Test
    void missingPartyLookupShouldFallbackToPartyIdName() {
        when(partyLookupProvider.resolve(12L)).thenReturn(null);
        when(userRepository.findByPartyId(12L)).thenReturn(Optional.of(user(8L, "party12@example.com", 12L)));

        ApprovalNotificationTarget target = resolver.resolve("CURRENT_APPROVER", 12L);

        assertThat(target.name()).isEqualTo("Party 12");
        assertThat(target.email()).isEqualTo("party12@example.com");
    }

    @Test
    void nullPartyIdShouldReturnNullNameAndEmail() {
        ApprovalNotificationTarget target = resolver.resolve("REQUESTER", null);

        assertThat(target.role()).isEqualTo("REQUESTER");
        assertThat(target.partyId()).isNull();
        assertThat(target.name()).isNull();
        assertThat(target.email()).isNull();
        verifyNoInteractions(userRepository, partyLookupProvider);
    }

    private static User user(Long id, String email, Long partyId) {
        return new User(
                new AuditMetadata(id, 1L, null, null, null, null),
                "user" + id,
                "password",
                email,
                true,
                false,
                null,
                1L,
                "ROLE_USER",
                "User",
                partyId,
                null,
                null,
                null);
    }
}
