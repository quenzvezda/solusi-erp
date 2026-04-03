package com.solusi.erp.inventory.brand.application.usecase.query;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.inventory.brand.domain.model.Brand;
import com.solusi.erp.inventory.brand.domain.repository.BrandRepository;
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
@DisplayName("GetBrandLookupUseCase Tests")
class GetBrandLookupUseCaseTest {

    @Mock
    private BrandRepository repository;

    private GetBrandLookupUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetBrandLookupUseCaseImpl(repository);
    }

    @Test
    @DisplayName("getById returns a LookupDto with correct id, name, and code (in subText)")
    void getById_returnsMappedLookupDto() {
        AuditMetadata metadata = new AuditMetadata(1L, 1L, null, null, null, null);
        Brand brand = new Brand(metadata, "BR-001", "Acme", "note");

        when(repository.findById(1L)).thenReturn(Optional.of(brand));

        LookupDto result = useCase.getById(1L);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("Acme");
        assertThat(result.subText()).isEqualTo("BR-001");
    }

    @Test
    @DisplayName("getById throws DomainException when brand is not found")
    void getById_throwsDomainException_whenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(DomainException.class, () -> useCase.getById(99L));
    }

    @Test
    @DisplayName("search returns list of LookupDtos for matching brands")
    void search_returnsListOfLookupDtos() {
        AuditMetadata meta1 = new AuditMetadata(1L, 1L, null, null, null, null);
        AuditMetadata meta2 = new AuditMetadata(2L, 1L, null, null, null, null);
        Brand brand1 = new Brand(meta1, "BR-001", "Acme", "note");
        Brand brand2 = new Brand(meta2, "BR-002", "Beta", "note");

        when(repository.search("test", 5)).thenReturn(List.of(brand1, brand2));

        List<LookupDto> result = useCase.search("test", 5);

        assertThat(result).hasSize(2);
    }
}
