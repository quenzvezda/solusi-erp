package com.solusi.erp.inventory.stock.domain;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.inventory.stock.domain.model.CostAmount;
import com.solusi.erp.inventory.stock.domain.model.ReferenceType;
import com.solusi.erp.inventory.stock.domain.model.ValuationLayer;
import com.solusi.erp.inventory.stock.domain.repository.ValuationLayerRepository;
import com.solusi.erp.inventory.stock.domain.service.FifoValuationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FifoValuationService Domain Tests")
class FifoValuationServiceTest {

    @Mock
    private ValuationLayerRepository layerRepository;

    private FifoValuationService service;

    @BeforeEach
    void setUp() {
        service = new FifoValuationService(layerRepository);
    }

    @Test
    @DisplayName("addLayer creates and saves a new valuation layer")
    void addLayer_createsNewLayer() {
        CostAmount cost = CostAmount.of(1L, new BigDecimal("15000"), new BigDecimal("10"));

        service.addLayer(1L, 10L, null, BigDecimal.TEN, cost);

        ArgumentCaptor<ValuationLayer> captor = ArgumentCaptor.forClass(ValuationLayer.class);
        verify(layerRepository).save(captor.capture());

        ValuationLayer saved = captor.getValue();
        assertThat(saved.getProductId()).isEqualTo(1L);
        assertThat(saved.getContainerId()).isEqualTo(10L);
        assertThat(saved.getInitialQuantity()).isEqualByComparingTo(BigDecimal.TEN);
        assertThat(saved.getRemainingQuantity()).isEqualByComparingTo(BigDecimal.TEN);
        assertThat(saved.getUnitCost().originalAmount()).isEqualByComparingTo(new BigDecimal("10"));
    }

    @Test
    @DisplayName("consumeLayers follows FIFO order — oldest layer consumed first")
    void consumeLayers_fifoOrder() {
        // Layer 1: 10 units @ $5 local
        ValuationLayer layer1 = new ValuationLayer(
                new AuditMetadata(1L, 1L, LocalDateTime.now().minusDays(2), null, null, null),
                1L, 10L, null, BigDecimal.TEN, BigDecimal.TEN,
                new CostAmount(1L, BigDecimal.ONE, new BigDecimal("5"), new BigDecimal("5")));

        // Layer 2: 10 units @ $6 local
        ValuationLayer layer2 = new ValuationLayer(
                new AuditMetadata(2L, 1L, LocalDateTime.now().minusDays(1), null, null, null),
                1L, 10L, null, BigDecimal.TEN, BigDecimal.TEN,
                new CostAmount(1L, BigDecimal.ONE, new BigDecimal("6"), new BigDecimal("6")));

        when(layerRepository.findAvailableLayers(1L, 10L, BigDecimal.ZERO))
                .thenReturn(List.of(layer1, layer2));

        // Consume 12 units: should take 10 from layer1 + 2 from layer2
        CostAmount result = service.consumeLayers(1L, 10L, null, new BigDecimal("12"));

        // Total cost: 10*5 + 2*6 = 50 + 12 = 62. Average = 62/12 = 5.1667
        assertThat(result.localAmount()).isEqualByComparingTo(new BigDecimal("5.1667"));
        assertThat(layer1.getRemainingQuantity()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(layer2.getRemainingQuantity()).isEqualByComparingTo(new BigDecimal("8"));

        // Both layers saved
        verify(layerRepository, times(2)).save(any(ValuationLayer.class));
    }

    @Test
    @DisplayName("consumeLayers throws when insufficient stock in layers")
    void consumeLayers_insufficientStock_throws() {
        ValuationLayer layer = new ValuationLayer(
                new AuditMetadata(1L, 1L, LocalDateTime.now(), null, null, null),
                1L, 10L, null, BigDecimal.valueOf(5), BigDecimal.valueOf(5),
                new CostAmount(1L, BigDecimal.ONE, new BigDecimal("10"), new BigDecimal("10")));

        when(layerRepository.findAvailableLayers(1L, 10L, BigDecimal.ZERO))
                .thenReturn(List.of(layer));

        assertThatThrownBy(() -> service.consumeLayers(1L, 10L, null, BigDecimal.TEN))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("insufficient_stock");
    }

    @Test
    @DisplayName("consumeLayers uses serial-specific lookup for serialized items")
    void consumeLayers_serialized_usesSerialLookup() {
        String sn = "SN-001";
        ValuationLayer layer = new ValuationLayer(
                new AuditMetadata(1L, 1L, LocalDateTime.now(), null, null, null),
                1L, 10L, sn, BigDecimal.ONE, BigDecimal.ONE,
                new CostAmount(1L, BigDecimal.ONE, new BigDecimal("100"), new BigDecimal("100")));

        when(layerRepository.findAvailableLayersBySerial(1L, 10L, sn, BigDecimal.ZERO))
                .thenReturn(List.of(layer));

        CostAmount result = service.consumeLayers(1L, 10L, sn, BigDecimal.ONE);

        assertThat(result.localAmount()).isEqualByComparingTo(new BigDecimal("100.0000"));
        verify(layerRepository).findAvailableLayersBySerial(1L, 10L, sn, BigDecimal.ZERO);
        verify(layerRepository, never()).findAvailableLayers(any(), any(), any());
    }

    @Test
    @DisplayName("consumeLayers with exact quantity consumes fully")
    void consumeLayers_exactQuantity() {
        ValuationLayer layer = new ValuationLayer(
                new AuditMetadata(1L, 1L, LocalDateTime.now(), null, null, null),
                1L, 10L, null, BigDecimal.TEN, BigDecimal.TEN,
                new CostAmount(1L, BigDecimal.ONE, new BigDecimal("5"), new BigDecimal("5")));

        when(layerRepository.findAvailableLayers(1L, 10L, BigDecimal.ZERO))
                .thenReturn(List.of(layer));

        CostAmount result = service.consumeLayers(1L, 10L, null, BigDecimal.TEN);

        assertThat(result.localAmount()).isEqualByComparingTo(new BigDecimal("5.0000"));
        assertThat(layer.getRemainingQuantity()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("addLayer stores valuation source reference metadata")
    void addLayer_withReferenceMetadata_persistsReference() {
        CostAmount cost = CostAmount.of(1L, new BigDecimal("15000"), new BigDecimal("10"));

        service.addLayer(
                1L,
                10L,
                null,
                BigDecimal.TEN,
                cost,
                ReferenceType.GOODS_RECEIPT,
                100L,
                1001L
        );

        ArgumentCaptor<ValuationLayer> captor = ArgumentCaptor.forClass(ValuationLayer.class);
        verify(layerRepository).save(captor.capture());

        ValuationLayer saved = captor.getValue();
        assertThat(saved.getReferenceType()).isEqualTo(ReferenceType.GOODS_RECEIPT);
        assertThat(saved.getReferenceId()).isEqualTo(100L);
        assertThat(saved.getReferenceLineId()).isEqualTo(1001L);
    }

    @Test
    @DisplayName("consumeSpecificLayers consumes only layers matching valuation reference")
    void consumeSpecificLayers_usesReferenceLookupOnly() {
        ValuationLayer specificLayer = new ValuationLayer(
                new AuditMetadata(1L, 1L, LocalDateTime.now().minusDays(1), null, null, null),
                1L, 10L, null, BigDecimal.TEN, BigDecimal.TEN,
                new CostAmount(1L, BigDecimal.ONE, new BigDecimal("7"), new BigDecimal("7")),
                ReferenceType.GOODS_RECEIPT, 100L, 1001L);

        when(layerRepository.findAvailableLayersByReference(
                1L, 10L, ReferenceType.GOODS_RECEIPT, 100L, 1001L, BigDecimal.ZERO))
                .thenReturn(List.of(specificLayer));

        CostAmount result = service.consumeSpecificLayers(
                1L, 10L, null, ReferenceType.GOODS_RECEIPT, 100L, 1001L, new BigDecimal("4"));

        assertThat(result.localAmount()).isEqualByComparingTo(new BigDecimal("7.0000"));
        assertThat(specificLayer.getRemainingQuantity()).isEqualByComparingTo(new BigDecimal("6"));
        verify(layerRepository).findAvailableLayersByReference(
                1L, 10L, ReferenceType.GOODS_RECEIPT, 100L, 1001L, BigDecimal.ZERO);
        verify(layerRepository, never()).findAvailableLayers(any(), any(), any());
    }

    @Test
    @DisplayName("consumeSpecificLayers throws when matching reference layer is insufficient")
    void consumeSpecificLayers_insufficientMatchingLayer_throws() {
        ValuationLayer specificLayer = new ValuationLayer(
                new AuditMetadata(1L, 1L, LocalDateTime.now(), null, null, null),
                1L, 10L, null, BigDecimal.ONE, BigDecimal.ONE,
                new CostAmount(1L, BigDecimal.ONE, new BigDecimal("7"), new BigDecimal("7")),
                ReferenceType.GOODS_RECEIPT, 100L, 1001L);

        when(layerRepository.findAvailableLayersByReference(
                1L, 10L, ReferenceType.GOODS_RECEIPT, 100L, 1001L, BigDecimal.ZERO))
                .thenReturn(List.of(specificLayer));

        assertThatThrownBy(() -> service.consumeSpecificLayers(
                1L, 10L, null, ReferenceType.GOODS_RECEIPT, 100L, 1001L, new BigDecimal("2")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("insufficient_stock");
    }

    @Test
    @DisplayName("consumeSpecificLayers uses serial and valuation reference for serialized item")
    void consumeSpecificLayers_serialized_usesSerialReferenceLookup() {
        ValuationLayer serialLayer = new ValuationLayer(
                new AuditMetadata(1L, 1L, LocalDateTime.now(), null, null, null),
                1L, 10L, "SN-001", BigDecimal.ONE, BigDecimal.ONE,
                new CostAmount(1L, BigDecimal.ONE, new BigDecimal("50"), new BigDecimal("50")),
                ReferenceType.GOODS_RECEIPT, 100L, 1001L);

        when(layerRepository.findAvailableLayersByReferenceAndSerial(
                1L, 10L, "SN-001", ReferenceType.GOODS_RECEIPT, 100L, 1001L, BigDecimal.ZERO))
                .thenReturn(List.of(serialLayer));

        CostAmount result = service.consumeSpecificLayers(
                1L, 10L, "SN-001", ReferenceType.GOODS_RECEIPT, 100L, 1001L, BigDecimal.ONE);

        assertThat(result.localAmount()).isEqualByComparingTo(new BigDecimal("50.0000"));
        verify(layerRepository).findAvailableLayersByReferenceAndSerial(
                1L, 10L, "SN-001", ReferenceType.GOODS_RECEIPT, 100L, 1001L, BigDecimal.ZERO);
        verify(layerRepository, never()).findAvailableLayersBySerial(any(), any(), any(), any());
    }
}
