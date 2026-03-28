package com.solusi.erp.inventory.adjustment.application.usecase;

import com.solusi.erp.inventory.adjustment.application.usecase.command.CreateStockAdjustmentUseCase;
import com.solusi.erp.inventory.adjustment.application.usecase.command.CreateStockAdjustmentUseCaseImpl;
import com.solusi.erp.inventory.adjustment.application.usecase.command.LineCommand;
import com.solusi.erp.inventory.adjustment.domain.model.AdjustmentStatus;
import com.solusi.erp.inventory.adjustment.domain.model.StockAdjustment;
import com.solusi.erp.inventory.adjustment.domain.repository.StockAdjustmentRepository;
import com.solusi.erp.inventory.model.Facility;
import com.solusi.erp.master.model.Currency;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateStockAdjustmentUseCase Tests")
class CreateStockAdjustmentUseCaseTest {

    @Mock
    private StockAdjustmentRepository repository;
    @Mock
    private com.solusi.erp.inventory.repository.FacilityRepository facilityRepository;
    @Mock
    private com.solusi.erp.master.currency.infrastructure.persistence.CurrencyJpaRepository currencyRepository;
    @Mock
    private com.solusi.erp.core.service.SequenceGeneratorService sequenceGeneratorService;
    @Mock
    private MessageSource messageSource;

    private CreateStockAdjustmentUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateStockAdjustmentUseCaseImpl(repository, facilityRepository,
                currencyRepository, sequenceGeneratorService, messageSource);
    }

    @Test
    @DisplayName("execute creates and saves stock adjustment with correct code")
    void execute_createsAdjustmentWithCode() {
        Facility facility = mock(Facility.class);
        when(facility.getName()).thenReturn("Main Warehouse");
        when(facilityRepository.findById(1L)).thenReturn(Optional.of(facility));

        Currency currency = mock(Currency.class);
        when(currency.getAlias()).thenReturn("IDR");
        when(currencyRepository.findById(1L)).thenReturn(Optional.of(currency));

        when(sequenceGeneratorService.generate("STOCK_ADJUSTMENT")).thenReturn("ADJ-2025-0001");

        when(repository.save(any(StockAdjustment.class))).thenAnswer(inv -> inv.getArgument(0));

        LineCommand line = new LineCommand(null, null, 1L, "P001", "Product 1", false,
                null, null, null, 1L, "BIN-01", "Bin 1", "Main WH",
                null, null, BigDecimal.ONE, new BigDecimal("5"), new BigDecimal("100"), null);

        StockAdjustment result = useCase.execute(LocalDate.now(), "test note", 1L, 1L,
                BigDecimal.ONE, List.of(line));

        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("ADJ-2025-0001");
        assertThat(result.getStatus()).isEqualTo(AdjustmentStatus.DRAFT);
        assertThat(result.getFacilityName()).isEqualTo("Main Warehouse");
        assertThat(result.getCurrencyAlias()).isEqualTo("IDR");

        verify(repository).save(any(StockAdjustment.class));
        verify(sequenceGeneratorService).generate("STOCK_ADJUSTMENT");
    }

    @Test
    @DisplayName("execute throws when facility not found")
    void execute_throwsWhenFacilityNotFound() {
        when(facilityRepository.findById(anyLong())).thenReturn(Optional.empty());
        when(messageSource.getMessage(anyString(), any(), any())).thenReturn("Not found");

        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                useCase.execute(LocalDate.now(), null, 99L, 1L, BigDecimal.ONE, List.of()))
                .isInstanceOf(RuntimeException.class);
    }
}

