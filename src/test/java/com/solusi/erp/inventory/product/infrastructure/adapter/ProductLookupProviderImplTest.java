package com.solusi.erp.inventory.product.infrastructure.adapter;

import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.inventory.product.infrastructure.persistence.JpaProductRepository;
import com.solusi.erp.inventory.product.infrastructure.persistence.ProductEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProductLookupProviderImplTest {

    private final JpaProductRepository jpaProductRepository = mock(JpaProductRepository.class);
    private final ProductLookupProviderImpl provider = new ProductLookupProviderImpl(jpaProductRepository);

    @Test
    @DisplayName("resolve returns LookupDto with name and code as subText")
    void resolve_found_returnsLookupDto() {
        ProductEntity entity = new ProductEntity();
        entity.setId(1L);
        entity.setCode("PRD-001");
        entity.setName("Widget A");

        when(jpaProductRepository.findById(1L)).thenReturn(Optional.of(entity));

        LookupDto result = provider.resolve(1L);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("Widget A");
        assertThat(result.subText()).isEqualTo("PRD-001");
    }

    @Test
    @DisplayName("resolve returns null when productId is null")
    void resolve_nullId_returnsNull() {
        LookupDto result = provider.resolve(null);
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("resolve returns null when product not found")
    void resolve_notFound_returnsNull() {
        when(jpaProductRepository.findById(999L)).thenReturn(Optional.empty());

        LookupDto result = provider.resolve(999L);
        assertThat(result).isNull();
    }
}
