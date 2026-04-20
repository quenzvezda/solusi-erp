package com.solusi.erp.purchasing.supplierpricelist.application.usecase.command;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateSupplierPriceListUseCase Tests")
class UpdateSupplierPriceListUseCaseTest {

    @Mock
    private SupplierPriceListRepository repository;

    private UpdateSupplierPriceListUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new UpdateSupplierPriceListUseCaseImpl(repository);
    }

    @Test
    @DisplayName("execute updates price list fields successfully")
    void execute_updatesSuccessfully() {
        AuditMetadata metadata = new AuditMetadata(1L, 1L, null, null, null, null);
        SupplierPriceList existing = new SupplierPriceList(metadata, "SPL-001",
            10L, 20L, 30L, 40L,
            new BigDecimal("100.0000"), new BigDecimal("1.0000"),
            LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 30), "Old", true);

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.existsOverlapping(eq(15L), eq(25L), eq(35L), eq(45L),
            eq(LocalDate.of(2026, 7, 1)), eq(LocalDate.of(2026, 12, 31)), eq(1L))).thenReturn(false);
        when(repository.save(any(SupplierPriceList.class))).thenAnswer(inv -> inv.getArgument(0));

        SupplierPriceList result = useCase.execute(1L,
            15L, 25L, 35L, 45L,
            new BigDecimal("200.0000"), new BigDecimal("5.0000"),
            LocalDate.of(2026, 7, 1), LocalDate.of(2026, 12, 31),
            "Updated", true
        );

        assertThat(result.getSupplierId()).isEqualTo(15L);
        assertThat(result.getProductId()).isEqualTo(25L);
        assertThat(result.getUnitPrice()).isEqualByComparingTo("200.0000");
        assertThat(result.getNote()).isEqualTo("Updated");
        verify(repository).save(any(SupplierPriceList.class));
    }

    @Test
    @DisplayName("execute throws DomainException when not found")
    void execute_throwsWhenNotFound() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(999L,
            2L, 3L, 4L, 5L,
            new BigDecimal("100.0000"), new BigDecimal("1.0000"),
            LocalDate.of(2026, 7, 1), null, null, true
        ))
            .isInstanceOf(DomainException.class)
            .hasMessageContaining("msg.error.spl.notfound");
    }

    @Test
    @DisplayName("execute throws DomainException when overlapping after update")
    void execute_throwsWhenOverlapping() {
        AuditMetadata metadata = new AuditMetadata(1L, 1L, null, null, null, null);
        SupplierPriceList existing = new SupplierPriceList(metadata, "SPL-001",
            10L, 20L, 30L, 40L,
            new BigDecimal("100.0000"), new BigDecimal("1.0000"),
            LocalDate.of(2026, 1, 1), null, null, true);

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.existsOverlapping(eq(10L), eq(20L), eq(30L), eq(40L),
            eq(LocalDate.of(2026, 7, 1)), eq(LocalDate.of(2026, 12, 31)), eq(1L))).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(1L,
            10L, 20L, 30L, 40L,
            new BigDecimal("100.0000"), new BigDecimal("1.0000"),
            LocalDate.of(2026, 7, 1), LocalDate.of(2026, 12, 31), null, true
        ))
            .isInstanceOf(DomainException.class)
            .hasMessageContaining("msg.error.spl.overlap");
    }
}
