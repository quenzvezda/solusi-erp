package com.solusi.erp.inventory.stock.infrastructure.service;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.inventory.stock.application.dto.StockMovementPayload;
import com.solusi.erp.inventory.stock.domain.model.InventoryReservation;
import com.solusi.erp.inventory.stock.domain.model.InventoryReservationRequest;
import com.solusi.erp.inventory.stock.domain.model.InventoryReservationStatus;
import com.solusi.erp.inventory.stock.domain.model.MovementType;
import com.solusi.erp.inventory.stock.domain.model.ReferenceType;
import com.solusi.erp.inventory.stock.domain.model.ReservationOwnerType;
import com.solusi.erp.inventory.stock.domain.port.StockService;
import com.solusi.erp.inventory.stock.domain.repository.InventoryReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryReservationServiceTest {

    @Mock
    private InventoryReservationRepository reservationRepository;

    @Mock
    private StockService stockService;

    private InventoryReservationServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new InventoryReservationServiceImpl(reservationRepository, stockService);
    }

    @Test
    void reserve_allMovementsSucceed_persistsActiveOwnershipRows() {
        List<InventoryReservationRequest> requests = List.of(request(40L, null, BigDecimal.TEN));

        service.reserve(ReservationOwnerType.PURCHASE_RETURN, 100L, "PRT-001", requests);

        ArgumentCaptor<StockMovementPayload> payloadCaptor = ArgumentCaptor.forClass(StockMovementPayload.class);
        verify(stockService).adjust(payloadCaptor.capture());
        assertThat(payloadCaptor.getValue().getMovementType()).isEqualTo(MovementType.RESERVE);
        assertThat(payloadCaptor.getValue().getReferenceType()).isEqualTo(ReferenceType.PURCHASE_RETURN);
        assertThat(payloadCaptor.getValue().getReferenceCode()).isEqualTo("PRT-001");
        verify(reservationRepository).saveAll(any());
    }

    @Test
    void reserve_ownerAlreadyHasActiveReservation_rejectsSecondReserve() {
        when(reservationRepository.existsActiveByOwner(ReservationOwnerType.PURCHASE_RETURN, 100L)).thenReturn(true);

        assertThatThrownBy(() -> service.reserve(
                ReservationOwnerType.PURCHASE_RETURN, 100L, "PRT-001",
                List.of(request(40L, null, BigDecimal.ONE))))
                .isInstanceOf(DomainException.class)
                .hasMessage("msg.error.inventory.reservation.owner_already_reserved");

        verify(stockService, never()).adjust(any());
    }

    @Test
    void reserve_partialMovementFailure_doesNotPersistOwnershipRows() {
        List<InventoryReservationRequest> requests = List.of(
                request(40L, null, BigDecimal.ONE),
                request(41L, null, BigDecimal.ONE)
        );
        org.mockito.Mockito.doNothing()
                .doThrow(new RuntimeException("insufficient"))
                .when(stockService).adjust(any(StockMovementPayload.class));

        assertThatThrownBy(() -> service.reserve(
                ReservationOwnerType.PURCHASE_RETURN, 100L, "PRT-001", requests))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("insufficient");

        verify(reservationRepository, never()).saveAll(any());
    }

    @Test
    void reserve_duplicateSerialInRequest_rejectsBeforeMovements() {
        List<InventoryReservationRequest> requests = List.of(
                request(40L, "SER-001", BigDecimal.ONE),
                request(40L, "SER-001", BigDecimal.ONE)
        );

        assertThatThrownBy(() -> service.reserve(
                ReservationOwnerType.PURCHASE_RETURN, 100L, "PRT-001", requests))
                .isInstanceOf(DomainException.class)
                .hasMessage("msg.error.inventory.reservation.serial_already_reserved");

        verify(stockService, never()).adjust(any());
    }

    @Test
    void release_activeRows_releasesBalanceAndOwnership() {
        InventoryReservation reservation = reservation(request(40L, null, BigDecimal.TEN));
        when(reservationRepository.findActiveByOwner(ReservationOwnerType.PURCHASE_RETURN, 100L))
                .thenReturn(List.of(reservation));

        service.release(ReservationOwnerType.PURCHASE_RETURN, 100L);

        verify(stockService).adjust(any(StockMovementPayload.class));
        assertThat(reservation.getStatus()).isEqualTo(InventoryReservationStatus.RELEASED);
        verify(reservationRepository).saveAll(List.of(reservation));
    }

    @Test
    void assertActiveCoverage_mismatchedQuantity_rejects() {
        InventoryReservation reservation = reservation(request(40L, null, BigDecimal.TEN));
        when(reservationRepository.findActiveByOwner(ReservationOwnerType.PURCHASE_RETURN, 100L))
                .thenReturn(List.of(reservation));

        assertThatThrownBy(() -> service.assertActiveCoverage(
                ReservationOwnerType.PURCHASE_RETURN, 100L,
                List.of(request(40L, null, BigDecimal.ONE))))
                .isInstanceOf(DomainException.class)
                .hasMessage("msg.error.inventory.reservation.coverage_mismatch");
    }

    @Test
    void consume_activeRows_marksOwnershipConsumed() {
        InventoryReservation reservation = reservation(request(40L, null, BigDecimal.TEN));
        when(reservationRepository.findActiveByOwner(ReservationOwnerType.PURCHASE_RETURN, 100L))
                .thenReturn(List.of(reservation));

        service.consume(ReservationOwnerType.PURCHASE_RETURN, 100L);

        assertThat(reservation.getStatus()).isEqualTo(InventoryReservationStatus.CONSUMED);
        verify(reservationRepository).saveAll(List.of(reservation));
    }

    private InventoryReservation reservation(InventoryReservationRequest request) {
        return InventoryReservation.createActive(
                ReservationOwnerType.PURCHASE_RETURN, 100L, "PRT-001", request);
    }

    private InventoryReservationRequest request(Long containerId, String serialNumber, BigDecimal quantity) {
        return new InventoryReservationRequest(
                10L,
                20L,
                30L,
                containerId,
                serialNumber != null,
                serialNumber,
                ReferenceType.GOODS_RECEIPT,
                50L,
                60L,
                quantity
        );
    }
}
