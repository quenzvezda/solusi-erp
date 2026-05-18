package com.solusi.erp.inventory.adjustment.application.usecase;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.inventory.adjustment.application.usecase.command.ProcessStockAdjustmentUseCase;
import com.solusi.erp.inventory.adjustment.application.usecase.command.ProcessStockAdjustmentUseCaseImpl;
import com.solusi.erp.inventory.adjustment.domain.model.AdjustmentStatus;
import com.solusi.erp.inventory.adjustment.domain.model.StockAdjustment;
import com.solusi.erp.inventory.adjustment.domain.model.StockAdjustmentLineItem;
import com.solusi.erp.inventory.adjustment.domain.repository.StockAdjustmentRepository;
import com.solusi.erp.inventory.stock.application.dto.StockMovementPayload;
import com.solusi.erp.inventory.stock.domain.port.StockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProcessStockAdjustmentUseCase Tests")
class ProcessStockAdjustmentUseCaseTest {

    @Mock
    private StockAdjustmentRepository repository;
    @Mock
    private StockService stockService;
    @Mock
    private MessageSource messageSource;

    private ProcessStockAdjustmentUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ProcessStockAdjustmentUseCaseImpl(repository, stockService, messageSource);
    }

    private StockAdjustment buildDraftAdjustment(List<StockAdjustmentLineItem> lines) {
        return new StockAdjustment(
                new AuditMetadata(1L, 1L, null, null, null, null),
                "ADJ-001", LocalDate.now(), AdjustmentStatus.DRAFT, null,
                1L, "Main WH", 1L, "IDR", BigDecimal.ONE,
                new BigDecimal("500"), new BigDecimal("500"), lines);
    }

    @Test
    @DisplayName("execute throws localized not found message when adjustment does not exist")
    void execute_notFound_throwsLocalizedMessage() {
        when(repository.findById(404L)).thenReturn(Optional.empty());
        when(messageSource.getMessage(eq("msg.error.notfound"), isNull(), any()))
                .thenReturn("Not found");

        assertThatThrownBy(() -> useCase.execute(404L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Not found");
        verifyNoInteractions(stockService);
    }

    @Test
    @DisplayName("execute processes standard line and calls stockService once")
    void execute_standardLine_callsStockServiceOnce() {
        StockAdjustmentLineItem line = new StockAdjustmentLineItem(
                1L, 1, 10L, "P001", "Prod", false,
                null, null, null, 5L, "BIN-01", "Bin", "WH",
                null, null, BigDecimal.ONE, new BigDecimal("5"), new BigDecimal("100"),
                new BigDecimal("500"), null);

        StockAdjustment domain = buildDraftAdjustment(List.of(line));
        when(repository.findById(1L)).thenReturn(Optional.of(domain));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        useCase.execute(1L);

        verify(stockService, times(1)).adjust(any(StockMovementPayload.class));
        verify(repository).save(domain);
    }

    @Test
    @DisplayName("execute ignores serialized line with zero quantity")
    void execute_serializedZeroQuantity_savesWithoutStockMovement() {
        StockAdjustmentLineItem line = new StockAdjustmentLineItem(
                1L, 1, 10L, "P001", "Prod", true,
                null, null, null, 5L, "BIN-01", "Bin", "WH",
                null, null, BigDecimal.ONE, BigDecimal.ZERO, new BigDecimal("100"),
                BigDecimal.ZERO, null);

        StockAdjustment domain = buildDraftAdjustment(List.of(line));
        when(repository.findById(1L)).thenReturn(Optional.of(domain));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        useCase.execute(1L);

        verifyNoInteractions(stockService);
        verify(repository).save(domain);
    }

    @Test
    @DisplayName("execute generates missing serials for positive serialized adjustment")
    void execute_serializedPositiveWithMissingSerials_generatesMissingSerials() {
        StockAdjustmentLineItem line = new StockAdjustmentLineItem(
                1L, 1, 10L, "P001", "Prod", true,
                null, null, null, 5L, "BIN-01", "Bin", "WH",
                null, null, BigDecimal.ONE, new BigDecimal("3"), new BigDecimal("100"),
                new BigDecimal("300"), "SN-001");

        StockAdjustment domain = buildDraftAdjustment(List.of(line));
        when(repository.findById(1L)).thenReturn(Optional.of(domain));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        useCase.execute(1L);

        ArgumentCaptor<StockMovementPayload> captor = ArgumentCaptor.forClass(StockMovementPayload.class);
        verify(stockService, times(3)).adjust(captor.capture());
        assertThat(captor.getAllValues()).extracting(StockMovementPayload::getSerialNumber)
                .contains("SN-001")
                .doesNotContainNull();
        assertThat(captor.getAllValues()).extracting(StockMovementPayload::getQuantity)
                .allSatisfy(quantity -> assertThat(quantity).isEqualByComparingTo("1"));
    }

    @Test
    @DisplayName("execute sends null serials for negative serialized adjustment when serials are missing")
    void execute_serializedNegativeWithoutEnoughSerials_keepsMissingSerialsNull() {
        StockAdjustmentLineItem line = new StockAdjustmentLineItem(
                1L, 1, 10L, "P001", "Prod", true,
                null, null, null, 5L, "BIN-01", "Bin", "WH",
                null, null, BigDecimal.ONE, new BigDecimal("-2"), new BigDecimal("100"),
                new BigDecimal("-200"), "SN-001");

        StockAdjustment domain = buildDraftAdjustment(List.of(line));
        when(repository.findById(1L)).thenReturn(Optional.of(domain));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        useCase.execute(1L);

        ArgumentCaptor<StockMovementPayload> captor = ArgumentCaptor.forClass(StockMovementPayload.class);
        verify(stockService, times(2)).adjust(captor.capture());
        assertThat(captor.getAllValues()).extracting(StockMovementPayload::getSerialNumber)
                .contains("SN-001");
        assertThat(captor.getAllValues()).extracting(StockMovementPayload::getSerialNumber)
                .containsNull();
        assertThat(captor.getAllValues()).extracting(StockMovementPayload::getQuantity)
                .allSatisfy(quantity -> assertThat(quantity).isEqualByComparingTo("-1"));
    }

    @Test
    @DisplayName("execute processes serialized line: explodes into unit calls")
    void execute_serializedLine_explodesIntoUnits() {
        StockAdjustmentLineItem line = new StockAdjustmentLineItem(
                1L, 1, 10L, "P001", "Prod", true,
                null, null, null, 5L, "BIN-01", "Bin", "WH",
                null, null, BigDecimal.ONE, new BigDecimal("3"), new BigDecimal("100"),
                new BigDecimal("300"), "SN-001,SN-002,SN-003");

        StockAdjustment domain = buildDraftAdjustment(List.of(line));
        when(repository.findById(1L)).thenReturn(Optional.of(domain));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        useCase.execute(1L);

        // 3 units → 3 calls
        verify(stockService, times(3)).adjust(any(StockMovementPayload.class));
    }

    @Test
    @DisplayName("execute on already-COMPLETED throws exception from domain")
    void execute_alreadyCompleted_throwsException() {
        StockAdjustment domain = new StockAdjustment(
                new AuditMetadata(1L, 1L, null, null, null, null),
                "ADJ-001", LocalDate.now(), AdjustmentStatus.COMPLETED, null,
                1L, "WH", 1L, "IDR", BigDecimal.ONE,
                BigDecimal.ZERO, BigDecimal.ZERO, List.of());

        when(repository.findById(1L)).thenReturn(Optional.of(domain));

        assertThatThrownBy(() -> useCase.execute(1L))
                .isInstanceOf(RuntimeException.class);
    }
}
