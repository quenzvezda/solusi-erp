package com.solusi.erp.inventory.productcategory.infrastructure.adapter;

import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.inventory.productcategory.infrastructure.persistence.ProductCategoryEntity;
import com.solusi.erp.inventory.productcategory.infrastructure.persistence.ProductCategoryJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProductCategoryLookupProviderImplTest {

    private final ProductCategoryJpaRepository productCategoryJpaRepository = mock(ProductCategoryJpaRepository.class);
    private final ProductCategoryLookupProviderImpl provider = new ProductCategoryLookupProviderImpl(productCategoryJpaRepository);

    @Test
    @DisplayName("resolve returns LookupDto with name and code as subText")
    void resolve_found_returnsLookupDto() {
        ProductCategoryEntity entity = new ProductCategoryEntity();
        entity.setId(1L);
        entity.setCode("CAT-001");
        entity.setName("Electronics");

        when(productCategoryJpaRepository.findById(1L)).thenReturn(Optional.of(entity));

        LookupDto result = provider.resolve(1L);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("Electronics");
        assertThat(result.subText()).isEqualTo("CAT-001");
    }

    @Test
    @DisplayName("resolve returns null when categoryId is null")
    void resolve_nullId_returnsNull() {
        LookupDto result = provider.resolve(null);
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("resolve returns null when category not found")
    void resolve_notFound_returnsNull() {
        when(productCategoryJpaRepository.findById(999L)).thenReturn(Optional.empty());

        LookupDto result = provider.resolve(999L);
        assertThat(result).isNull();
    }
}
