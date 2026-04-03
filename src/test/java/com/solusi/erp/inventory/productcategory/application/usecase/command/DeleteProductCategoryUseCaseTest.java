package com.solusi.erp.inventory.productcategory.application.usecase.command;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.inventory.productcategory.domain.model.ProductCategory;
import com.solusi.erp.inventory.productcategory.domain.model.ProductCategoryType;
import com.solusi.erp.inventory.productcategory.domain.port.ProductCategoryInUseChecker;
import com.solusi.erp.inventory.productcategory.domain.repository.ProductCategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("DeleteProductCategoryUseCase Tests")
class DeleteProductCategoryUseCaseTest {

    private ProductCategoryRepository repository;
    private ProductCategoryInUseChecker inUseChecker;
    private DeleteProductCategoryUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        repository = mock(ProductCategoryRepository.class);
        inUseChecker = mock(ProductCategoryInUseChecker.class);
        useCase = new DeleteProductCategoryUseCaseImpl(repository, inUseChecker);
    }

    @Test
    @DisplayName("execute deletes an existing category when not in use")
    void execute_deletesExistingCategory() {
        AuditMetadata metadata = new AuditMetadata(1L, 1L, null, null, null, null);
        ProductCategory existing = new ProductCategory(metadata, "CAT-001", "Electronics", ProductCategoryType.STOCK, "note");
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(inUseChecker.isUsedByAnyProduct(1L)).thenReturn(false);

        useCase.execute(1L);

        verify(repository).delete(1L);
    }

    @Test
    @DisplayName("execute throws DomainException when category is not found")
    void execute_throwsDomainException_whenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(DomainException.class, () -> useCase.execute(99L));
        verify(repository, never()).delete(99L);
    }

    @Test
    @DisplayName("execute throws DomainException when category is in use by a product")
    void execute_throwsDomainException_whenInUse() {
        AuditMetadata metadata = new AuditMetadata(2L, 2L, null, null, null, null);
        ProductCategory existing = new ProductCategory(metadata, "CAT-002", "Used Category", ProductCategoryType.STOCK, null);
        when(repository.findById(2L)).thenReturn(Optional.of(existing));
        when(inUseChecker.isUsedByAnyProduct(2L)).thenReturn(true);

        assertThrows(DomainException.class, () -> useCase.execute(2L));
        verify(repository, never()).delete(2L);
    }
}
