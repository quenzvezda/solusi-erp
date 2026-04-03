package com.solusi.erp.inventory.brand.infrastructure.adapter;

import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.inventory.brand.infrastructure.persistence.BrandEntity;
import com.solusi.erp.inventory.brand.infrastructure.persistence.BrandJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BrandLookupProviderImplTest {

    private final BrandJpaRepository brandJpaRepository = mock(BrandJpaRepository.class);
    private final BrandLookupProviderImpl provider = new BrandLookupProviderImpl(brandJpaRepository);

    @Test
    @DisplayName("resolve returns LookupDto with name and code as subText")
    void resolve_found_returnsLookupDto() {
        BrandEntity entity = new BrandEntity();
        entity.setId(1L);
        entity.setCode("BRD-001");
        entity.setName("Acme Corp");

        when(brandJpaRepository.findById(1L)).thenReturn(Optional.of(entity));

        LookupDto result = provider.resolve(1L);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("Acme Corp");
        assertThat(result.subText()).isEqualTo("BRD-001");
    }

    @Test
    @DisplayName("resolve returns null when brandId is null")
    void resolve_nullId_returnsNull() {
        LookupDto result = provider.resolve(null);
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("resolve returns null when brand not found")
    void resolve_notFound_returnsNull() {
        when(brandJpaRepository.findById(999L)).thenReturn(Optional.empty());

        LookupDto result = provider.resolve(999L);
        assertThat(result).isNull();
    }
}
