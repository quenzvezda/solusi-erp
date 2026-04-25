package com.solusi.erp.purchasing.supplierpricelist.application.usecase.command;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.core.infrastructure.sequence.SequenceGeneratorService;
import com.solusi.erp.purchasing.supplierpricelist.domain.model.SupplierPriceList;
import com.solusi.erp.purchasing.supplierpricelist.domain.repository.SupplierPriceListRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateSupplierPriceListUseCase Tests")
class CreateSupplierPriceListUseCaseTest {

    @Mock
    private SupplierPriceListRepository repository;

    @Mock
    private SequenceGeneratorService sequenceGeneratorService;

    private CreateSupplierPriceListUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateSupplierPriceListUseCaseImpl(repository, sequenceGeneratorService);
    }

    @Test
    @DisplayName("execute generates code from sequence and saves price list")
    void execute_generatesCodeAndSaves() {
        when(sequenceGeneratorService.generate("SPL")).thenReturn("SPL-2607-00001");
        when(repository.existsOverlapping(eq(1L), eq(2L), eq(3L), eq(4L),
            eq(LocalDate.of(2026, 7, 1)), isNull(), isNull())).thenReturn(false);
        when(repository.save(any(SupplierPriceList.class))).thenAnswer(inv -> inv.getArgument(0));

        SupplierPriceList result = useCase.execute(
            1L, 2L, 3L, 4L,
            new BigDecimal("150.0000"),
            new BigDecimal("10.0000"),
            LocalDate.of(2026, 7, 1),
            null, "Test note", true
        );

        assertThat(result.getCode()).isEqualTo("SPL-2607-00001");
        assertThat(result.getSupplierId()).isEqualTo(1L);
        assertThat(result.getUnitPrice()).isEqualByComparingTo("150.0000");
        verify(repository).save(any(SupplierPriceList.class));
    }

    @Test
    @DisplayName("execute returns persisted result with id from repository")
    void execute_returnsPersisted() {
        AuditMetadata metadata = new AuditMetadata(10L, 1L, null, null, null, null);
        SupplierPriceList persisted = new SupplierPriceList(metadata, "SPL-2607-00001",
            1L, 2L, 3L, 4L, new BigDecimal("150.0000"), new BigDecimal("10.0000"),
            LocalDate.of(2026, 7, 1), null, "note", true);

        when(sequenceGeneratorService.generate("SPL")).thenReturn("SPL-2607-00001");
        when(repository.existsOverlapping(any(), any(), any(), any(), any(), any(), any())).thenReturn(false);
        when(repository.save(any(SupplierPriceList.class))).thenReturn(persisted);

        SupplierPriceList result = useCase.execute(
            1L, 2L, 3L, 4L,
            new BigDecimal("150.0000"),
            new BigDecimal("10.0000"),
            LocalDate.of(2026, 7, 1),
            null, "note", true
        );

        assertThat(result.getId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("execute throws DomainException when overlapping price list exists")
    void execute_throwsWhenOverlapping() {
        when(sequenceGeneratorService.generate("SPL")).thenReturn("SPL-2607-00001");
        when(repository.existsOverlapping(eq(1L), eq(2L), eq(3L), eq(4L),
            eq(LocalDate.of(2026, 7, 1)), eq(LocalDate.of(2026, 12, 31)), isNull())).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(
            1L, 2L, 3L, 4L,
            new BigDecimal("150.0000"),
            new BigDecimal("10.0000"),
            LocalDate.of(2026, 7, 1),
            LocalDate.of(2026, 12, 31),
            null, true
        ))
            .isInstanceOf(DomainException.class)
            .hasMessageContaining("msg.error.spl.overlap");
    }
}
