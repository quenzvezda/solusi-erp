package com.solusi.erp.master.geographic.infrastructure.adapter;

import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.master.geographic.infrastructure.persistence.Geographic;
import com.solusi.erp.master.geographic.infrastructure.persistence.GeographicJpaRepository;
import com.solusi.erp.master.shared.model.GeographicType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GeographicLookupProviderImplTest {

    private final GeographicJpaRepository geographicJpaRepository = mock(GeographicJpaRepository.class);
    private final GeographicLookupProviderImpl provider = new GeographicLookupProviderImpl(geographicJpaRepository);

    @Test
    @DisplayName("resolve returns LookupDto with name and code as subText")
    void resolve_found_returnsLookupDto() {
        Geographic geo = new Geographic();
        geo.setId(1L);
        geo.setCode("JKT");
        geo.setName("Jakarta");
        geo.setType(GeographicType.CITY_MUNICIPALITY);
        geo.setIsActive(true);

        when(geographicJpaRepository.findById(1L)).thenReturn(Optional.of(geo));

        LookupDto result = provider.resolve(1L);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("Jakarta");
        assertThat(result.subText()).isEqualTo("JKT");
    }

    @Test
    @DisplayName("resolve returns null when geographicId is null")
    void resolve_nullId_returnsNull() {
        LookupDto result = provider.resolve(null);
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("resolve returns null when geographic not found")
    void resolve_notFound_returnsNull() {
        when(geographicJpaRepository.findById(999L)).thenReturn(Optional.empty());

        LookupDto result = provider.resolve(999L);
        assertThat(result).isNull();
    }
}
