package com.solusi.erp.inventory.stock.infrastructure.service;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.core.model.CurrencyAmount;
import com.solusi.erp.inventory.container.infrastructure.persistence.ContainerEntity;
import com.solusi.erp.inventory.container.infrastructure.persistence.ContainerJpaRepository;
import com.solusi.erp.inventory.grid.infrastructure.persistence.GridEntity;
import com.solusi.erp.inventory.grid.infrastructure.persistence.GridJpaRepository;
import com.solusi.erp.inventory.stock.application.dto.StockMovementPayload;
import com.solusi.erp.inventory.stock.domain.model.MovementType;
import com.solusi.erp.inventory.stock.domain.model.ReferenceType;
import com.solusi.erp.inventory.stock.domain.model.StockBalance;
import com.solusi.erp.inventory.stock.domain.model.StockMovementReversalRequest;
import com.solusi.erp.inventory.stock.domain.port.StockService;
import com.solusi.erp.inventory.stock.domain.repository.StockBalanceRepository;
import com.solusi.erp.inventory.stock.infrastructure.persistence.InventoryMovementEntity;
import com.solusi.erp.inventory.stock.infrastructure.persistence.InventoryMovementJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockMovementReversalServiceTest {

    @Mock
    private InventoryMovementJpaRepository movementRepository;

    @Mock
    private ContainerJpaRepository containerRepository;

    @Mock
    private GridJpaRepository gridRepository;

    @Mock
    private StockBalanceRepository stockBalanceRepository;

    @Mock
    private StockService stockService;

    private StockMovementReversalServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new StockMovementReversalServiceImpl(
                movementRepository,
                containerRepository,
                gridRepository,
                stockBalanceRepository,
                stockService);
    }

    @Test
    void reversesOutboundMovementAsLinkedReceipt() {
        InventoryMovementEntity original = issueMovement(10L, 1L, "SN-001");
        stubOriginal(original);
        stubSameFacility(1L, 2L);
        when(stockBalanceRepository.findByProductContainerSerial(100L, 2L, "SN-001"))
                .thenReturn(Optional.empty());

        service.reverse(List.of(request(10L, 2L)));

        ArgumentCaptor<StockMovementPayload> captor = ArgumentCaptor.forClass(StockMovementPayload.class);
        verify(stockService).adjust(captor.capture());
        StockMovementPayload payload = captor.getValue();
        assertThat(payload.getMovementType()).isEqualTo(MovementType.RECEIPT);
        assertThat(payload.getQuantity()).isEqualByComparingTo("4.0000");
        assertThat(payload.getProductId()).isEqualTo(100L);
        assertThat(payload.getContainerId()).isEqualTo(2L);
        assertThat(payload.getSerialNumber()).isEqualTo("SN-001");
        assertThat(payload.getReferenceType()).isEqualTo(ReferenceType.GOODS_ISSUE);
        assertThat(payload.getReferenceId()).isEqualTo(500L);
        assertThat(payload.getReferenceCode()).isEqualTo("GI-001");
        assertThat(payload.getCurrencyId()).isEqualTo(77L);
        assertThat(payload.getExchangeRate()).isEqualByComparingTo("15000");
        assertThat(payload.getNetPrice()).isEqualByComparingTo("10.00");
        assertThat(payload.getTransactionDate()).isEqualTo(LocalDate.of(2026, 6, 4).atStartOfDay());
        assertThat(payload.getReversalOfMovementId()).isEqualTo(10L);
    }

    @Test
    void rejectsAlreadyReversedMovement() {
        InventoryMovementEntity original = issueMovement(10L, 1L, null);
        when(movementRepository.findById(10L)).thenReturn(Optional.of(original));
        when(movementRepository.existsByReversalOfMovementId(10L)).thenReturn(true);

        assertThatThrownBy(() -> service.reverse(List.of(request(10L, 2L))))
                .isInstanceOf(DomainException.class)
                .hasMessage("msg.error.stock.reversal.already.reversed");

        verify(stockService, never()).adjust(any());
    }

    @Test
    void rejectsTargetContainerInDifferentFacility() {
        InventoryMovementEntity original = issueMovement(10L, 1L, null);
        stubOriginal(original);
        stubContainers(1L, 2L);
        when(gridRepository.findById(11L)).thenReturn(Optional.of(grid(11L, 5L)));
        when(gridRepository.findById(22L)).thenReturn(Optional.of(grid(22L, 6L)));

        assertThatThrownBy(() -> service.reverse(List.of(request(10L, 2L))))
                .isInstanceOf(DomainException.class)
                .hasMessage("msg.error.stock.reversal.facility.mismatch");

        verify(stockService, never()).adjust(any());
    }

    @Test
    void rejectsSerializedStockAlreadyOnHandAtTarget() {
        InventoryMovementEntity original = issueMovement(10L, 1L, "SN-001");
        stubOriginal(original);
        stubSameFacility(1L, 2L);
        StockBalance onHand = StockBalance.createNew(100L, 2L, "SN-001");
        onHand.applyMovement(MovementType.RECEIPT, BigDecimal.ONE);
        when(stockBalanceRepository.findByProductContainerSerial(100L, 2L, "SN-001"))
                .thenReturn(Optional.of(onHand));

        assertThatThrownBy(() -> service.reverse(List.of(request(10L, 2L))))
                .isInstanceOf(DomainException.class)
                .hasMessage("msg.error.stock.reversal.serial.on.hand");

        verify(stockService, never()).adjust(any());
    }

    @Test
    void rejectsNonOutboundMovement() {
        InventoryMovementEntity original = issueMovement(10L, 1L, null);
        original.setMovementType(MovementType.RECEIPT);
        when(movementRepository.findById(10L)).thenReturn(Optional.of(original));

        assertThatThrownBy(() -> service.reverse(List.of(request(10L, 2L))))
                .isInstanceOf(DomainException.class)
                .hasMessage("msg.error.stock.reversal.outbound.required");

        verify(stockService, never()).adjust(any());
    }

    @Test
    void translatesDuplicateConstraintRace() {
        InventoryMovementEntity original = issueMovement(10L, 1L, null);
        stubOriginal(original);
        stubSameFacility(1L, 2L);
        doThrow(new DataIntegrityViolationException("duplicate"))
                .when(stockService).adjust(any(StockMovementPayload.class));

        assertThatThrownBy(() -> service.reverse(List.of(request(10L, 2L))))
                .isInstanceOf(DomainException.class)
                .hasMessage("msg.error.stock.reversal.already.reversed");
    }

    @Test
    void rejectsMissingReversalDate() {
        assertThatThrownBy(() -> service.reverse(List.of(new StockMovementReversalRequest(10L, 2L, null, null))))
                .isInstanceOf(DomainException.class)
                .hasMessage("msg.error.stock.reversal.date.required");
    }

    private void stubOriginal(InventoryMovementEntity original) {
        when(movementRepository.findById(original.getId())).thenReturn(Optional.of(original));
        when(movementRepository.existsByReversalOfMovementId(original.getId())).thenReturn(false);
    }

    private void stubSameFacility(Long originalContainerId, Long targetContainerId) {
        stubContainers(originalContainerId, targetContainerId);
        when(gridRepository.findById(11L)).thenReturn(Optional.of(grid(11L, 5L)));
        when(gridRepository.findById(22L)).thenReturn(Optional.of(grid(22L, 5L)));
    }

    private void stubContainers(Long originalContainerId, Long targetContainerId) {
        when(containerRepository.findById(originalContainerId)).thenReturn(Optional.of(container(originalContainerId, 11L, true)));
        when(containerRepository.findById(targetContainerId)).thenReturn(Optional.of(container(targetContainerId, 22L, true)));
    }

    private StockMovementReversalRequest request(Long originalMovementId, Long targetContainerId) {
        return new StockMovementReversalRequest(
                originalMovementId,
                targetContainerId,
                LocalDate.of(2026, 6, 4),
                "cancel");
    }

    private InventoryMovementEntity issueMovement(Long id, Long containerId, String serialNumber) {
        InventoryMovementEntity entity = new InventoryMovementEntity();
        entity.setId(id);
        entity.setProductId(100L);
        entity.setContainerId(containerId);
        entity.setSerialNumber(serialNumber);
        entity.setQuantity(new BigDecimal("4.0000"));
        entity.setMovementType(MovementType.ISSUE);
        entity.setReferenceType(ReferenceType.GOODS_ISSUE);
        entity.setReferenceId(500L);
        entity.setReferenceCode("GI-001");
        entity.setUnitCost(CurrencyAmount.builder()
                .currencyId(77L)
                .exchangeRate(new BigDecimal("15000"))
                .originalAmount(new BigDecimal("10.00"))
                .localAmount(new BigDecimal("150000.00"))
                .build());
        return entity;
    }

    private ContainerEntity container(Long id, Long gridId, boolean active) {
        ContainerEntity entity = new ContainerEntity();
        entity.setId(id);
        entity.setGridId(gridId);
        entity.setIsActive(active);
        return entity;
    }

    private GridEntity grid(Long id, Long facilityId) {
        GridEntity entity = new GridEntity();
        entity.setId(id);
        entity.setFacilityId(facilityId);
        return entity;
    }
}
