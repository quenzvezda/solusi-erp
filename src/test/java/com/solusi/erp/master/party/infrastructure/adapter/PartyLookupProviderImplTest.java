package com.solusi.erp.master.party.infrastructure.adapter;

import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.master.party.infrastructure.persistence.Party;
import com.solusi.erp.master.party.infrastructure.persistence.PartyJpaRepository;
import com.solusi.erp.master.shared.model.PartyType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;

import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PartyLookupProviderImplTest {

    private final PartyJpaRepository partyJpaRepository = mock(PartyJpaRepository.class);
    private final MessageSource messageSource = mock(MessageSource.class);
    private final PartyLookupProviderImpl provider = new PartyLookupProviderImpl(partyJpaRepository, messageSource);

    @Test
    @DisplayName("resolve returns LookupDto with correct subText for ORGANIZATION party")
    void resolve_organization_returnsCodeAndTypeLabel() {
        Party party = new Party();
        party.setId(1L);
        party.setCode("PRT-001");
        party.setName("Solusi Program");
        party.setType(PartyType.ORGANIZATION);

        when(partyJpaRepository.findById(1L)).thenReturn(Optional.of(party));
        when(messageSource.getMessage(eq("label.party.type.organization"), any(), any(Locale.class)))
                .thenReturn("Organisasi / Perusahaan");

        LookupDto result = provider.resolve(1L);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("Solusi Program");
        assertThat(result.subText()).isEqualTo("PRT-001 - Organisasi / Perusahaan");
    }

    @Test
    @DisplayName("resolve returns LookupDto with salutation prepended for PERSON party")
    void resolve_person_includesSalutation() {
        Party party = new Party();
        party.setId(2L);
        party.setCode("PRT-002");
        party.setSalutation("Mr.");
        party.setName("John Doe");
        party.setType(PartyType.PERSON);

        when(partyJpaRepository.findById(2L)).thenReturn(Optional.of(party));
        when(messageSource.getMessage(eq("label.party.type.person"), any(), any(Locale.class)))
                .thenReturn("Perorangan");

        LookupDto result = provider.resolve(2L);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(2L);
        assertThat(result.name()).isEqualTo("Mr. John Doe");
        assertThat(result.subText()).isEqualTo("PRT-002 - Perorangan");
    }

    @Test
    @DisplayName("resolve returns null when partyId is null")
    void resolve_nullId_returnsNull() {
        LookupDto result = provider.resolve(null);
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("resolve returns null when party not found")
    void resolve_notFound_returnsNull() {
        when(partyJpaRepository.findById(999L)).thenReturn(Optional.empty());

        LookupDto result = provider.resolve(999L);
        assertThat(result).isNull();
    }
}
