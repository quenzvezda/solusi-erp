package com.solusi.erp.master.party.infrastructure.adapter;

import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.master.party.domain.port.PartyLookupProvider;
import com.solusi.erp.master.party.infrastructure.persistence.Party;
import com.solusi.erp.master.party.infrastructure.persistence.PartyJpaRepository;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.util.StringUtils;

/**
 * Infrastructure adapter that resolves Party into LookupDto with consistent subText.
 * SubText format: "{code} - {typeLabel}" (e.g. "PRT-001 - Organisasi / Perusahaan").
 * This is the single source of truth — all consumers get the same representation.
 */
public class PartyLookupProviderImpl implements PartyLookupProvider {

    private final PartyJpaRepository partyJpaRepository;
    private final MessageSource messageSource;

    public PartyLookupProviderImpl(PartyJpaRepository partyJpaRepository, MessageSource messageSource) {
        this.partyJpaRepository = partyJpaRepository;
        this.messageSource = messageSource;
    }

    @Override
    public LookupDto resolve(Long partyId) {
        if (partyId == null) return null;
        return partyJpaRepository.findById(partyId)
                .map(this::toLookupDto)
                .orElse(null);
    }

    private LookupDto toLookupDto(Party party) {
        String fullName = (StringUtils.hasText(party.getSalutation())
                ? party.getSalutation() + " " : "") + party.getName();
        String typeLabel = messageSource.getMessage(
                "label.party.type." + party.getType().name().toLowerCase(),
                null, LocaleContextHolder.getLocale());
        String subText = party.getCode() + " - " + typeLabel;
        return new LookupDto(party.getId(), fullName, subText);
    }
}
