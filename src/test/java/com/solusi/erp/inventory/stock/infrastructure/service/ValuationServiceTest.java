package com.solusi.erp.inventory.stock.infrastructure.service;

import com.solusi.erp.core.model.CurrencyAmount;
import com.solusi.erp.inventory.stock.infrastructure.persistence.ValuationLayerEntity;
import com.solusi.erp.inventory.stock.infrastructure.persistence.ValuationLayerJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ValuationServiceTest {

    @Mock
    private ValuationLayerJpaRepository valuationLayerRepository;

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private ValuationServiceImpl valuationService;

    @Test
    void shouldAddStock() {
        CurrencyAmount cost = CurrencyAmount.builder().localAmount(new BigDecimal("100")).build();
        valuationService.addStock(1L, 1L, null, new BigDecimal("10"), cost);
        verify(valuationLayerRepository).save(any(ValuationLayerEntity.class));
    }

    @Test
    void shouldConsumeSingleLayerPartially() {
        ValuationLayerEntity layer = createLayer(10, "100");
        when(valuationLayerRepository.findByProductIdAndContainerIdAndRemainingQuantityGreaterThanOrderByCreatedDateAsc(eq(1L), eq(1L), any()))
                .thenReturn(new ArrayList<>(List.of(layer)));

        CurrencyAmount hpp = valuationService.consumeStock(1L, 1L, null, new BigDecimal("4"));

        assertEquals(0, hpp.getLocalAmount().compareTo(new BigDecimal("100")));
        assertEquals(0, layer.getRemainingQuantity().compareTo(new BigDecimal("6")));
    }

    @Test
    void shouldConsumeMultipleLayers() {
        ValuationLayerEntity layer1 = createLayer(5, "100");
        ValuationLayerEntity layer2 = createLayer(10, "200");
        when(valuationLayerRepository.findByProductIdAndContainerIdAndRemainingQuantityGreaterThanOrderByCreatedDateAsc(eq(1L), eq(1L), any()))
                .thenReturn(new ArrayList<>(Arrays.asList(layer1, layer2)));

        // Consume 7 pieces: 5 from layer1 (@100) and 2 from layer2 (@200)
        // Total local cost: (5 * 100) + (2 * 200) = 500 + 400 = 900
        // Weighted average unit cost: 900 / 7 = 128.5714
        CurrencyAmount hpp = valuationService.consumeStock(1L, 1L, null, new BigDecimal("7"));

        assertEquals(0, hpp.getLocalAmount().compareTo(new BigDecimal("128.5714")));
        assertEquals(0, layer1.getRemainingQuantity().compareTo(BigDecimal.ZERO));
        assertEquals(0, layer2.getRemainingQuantity().compareTo(new BigDecimal("8")));
    }

    @Test
    void shouldThrowExceptionIfInsufficientStock() {
        ValuationLayerEntity layer = createLayer(5, "100");
        when(valuationLayerRepository.findByProductIdAndContainerIdAndRemainingQuantityGreaterThanOrderByCreatedDateAsc(eq(1L), eq(1L), any()))
                .thenReturn(new ArrayList<>(List.of(layer)));

        assertThrows(RuntimeException.class, () -> valuationService.consumeStock(1L, 1L, null, new BigDecimal("10")));
    }

    @Test
    void shouldHandleZeroCostItems() {
        ValuationLayerEntity layer = createLayer(10, "0");
        when(valuationLayerRepository.findByProductIdAndContainerIdAndRemainingQuantityGreaterThanOrderByCreatedDateAsc(eq(1L), eq(1L), any()))
                .thenReturn(new ArrayList<>(List.of(layer)));

        CurrencyAmount hpp = valuationService.consumeStock(1L, 1L, null, new BigDecimal("5"));

        assertEquals(0, hpp.getLocalAmount().compareTo(BigDecimal.ZERO));
    }

    @Test
    void shouldHandleSalesReturnAsNewLayer() {
        // Sales return is essentially adding a new layer with the original cost
        CurrencyAmount originalCost = CurrencyAmount.builder().localAmount(new BigDecimal("150")).build();
        
        valuationService.addStock(1L, 1L, null, new BigDecimal("2"), originalCost);
        
        verify(valuationLayerRepository).save(argThat(layer -> 
            layer.getInitialQuantity().compareTo(new BigDecimal("2")) == 0 &&
            layer.getUnitCost().getLocalAmount().compareTo(new BigDecimal("150")) == 0
        ));
    }

    private ValuationLayerEntity createLayer(double qty, String localAmount) {
        ValuationLayerEntity layer = new ValuationLayerEntity();
        layer.setRemainingQuantity(BigDecimal.valueOf(qty));
        layer.setUnitCost(CurrencyAmount.builder().localAmount(new BigDecimal(localAmount)).build());
        return layer;
    }
}
