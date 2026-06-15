package com.solusi.erp.purchasing.purchaseorder.application.service;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.core.messaging.domain.model.IntegrationEvent;
import com.solusi.erp.master.currency.domain.port.CurrencyLookupProvider;
import com.solusi.erp.master.party.domain.port.PartyLookupProvider;
import com.solusi.erp.master.tax.domain.model.TaxCalculationMode;
import com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrder;
import com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrderApprovedPayload;
import com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrderStatus;
import com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrderType;
import com.solusi.erp.security.user.domain.model.User;
import com.solusi.erp.security.user.domain.model.UserProfile;
import com.solusi.erp.security.user.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PurchaseOrderApprovedEventFactoryTest {

    private UserRepository userRepository;
    private PartyLookupProvider partyLookupProvider;
    private CurrencyLookupProvider currencyLookupProvider;
    private PurchaseOrderApprovedEventFactory factory;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        partyLookupProvider = mock(PartyLookupProvider.class);
        currencyLookupProvider = mock(CurrencyLookupProvider.class);
        factory = new PurchaseOrderApprovedEventFactory(
                userRepository,
                partyLookupProvider,
                currencyLookupProvider,
                Clock.fixed(Instant.parse("2026-06-15T03:30:00Z"), ZoneOffset.UTC));

        when(currencyLookupProvider.resolve(1L))
                .thenReturn(new LookupDto(1L, "Rupiah", "Rp - IDR", Map.of("alias", "IDR")));
    }

    @Test
    void requesterPartyNameShouldWinOverUserProfile() {
        when(userRepository.findById(7L)).thenReturn(Optional.of(user(
                7L,
                "requester",
                "requester@example.com",
                10L,
                UserProfile.createDefault("Profile Name", null))));
        when(partyLookupProvider.resolve(10L)).thenReturn(new LookupDto(10L, "Party Requester", "REQ"));
        when(partyLookupProvider.resolve(99L)).thenReturn(new LookupDto(99L, "Approver Party", "APR"));

        IntegrationEvent event = factory.create(po(), 99L);

        PurchaseOrderApprovedPayload payload = (PurchaseOrderApprovedPayload) event.payload();
        assertThat(payload.requesterName()).isEqualTo("Party Requester");
        assertThat(payload.requesterEmail()).isEqualTo("requester@example.com");
        assertThat(payload.requesterUserId()).isEqualTo(7L);
        assertThat(payload.requesterPartyId()).isEqualTo(10L);
        assertThat(payload.approverName()).isEqualTo("Approver Party");
        assertThat(payload.currencyCode()).isEqualTo("IDR");
        assertThat(payload.approvedAt()).isEqualTo("2026-06-15T03:30:00Z");
    }

    @Test
    void requesterProfileFullNameShouldBeUsedWhenPartyMissing() {
        when(userRepository.findById(7L)).thenReturn(Optional.of(user(
                7L,
                "requester",
                "requester@example.com",
                10L,
                UserProfile.createDefault("Profile Name", null))));

        IntegrationEvent event = factory.create(po(), 99L);

        PurchaseOrderApprovedPayload payload = (PurchaseOrderApprovedPayload) event.payload();
        assertThat(payload.requesterName()).isEqualTo("Profile Name");
    }

    @Test
    void requesterUsernameShouldBeUsedWhenProfileMissing() {
        when(userRepository.findById(7L)).thenReturn(Optional.of(user(
                7L,
                "requester",
                "requester@example.com",
                null,
                null)));

        IntegrationEvent event = factory.create(po(), 99L);

        PurchaseOrderApprovedPayload payload = (PurchaseOrderApprovedPayload) event.payload();
        assertThat(payload.requesterName()).isEqualTo("requester");
    }

    @Test
    void nullRequesterEmailShouldStillProduceEvent() {
        when(userRepository.findById(7L)).thenReturn(Optional.of(user(
                7L,
                "requester",
                null,
                null,
                null)));

        IntegrationEvent event = factory.create(po(), 99L);

        PurchaseOrderApprovedPayload payload = (PurchaseOrderApprovedPayload) event.payload();
        assertThat(payload.requesterEmail()).isNull();
        assertThat(event.eventType()).isEqualTo("PurchaseOrderApproved");
        assertThat(event.eventVersion()).isEqualTo(1);
        assertThat(event.topic()).isEqualTo("erp.procurement.events.v1");
        assertThat(event.messageKey()).isEqualTo("42");
        assertThat(event.aggregateType()).isEqualTo("PurchaseOrder");
        assertThat(event.aggregateId()).isEqualTo("42");
    }

    @Test
    void approverShouldFallbackToPartyIdWhenLookupMissing() {
        when(userRepository.findById(7L)).thenReturn(Optional.of(user(
                7L,
                "requester",
                "requester@example.com",
                null,
                null)));

        IntegrationEvent event = factory.create(po(), 99L);

        PurchaseOrderApprovedPayload payload = (PurchaseOrderApprovedPayload) event.payload();
        assertThat(payload.approverPartyId()).isEqualTo(99L);
        assertThat(payload.approverName()).isEqualTo("Party 99");
    }

    private static PurchaseOrder po() {
        return PurchaseOrder.rehydrate(
                new AuditMetadata(
                        42L,
                        1L,
                        LocalDateTime.of(2026, 6, 14, 9, 0),
                        7L,
                        null,
                        null),
                "PO-2606-00001",
                LocalDate.of(2026, 6, 14),
                null,
                100L,
                null,
                1L,
                BigDecimal.ONE,
                new BigDecimal("14000000.00"),
                new BigDecimal("1000000.00"),
                new BigDecimal("15000000.00"),
                PurchaseOrderStatus.SUBMITTED,
                30,
                null,
                PurchaseOrderType.DIRECT,
                1L,
                "PPN",
                "PPN",
                new BigDecimal("11.00"),
                TaxCalculationMode.EXCLUSIVE,
                null,
                true,
                List.of());
    }

    private static User user(Long id, String username, String email, Long partyId, UserProfile profile) {
        return new User(
                new AuditMetadata(id, 1L, null, null, null, null),
                username,
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
                profile);
    }
}
