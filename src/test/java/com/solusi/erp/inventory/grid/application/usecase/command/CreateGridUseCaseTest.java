package com.solusi.erp.inventory.grid.application.usecase.command;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.inventory.grid.domain.model.Grid;
import com.solusi.erp.inventory.grid.domain.repository.GridRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateGridUseCase Tests")
class CreateGridUseCaseTest {

    @Mock
    private GridRepository repository;

    private CreateGridUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateGridUseCaseImpl(repository);
    }

    @Test
    @DisplayName("execute throws DomainException when code already exists in facility")
    void execute_throwsWhenDuplicateCode() {
        when(repository.existsByFacilityIdAndCode(1L, "GRD-001")).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(1L, "GRD-001", "Storage A", null, true))
            .isInstanceOf(DomainException.class)
            .hasMessageContaining("msg.error.grid.duplicate-code");
    }

    @Test
    @DisplayName("execute saves grid when code is unique")
    void execute_savesGridWhenUnique() {
        when(repository.existsByFacilityIdAndCode(1L, "GRD-001")).thenReturn(false);
        when(repository.save(any(Grid.class))).thenAnswer(inv -> inv.getArgument(0));

        Grid result = useCase.execute(1L, "GRD-001", "Storage A", "Note", true);

        assertThat(result.getCode()).isEqualTo("GRD-001");
        assertThat(result.getName()).isEqualTo("Storage A");
        assertThat(result.getFacilityId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("execute returns persisted result from repository")
    void execute_returnsPersisted() {
        AuditMetadata metadata = new AuditMetadata(15L, 1L, null, null, null, null);
        Grid persisted = new Grid(metadata, 1L, "Main WH", "GRD-001", "Storage A", null, true);

        when(repository.existsByFacilityIdAndCode(1L, "GRD-001")).thenReturn(false);
        when(repository.save(any(Grid.class))).thenReturn(persisted);

        Grid result = useCase.execute(1L, "GRD-001", "Storage A", null, true);

        assertThat(result.getId()).isEqualTo(15L);
    }
}
