package com.solusi.erp.inventory.adjustment.application.usecase.command;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.inventory.adjustment.domain.model.AdjustmentStatus;
import com.solusi.erp.inventory.adjustment.domain.model.StockAdjustment;
import com.solusi.erp.inventory.adjustment.domain.repository.StockAdjustmentRepository;
import com.solusi.erp.inventory.facility.infrastructure.persistence.FacilityEntity;
import com.solusi.erp.inventory.facility.infrastructure.persistence.FacilityJpaRepository;
import com.solusi.erp.master.currency.domain.model.Currency;
import com.solusi.erp.master.currency.domain.repository.CurrencyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateStockAdjustmentUseCase Tests")
class UpdateStockAdjustmentUseCaseImplTest {

    @Mock private StockAdjustmentRepository repository;
    @Mock private FacilityJpaRepository facilityJpaRepository;
    @Mock private CurrencyRepository currencyRepository;
    @Mock private MessageSource messageSource;

    private UpdateStockAdjustmentUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new UpdateStockAdjustmentUseCaseImpl(
                repository, facilityJpaRepository, currencyRepository, messageSource);
        lenient().when(messageSource.getMessage(anyString(), isNull(), any(Locale.class)))
                .thenAnswer(inv -> inv.getArgument(0));
    }

    private StockAdjustment draftAdjustment() {
        return new StockAdjustment(
                AuditMetadata.empty(), "ADJ-001", LocalDate.now(),
                AdjustmentStatus.DRAFT, "old note",
                1L, "Old Facility", 1L, "USD",
                BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, List.of()
        );
    }

    private FacilityEntity facilityWithName(String name) {
        FacilityEntity f = new FacilityEntity();
        f.setName(name);
        return f;
    }

    private Currency currencyWithAlias(String alias) {
        return new Currency(AuditMetadata.empty(), "$", alias, "Dollar", null, false, true);
    }

    @Test
    @DisplayName("throws RuntimeException when stock adjustment is not found")
    void execute_throws_whenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(
                99L, LocalDate.now(), "note", 1L, 1L, BigDecimal.ONE, List.of()))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("throws RuntimeException when adjustment status is COMPLETED")
    void execute_throws_whenAlreadyCompleted() {
        StockAdjustment completed = new StockAdjustment(
                AuditMetadata.empty(), "ADJ-001", LocalDate.now(),
                AdjustmentStatus.COMPLETED, "done",
                1L, "Facility", 1L, "USD",
                BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, List.of()
        );
        when(repository.findById(1L)).thenReturn(Optional.of(completed));

        assertThatThrownBy(() -> useCase.execute(
                1L, LocalDate.now(), "note", 1L, 1L, BigDecimal.ONE, List.of()))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("throws RuntimeException when facility is not found")
    void execute_throws_whenFacilityNotFound() {
        when(repository.findById(1L)).thenReturn(Optional.of(draftAdjustment()));
        when(facilityJpaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(
                1L, LocalDate.now(), "note", 99L, 1L, BigDecimal.ONE, List.of()))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("throws RuntimeException when currency is not found")
    void execute_throws_whenCurrencyNotFound() {
        when(repository.findById(1L)).thenReturn(Optional.of(draftAdjustment()));
        when(facilityJpaRepository.findById(1L)).thenReturn(Optional.of(facilityWithName("WH")));
        when(currencyRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(
                1L, LocalDate.now(), "note", 1L, 99L, BigDecimal.ONE, List.of()))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("successfully updates draft adjustment with all provided fields")
    void execute_updatesAdjustment_successfully() {
        when(repository.findById(1L)).thenReturn(Optional.of(draftAdjustment()));
        when(facilityJpaRepository.findById(2L)).thenReturn(Optional.of(facilityWithName("Main Warehouse")));
        when(currencyRepository.findById(3L)).thenReturn(Optional.of(currencyWithAlias("IDR")));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        LineCommand line = new LineCommand(
                null, 0, 10L, "PRD-001", "Product 1", false,
                1L, "G01", "Grid 1", 1L, "C01", "Container 1", "Main WH",
                1L, "PCS", BigDecimal.ONE,
                new BigDecimal("5"), new BigDecimal("10000"), "SN-001"
        );

        StockAdjustment result = useCase.execute(
                1L, LocalDate.of(2025, 1, 15), "Updated note",
                2L, 3L, new BigDecimal("15000"), List.of(line)
        );

        assertThat(result.getFacilityId()).isEqualTo(2L);
        assertThat(result.getFacilityName()).isEqualTo("Main Warehouse");
        assertThat(result.getCurrencyAlias()).isEqualTo("IDR");
        assertThat(result.getNote()).isEqualTo("Updated note");
        assertThat(result.getLines()).hasSize(1);
    }

    @Test
    @DisplayName("line item total = quantity × unitCost")
    void execute_calculatesLineItemTotal_fromQuantityTimesUnitCost() {
        when(repository.findById(1L)).thenReturn(Optional.of(draftAdjustment()));
        when(facilityJpaRepository.findById(1L)).thenReturn(Optional.of(facilityWithName("WH")));
        when(currencyRepository.findById(1L)).thenReturn(Optional.of(currencyWithAlias("IDR")));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        LineCommand line = new LineCommand(
                null, 0, 1L, "P1", "Prod 1", false,
                null, null, null, null, null, null, "WH",
                1L, "PCS", BigDecimal.ONE,
                new BigDecimal("3"), new BigDecimal("2000"), null
        );

        StockAdjustment result = useCase.execute(
                1L, LocalDate.now(), "note", 1L, 1L, BigDecimal.ONE, List.of(line));

        // total = 3 × 2000 = 6000; rate = 1; totalLocal = 6000
        assertThat(result.getTotalAmountOriginal()).isEqualByComparingTo(new BigDecimal("6000"));
        assertThat(result.getTotalAmountLocal()).isEqualByComparingTo(new BigDecimal("6000"));
    }

    @Test
    @DisplayName("line item total defaults to ZERO when quantity is null")
    void execute_usesZeroTotal_whenQuantityIsNull() {
        when(repository.findById(1L)).thenReturn(Optional.of(draftAdjustment()));
        when(facilityJpaRepository.findById(1L)).thenReturn(Optional.of(facilityWithName("WH")));
        when(currencyRepository.findById(1L)).thenReturn(Optional.of(currencyWithAlias("IDR")));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        LineCommand line = new LineCommand(
                null, 0, 1L, "P1", "Prod 1", false,
                null, null, null, null, null, null, "WH",
                1L, "PCS", BigDecimal.ONE,
                null, new BigDecimal("5000"), null   // quantity = null
        );

        StockAdjustment result = useCase.execute(
                1L, LocalDate.now(), "note", 1L, 1L, BigDecimal.ONE, List.of(line));

        assertThat(result.getTotalAmountOriginal()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
