package com.solusi.erp.inventory.productcategory.application.usecase.command;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.infrastructure.sequence.SequenceGeneratorService;
import com.solusi.erp.inventory.model.ProductCategoryType;
import com.solusi.erp.inventory.productcategory.domain.model.ProductCategory;
import com.solusi.erp.inventory.productcategory.domain.repository.ProductCategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateProductCategoryUseCase Tests")
class CreateProductCategoryUseCaseTest {

    @Mock
    private ProductCategoryRepository repository;

    @Mock
    private SequenceGeneratorService sequenceGeneratorService;

    private CreateProductCategoryUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateProductCategoryUseCaseImpl(repository, sequenceGeneratorService);
    }

    @Test
    @DisplayName("execute generates code from sequence and saves category")
    void execute_generatesCodeAndSavesCategory() {
        when(sequenceGeneratorService.generate("PRODUCT_CATEGORY")).thenReturn("CAT-001");
        when(repository.save(any(ProductCategory.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductCategory result = useCase.execute("Electronics", ProductCategoryType.STOCK, "A note");

        assertThat(result.getCode()).isEqualTo("CAT-001");
        assertThat(result.getName()).isEqualTo("Electronics");
        assertThat(result.getType()).isEqualTo(ProductCategoryType.STOCK);
    }

    @Test
    @DisplayName("execute returns the persisted category from repository")
    void execute_returnsPersistedCategory() {
        AuditMetadata metadata = new AuditMetadata(5L, 1L, null, null, null, null);
        ProductCategory persisted = new ProductCategory(metadata, "CAT-001", "Electronics", ProductCategoryType.STOCK, "note");

        when(sequenceGeneratorService.generate("PRODUCT_CATEGORY")).thenReturn("CAT-001");
        when(repository.save(any(ProductCategory.class))).thenReturn(persisted);

        ProductCategory result = useCase.execute("Electronics", ProductCategoryType.STOCK, "note");

        assertThat(result.getId()).isEqualTo(5L);
    }
}
