package com.solusi.erp.inventory.productcategory.application.usecase.command;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.inventory.productcategory.domain.model.ProductCategoryType;
import com.solusi.erp.inventory.productcategory.domain.model.ProductCategory;
import com.solusi.erp.inventory.productcategory.domain.repository.ProductCategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeleteProductCategoryUseCase Tests")
class DeleteProductCategoryUseCaseTest {

    @Mock
    private ProductCategoryRepository repository;

    private DeleteProductCategoryUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new DeleteProductCategoryUseCaseImpl(repository);
    }

    @Test
    @DisplayName("execute deletes an existing category by id")
    void execute_deletesExistingCategory() {
        AuditMetadata metadata = new AuditMetadata(1L, 1L, null, null, null, null);
        ProductCategory existing = new ProductCategory(metadata, "CAT-001", "Electronics", ProductCategoryType.STOCK, "note");

        // existsByCode returns false (default mock) → findById is called
        when(repository.existsByCode(String.valueOf(1L))).thenReturn(false);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));

        useCase.execute(1L);

        verify(repository).delete(1L);
    }

    @Test
    @DisplayName("execute throws DomainException when category is not found")
    void execute_throwsDomainException_whenNotFound() {
        when(repository.existsByCode(String.valueOf(99L))).thenReturn(false);
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(DomainException.class, () -> useCase.execute(99L));
    }
}
