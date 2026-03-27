package com.solusi.erp.inventory.productcategory.application.usecase.command;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.inventory.model.ProductCategoryType;
import com.solusi.erp.inventory.productcategory.domain.model.ProductCategory;
import com.solusi.erp.inventory.productcategory.domain.repository.ProductCategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateProductCategoryUseCase Tests")
class UpdateProductCategoryUseCaseTest {

    @Mock
    private ProductCategoryRepository repository;

    private UpdateProductCategoryUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new UpdateProductCategoryUseCaseImpl(repository);
    }

    @Test
    @DisplayName("execute updates name, type and note of an existing category")
    void execute_updatesExistingCategory() {
        AuditMetadata metadata = new AuditMetadata(1L, 1L, null, null, null, null);
        ProductCategory existing = new ProductCategory(metadata, "CAT-001", "Old Name", ProductCategoryType.STOCK, "Old Note");

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(ProductCategory.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductCategory result = useCase.execute(1L, "New Name", ProductCategoryType.SERVICE, "New Note");

        assertThat(result.getName()).isEqualTo("New Name");
        assertThat(result.getType()).isEqualTo(ProductCategoryType.SERVICE);
        assertThat(result.getNote()).isEqualTo("New Note");
        assertThat(result.getCode()).isEqualTo("CAT-001");
    }

    @Test
    @DisplayName("execute throws DomainException when category is not found")
    void execute_throwsDomainException_whenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(DomainException.class,
                () -> useCase.execute(99L, "Name", ProductCategoryType.STOCK, "Note"));
    }
}
