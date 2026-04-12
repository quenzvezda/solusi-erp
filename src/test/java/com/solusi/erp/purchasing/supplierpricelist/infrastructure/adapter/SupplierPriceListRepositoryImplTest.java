package com.solusi.erp.purchasing.supplierpricelist.infrastructure.adapter;

import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.purchasing.supplierpricelist.infrastructure.persistence.SupplierPriceListEntity;
import com.solusi.erp.purchasing.supplierpricelist.infrastructure.persistence.SupplierPriceListJpaRepository;
import com.solusi.erp.purchasing.supplierpricelist.infrastructure.persistence.SupplierPriceListPersistenceMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SupplierPriceListRepositoryImpl tests")
class SupplierPriceListRepositoryImplTest {

    @Mock
    private SupplierPriceListJpaRepository jpaRepository;

    @Mock
    private SupplierPriceListPersistenceMapper mapper;

    private SupplierPriceListRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        repository = new SupplierPriceListRepositoryImpl(jpaRepository, mapper);
    }

    @Test
    @DisplayName("findAll uses search with trimmed keyword")
    void findAll_usesSearchWithTrimmedKeyword() {
        when(jpaRepository.search(eq("supplier"), any(org.springframework.data.domain.Pageable.class)))
            .thenReturn(new PageImpl<>(List.of()));

        repository.findAll("  supplier  ", Pageable.of(0, 20));

        verify(jpaRepository).search(eq("supplier"), any(org.springframework.data.domain.Pageable.class));
        verify(jpaRepository, never()).findAll(any(org.springframework.data.domain.Pageable.class));
    }

    @Test
    @DisplayName("findAll uses plain findAll when keyword is blank")
    void findAll_usesFindAllWhenKeywordBlank() {
        when(jpaRepository.findAll(any(org.springframework.data.domain.Pageable.class)))
            .thenReturn(new PageImpl<>(List.of()));

        repository.findAll("   ", Pageable.of(0, 20));

        verify(jpaRepository).findAll(any(org.springframework.data.domain.Pageable.class));
        verify(jpaRepository, never()).search(any(), any(org.springframework.data.domain.Pageable.class));
    }

    @Test
    @DisplayName("search normalizes null keyword to empty string")
    void search_normalizesNullKeywordToEmpty() {
        when(jpaRepository.search(eq(""), eq(PageRequest.of(0, 10))))
            .thenReturn(new PageImpl<SupplierPriceListEntity>(List.of()));

        repository.search(null, 10);

        verify(jpaRepository).search(eq(""), eq(PageRequest.of(0, 10)));
    }

    @Test
    @DisplayName("search trims keyword before querying")
    void search_trimsKeywordBeforeQuerying() {
        when(jpaRepository.search(eq("PT Maju"), eq(PageRequest.of(0, 15))))
            .thenReturn(new PageImpl<SupplierPriceListEntity>(List.of()));

        repository.search("  PT Maju  ", 15);

        verify(jpaRepository).search(eq("PT Maju"), eq(PageRequest.of(0, 15)));
    }
}
