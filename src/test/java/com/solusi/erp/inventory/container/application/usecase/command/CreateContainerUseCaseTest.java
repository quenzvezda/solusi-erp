package com.solusi.erp.inventory.container.application.usecase.command;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.core.service.SequenceGeneratorService;
import com.solusi.erp.inventory.container.domain.model.Container;
import com.solusi.erp.inventory.container.domain.repository.ContainerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateContainerUseCase Tests")
class CreateContainerUseCaseTest {

    @Mock
    private ContainerRepository repository;

    @Mock
    private SequenceGeneratorService sequenceGeneratorService;

    private CreateContainerUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateContainerUseCaseImpl(repository, sequenceGeneratorService);
    }

    @Test
    @DisplayName("execute throws DomainException when barcode already exists")
    void execute_throwsWhenDuplicateBarcode() {
        when(repository.existsByBarcode("BARC-001")).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(1L, "Bin A1", "BARC-001", null, null, null, null, null, true))
            .isInstanceOf(DomainException.class)
            .hasMessageContaining("msg.error.container.duplicate-barcode");
    }

    @Test
    @DisplayName("execute generates code and saves container when barcode is unique")
    void execute_generatesCodeAndSaves() {
        when(repository.existsByBarcode("BARC-001")).thenReturn(false);
        when(sequenceGeneratorService.generate("CONTAINER")).thenReturn("CNT-001");
        when(repository.save(any(Container.class))).thenAnswer(inv -> inv.getArgument(0));

        Container result = useCase.execute(1L, "Bin A1", "BARC-001", null, null, null, null, null, true);

        assertThat(result.getCode()).isEqualTo("CNT-001");
        assertThat(result.getName()).isEqualTo("Bin A1");
        assertThat(result.getGridId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("execute skips barcode check when barcode is null/blank")
    void execute_skipsCheckWhenNullBarcode() {
        when(sequenceGeneratorService.generate("CONTAINER")).thenReturn("CNT-001");
        when(repository.save(any(Container.class))).thenAnswer(inv -> inv.getArgument(0));

        Container result = useCase.execute(1L, "Bin A1", null, null, null, null, null, null, true);

        assertThat(result.getCode()).isEqualTo("CNT-001");
    }

    @Test
    @DisplayName("execute returns persisted result from repository")
    void execute_returnsPersisted() {
        AuditMetadata metadata = new AuditMetadata(20L, 1L, null, null, null, null);
        Container persisted = new Container(metadata, 1L, "Storage A", "Main WH", "CNT-001",
            "Bin A1", null, null, null, null, null, null, true);

        when(sequenceGeneratorService.generate("CONTAINER")).thenReturn("CNT-001");
        when(repository.save(any(Container.class))).thenReturn(persisted);

        Container result = useCase.execute(1L, "Bin A1", null, null, null, null, null, null, true);

        assertThat(result.getId()).isEqualTo(20L);
    }
}
