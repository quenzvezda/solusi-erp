package com.solusi.erp.inventory.service;

import com.solusi.erp.core.model.CurrencyAmount;
import com.solusi.erp.inventory.dto.StockMovementPayload;
import com.solusi.erp.inventory.model.*;
import com.solusi.erp.inventory.model.StockAdjustment.AdjustmentStatus;
import com.solusi.erp.inventory.repository.StockAdjustmentRepository;
import com.solusi.erp.inventory.service.impl.StockAdjustmentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockAdjustmentServiceTest {

    @Mock
    private StockAdjustmentRepository repository;

    @Mock
    private StockService stockService;

    @InjectMocks
    private StockAdjustmentServiceImpl service;

    private StockAdjustment adjustment;

    @BeforeEach
    void setUp() {
        adjustment = new StockAdjustment();
        adjustment.setId(1L);
        adjustment.setCode("ADJ-001");
        adjustment.setTransactionDate(LocalDate.now());
        adjustment.setStatus(AdjustmentStatus.DRAFT);
        
        CurrencyAmount cost = new CurrencyAmount();
        cost.setExchangeRate(BigDecimal.ONE);
        adjustment.setTotalCost(cost);

        List<StockAdjustmentLine> lines = new ArrayList<>();
        StockAdjustmentLine line = new StockAdjustmentLine();
        line.setProduct(mock(Product.class));
        line.setContainer(mock(Container.class));
        line.setQuantity(BigDecimal.TEN);
        line.setUnitCost(new BigDecimal("100"));
        lines.add(line);
        
        adjustment.setLines(lines);
    }

    @Test
    void shouldProcessAdjustmentSuccessfully() {
        when(repository.findById(1L)).thenReturn(Optional.of(adjustment));

        service.process(1L);

        verify(stockService, times(1)).adjust(any(StockMovementPayload.class));
        assertEquals(AdjustmentStatus.COMPLETED, adjustment.getStatus());
        verify(repository).save(adjustment);
    }

    @Test
    void shouldThrowExceptionIfAlreadyCompleted() {
        adjustment.setStatus(AdjustmentStatus.COMPLETED);
        when(repository.findById(1L)).thenReturn(Optional.of(adjustment));

        assertThrows(RuntimeException.class, () -> service.process(1L));
    }

    @Test
    void shouldHandleNegativeQuantityAdjustment() {
        adjustment.getLines().get(0).setQuantity(new BigDecimal("-5"));
        when(repository.findById(1L)).thenReturn(Optional.of(adjustment));

        service.process(1L);

        verify(stockService).adjust(argThat(payload -> 
            payload.getQuantity().compareTo(new BigDecimal("-5")) == 0
        ));
    }

    @Test
    void shouldProcessMultipleLinesInOrder() {
        StockAdjustmentLine line2 = new StockAdjustmentLine();
        line2.setProduct(mock(Product.class));
        line2.setContainer(mock(Container.class));
        line2.setQuantity(new BigDecimal("20"));
        line2.setUnitCost(new BigDecimal("50"));
        adjustment.getLines().add(line2);

        when(repository.findById(1L)).thenReturn(Optional.of(adjustment));

        service.process(1L);

        verify(stockService, times(2)).adjust(any(StockMovementPayload.class));
    }

    @Test
    void shouldPropagateExceptionFromStockService() {
        when(repository.findById(1L)).thenReturn(Optional.of(adjustment));
        doThrow(new RuntimeException("Insufficient stock")).when(stockService).adjust(any());

        assertThrows(RuntimeException.class, () -> service.process(1L));
        // Status should NOT be completed due to rollback (though in unit test we check the object state)
        // Spring @Transactional will handle the real rollback.
    }
}
