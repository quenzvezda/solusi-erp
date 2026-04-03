package com.solusi.erp.inventory.grid.infrastructure.adapter;

import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.inventory.grid.infrastructure.persistence.GridEntity;
import com.solusi.erp.inventory.grid.infrastructure.persistence.GridJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GridLookupProviderImplTest {

    private final GridJpaRepository gridJpaRepository = mock(GridJpaRepository.class);
    private final GridLookupProviderImpl provider = new GridLookupProviderImpl(gridJpaRepository);

    @Test
    @DisplayName("resolve returns LookupDto with name and code as subText")
    void resolve_found_returnsLookupDto() {
        GridEntity entity = new GridEntity();
        entity.setId(1L);
        entity.setCode("GRD-001");
        entity.setName("Row A");

        when(gridJpaRepository.findById(1L)).thenReturn(Optional.of(entity));

        LookupDto result = provider.resolve(1L);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("Row A");
        assertThat(result.subText()).isEqualTo("GRD-001");
    }

    @Test
    @DisplayName("resolve returns null when gridId is null")
    void resolve_nullId_returnsNull() {
        LookupDto result = provider.resolve(null);
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("resolve returns null when grid not found")
    void resolve_notFound_returnsNull() {
        when(gridJpaRepository.findById(999L)).thenReturn(Optional.empty());

        LookupDto result = provider.resolve(999L);
        assertThat(result).isNull();
    }
}
