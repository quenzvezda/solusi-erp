package com.solusi.erp.purchasing.purchaseorder.application.service;

import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.core.messaging.domain.model.IntegrationEvent;
import com.solusi.erp.master.currency.domain.port.CurrencyLookupProvider;
import com.solusi.erp.master.party.domain.port.PartyLookupProvider;
import com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrder;
import com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrderApprovedPayload;
import com.solusi.erp.security.user.domain.model.User;
import com.solusi.erp.security.user.domain.model.UserProfile;
import com.solusi.erp.security.user.domain.repository.UserRepository;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class PurchaseOrderApprovedEventFactory {

    private static final String TOPIC = "erp.procurement.events.v1";
    private static final String EVENT_TYPE = "PurchaseOrderApproved";
    private static final int EVENT_VERSION = 1;
    private static final String SOURCE = "erp-monolith";
    private static final String AGGREGATE_TYPE = "PurchaseOrder";

    private final UserRepository userRepository;
    private final PartyLookupProvider partyLookupProvider;
    private final CurrencyLookupProvider currencyLookupProvider;
    private final Clock clock;

    public PurchaseOrderApprovedEventFactory(
            UserRepository userRepository,
            PartyLookupProvider partyLookupProvider,
            CurrencyLookupProvider currencyLookupProvider,
            Clock clock) {
        this.userRepository = Objects.requireNonNull(userRepository, "userRepository must not be null");
        this.partyLookupProvider = Objects.requireNonNull(partyLookupProvider, "partyLookupProvider must not be null");
        this.currencyLookupProvider = Objects.requireNonNull(currencyLookupProvider, "currencyLookupProvider must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    public IntegrationEvent create(PurchaseOrder purchaseOrder, Long approverPartyId) {
        Objects.requireNonNull(purchaseOrder, "purchaseOrder must not be null");
        Optional<User> requester = findRequester(purchaseOrder);
        User requesterUser = requester.orElse(null);
        Long requesterUserId = requesterUser != null ? requesterUser.getId() : purchaseOrder.getMetadata().createdBy();
        Long requesterPartyId = requesterUser != null ? requesterUser.getPartyId() : null;

        PurchaseOrderApprovedPayload payload = new PurchaseOrderApprovedPayload(
                purchaseOrder.getId(),
                purchaseOrder.getCode(),
                requesterUserId,
                requesterPartyId,
                resolveRequesterName(purchaseOrder, requesterUser),
                requesterUser != null ? requesterUser.getEmail() : null,
                approverPartyId,
                resolveApproverName(approverPartyId),
                DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(OffsetDateTime.now(clock)),
                purchaseOrder.getTotalAmount(),
                resolveCurrencyCode(purchaseOrder.getCurrencyId()));

        return new PurchaseOrderApprovedIntegrationEvent(
                String.valueOf(purchaseOrder.getId()),
                payload);
    }

    private Optional<User> findRequester(PurchaseOrder purchaseOrder) {
        Long createdBy = purchaseOrder.getMetadata().createdBy();
        return createdBy == null ? Optional.empty() : userRepository.findById(createdBy);
    }

    private String resolveRequesterName(PurchaseOrder purchaseOrder, User requesterUser) {
        if (requesterUser == null) {
            return "User " + purchaseOrder.getMetadata().createdBy();
        }

        LookupDto party = requesterUser.getPartyId() == null ? null : partyLookupProvider.resolve(requesterUser.getPartyId());
        if (party != null && hasText(party.name())) {
            return party.name();
        }

        UserProfile profile = requesterUser.getProfile();
        if (profile != null && hasText(profile.getFullName())) {
            return profile.getFullName();
        }

        return requesterUser.getUsername();
    }

    private String resolveApproverName(Long approverPartyId) {
        LookupDto approver = approverPartyId == null ? null : partyLookupProvider.resolve(approverPartyId);
        if (approver != null && hasText(approver.name())) {
            return approver.name();
        }
        return approverPartyId != null ? "Party " + approverPartyId : "Approver";
    }

    private String resolveCurrencyCode(Long currencyId) {
        LookupDto currency = currencyId == null ? null : currencyLookupProvider.resolve(currencyId);
        if (currency == null) {
            return null;
        }
        Map<String, Object> payload = currency.payload();
        Object alias = payload != null ? payload.get("alias") : null;
        if (alias instanceof String value && hasText(value)) {
            return value;
        }
        if (hasText(currency.subText())) {
            int separator = currency.subText().lastIndexOf('-');
            return separator >= 0 ? currency.subText().substring(separator + 1).trim() : currency.subText();
        }
        return currency.name();
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private record PurchaseOrderApprovedIntegrationEvent(
            String aggregateId,
            PurchaseOrderApprovedPayload payload) implements IntegrationEvent {

        @Override
        public String topic() {
            return TOPIC;
        }

        @Override
        public String messageKey() {
            return aggregateId;
        }

        @Override
        public String eventType() {
            return EVENT_TYPE;
        }

        @Override
        public int eventVersion() {
            return EVENT_VERSION;
        }

        @Override
        public String source() {
            return SOURCE;
        }

        @Override
        public String correlationId() {
            return aggregateId;
        }

        @Override
        public String aggregateType() {
            return AGGREGATE_TYPE;
        }
    }
}
