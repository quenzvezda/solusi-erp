package com.solusi.erp.inventory.productcategory.application.usecase.query;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.dto.LookupDto;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetProductCategoryLookupUseCase Tests")
class GetProductCategoryLookupUseCaseTest {

    @Mock
    private ProductCategoryRepository repository;

    private GetProductCategoryLookupUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetProductCategoryLookupUseCaseImpl(repository);
    }

    @Test
    @DisplayName("getById returns a LookupDto with id, name, code (subText), and type in payload")
    void getById_returnsMappedLookupDto() {
        AuditMetadata metadata = new AuditMetadata(1L, 1L, null, null, null, null);
        ProductCategory category = new ProductCategory(metadata, "CAT-001", "Electronics", ProductCategoryType.STOCK, "note");

        when(repository.findById(1L)).thenReturn(Optional.of(category));

        LookupDto result = useCase.getById(1L);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("Electronics");
        assertThat(result.subText()).isEqualTo("CAT-001");
        assertThat(result.payload()).containsEntry("type", "STOCK");
    }

    @Test
    @DisplayName("getById throws DomainException when category is not found")
    void getById_throwsDomainException_whenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(DomainException.class, () -> useCase.getById(99L));
    }

    @Test
    @DisplayName("search returns list of LookupDtos for matching categories")
    void search_returnsListOfLookupDtos() {
        AuditMetadata meta1 = new AuditMetadata(1L, 1L, null, null, null, null);
        AuditMetadata meta2 = new AuditMetadata(2L, 1L, null, null, null, null);
        ProductCategory cat1 = new ProductCategory(meta1, "CAT-001", "Electronics", ProductCategoryType.STOCK, "note");
        ProductCategory cat2 = new ProductCategory(meta2, "CAT-002", "Services", ProductCategoryType.SERVICE, "note");

        when(repository.search("test", 5)).thenReturn(List.of(cat1, cat2));

        List<LookupDto> result = useCase.search("test", 5);

        assertThat(result).hasSize(2);
    }
}
