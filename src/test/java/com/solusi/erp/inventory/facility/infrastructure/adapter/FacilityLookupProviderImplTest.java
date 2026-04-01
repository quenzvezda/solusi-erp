package com.solusi.erp.inventory.facility.infrastructure.adapter;

import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.inventory.facility.infrastructure.persistence.FacilityEntity;
import com.solusi.erp.inventory.facility.infrastructure.persistence.FacilityJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FacilityLookupProviderImplTest {

    private final FacilityJpaRepository facilityJpaRepository = mock(FacilityJpaRepository.class);
    private final FacilityLookupProviderImpl provider = new FacilityLookupProviderImpl(facilityJpaRepository);

    @Test
    @DisplayName("resolve returns LookupDto with name and code as subText")
    void resolve_found_returnsLookupDto() {
        FacilityEntity entity = new FacilityEntity();
        entity.setId(1L);
        entity.setCode("FAC-001");
        entity.setName("Warehouse A");

        when(facilityJpaRepository.findById(1L)).thenReturn(Optional.of(entity));

        LookupDto result = provider.resolve(1L);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("Warehouse A");
        assertThat(result.subText()).isEqualTo("FAC-001");
    }

    @Test
    @DisplayName("resolve returns null when facilityId is null")
    void resolve_nullId_returnsNull() {
        LookupDto result = provider.resolve(null);
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("resolve returns null when facility not found")
    void resolve_notFound_returnsNull() {
        when(facilityJpaRepository.findById(999L)).thenReturn(Optional.empty());

        LookupDto result = provider.resolve(999L);
        assertThat(result).isNull();
    }
}
