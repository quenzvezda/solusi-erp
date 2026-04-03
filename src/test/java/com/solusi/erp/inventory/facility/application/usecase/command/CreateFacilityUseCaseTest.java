package com.solusi.erp.inventory.facility.application.usecase.command;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.infrastructure.sequence.SequenceGeneratorService;
import com.solusi.erp.inventory.facility.domain.model.Facility;
import com.solusi.erp.inventory.facility.domain.repository.FacilityRepository;
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
@DisplayName("CreateFacilityUseCase Tests")
class CreateFacilityUseCaseTest {

    @Mock
    private FacilityRepository repository;

    @Mock
    private SequenceGeneratorService sequenceGeneratorService;

    private CreateFacilityUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateFacilityUseCaseImpl(repository, sequenceGeneratorService);
    }

    @Test
    @DisplayName("execute generates code from sequence and saves facility")
    void execute_generatesCodeAndSavesFacility() {
        when(sequenceGeneratorService.generate("FACILITY")).thenReturn("FAC-001");
        when(repository.save(any(Facility.class))).thenAnswer(inv -> inv.getArgument(0));

        Facility result = useCase.execute("Warehouse A", 1L, "Jl. Test", 10L, "12345", "Note", true);

        assertThat(result.getCode()).isEqualTo("FAC-001");
        assertThat(result.getName()).isEqualTo("Warehouse A");
        assertThat(result.getOwnerId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("execute returns persisted result from repository")
    void execute_returnsPersisted() {
        AuditMetadata metadata = new AuditMetadata(42L, 1L, null, null, null, null);
        Facility persisted = new Facility(metadata, "FAC-001", "Warehouse A", 1L, null,
            "Jl. Test", 10L, null, "12345", "Note", true);

        when(sequenceGeneratorService.generate("FACILITY")).thenReturn("FAC-001");
        when(repository.save(any(Facility.class))).thenReturn(persisted);

        Facility result = useCase.execute("Warehouse A", 1L, "Jl. Test", 10L, "12345", "Note", true);

        assertThat(result.getId()).isEqualTo(42L);
    }
}
