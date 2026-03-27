package com.solusi.erp.inventory.brand.application.usecase.command;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.service.SequenceGeneratorService;
import com.solusi.erp.inventory.brand.domain.model.Brand;
import com.solusi.erp.inventory.brand.domain.repository.BrandRepository;
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
@DisplayName("CreateBrandUseCase Tests")
class CreateBrandUseCaseTest {

    @Mock
    private BrandRepository repository;

    @Mock
    private SequenceGeneratorService sequenceGeneratorService;

    private CreateBrandUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateBrandUseCaseImpl(repository, sequenceGeneratorService);
    }

    @Test
    @DisplayName("execute generates code from sequence and saves brand")
    void execute_generatesCodeAndSavesBrand() {
        when(sequenceGeneratorService.generate("BRAND")).thenReturn("BR-001");
        when(repository.save(any(Brand.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Brand result = useCase.execute("Acme", "A note");

        assertThat(result.getCode()).isEqualTo("BR-001");
        assertThat(result.getName()).isEqualTo("Acme");
    }

    @Test
    @DisplayName("execute returns result from repository (with persisted id)")
    void execute_returnsResultFromRepository() {
        AuditMetadata metadata = new AuditMetadata(10L, 1L, null, null, null, null);
        Brand persisted = new Brand(metadata, "BR-001", "Acme", "note");

        when(sequenceGeneratorService.generate("BRAND")).thenReturn("BR-001");
        when(repository.save(any(Brand.class))).thenReturn(persisted);

        Brand result = useCase.execute("Acme", "note");

        assertThat(result.getId()).isEqualTo(10L);
    }
}
