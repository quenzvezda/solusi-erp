package com.solusi.erp.inventory.stock.infrastructure.service;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.inventory.stock.application.dto.StockMovementPayload;
import com.solusi.erp.inventory.stock.domain.model.InventoryReservation;
import com.solusi.erp.inventory.stock.domain.model.InventoryReservationRequest;
import com.solusi.erp.inventory.stock.domain.model.MovementType;
import com.solusi.erp.inventory.stock.domain.model.ReferenceType;
import com.solusi.erp.inventory.stock.domain.model.ReservationOwnerType;
import com.solusi.erp.inventory.stock.domain.port.InventoryReservationService;
import com.solusi.erp.inventory.stock.domain.port.StockService;
import com.solusi.erp.inventory.stock.domain.repository.InventoryReservationRepository;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class InventoryReservationServiceImpl implements InventoryReservationService {

    private final InventoryReservationRepository reservationRepository;
    private final StockService stockService;

    public InventoryReservationServiceImpl(InventoryReservationRepository reservationRepository,
                                           StockService stockService) {
        this.reservationRepository = reservationRepository;
        this.stockService = stockService;
    }

    @Override
    public void reserve(ReservationOwnerType ownerType, Long ownerId, String ownerCode,
                        List<InventoryReservationRequest> requests) {
        if (reservationRepository.existsActiveByOwner(ownerType, ownerId)) {
            throw new DomainException("msg.error.inventory.reservation.owner_already_reserved");
        }
        if (requests == null || requests.isEmpty()) {
            throw new DomainException("msg.error.inventory.reservation.requests_required");
        }

        Set<SerialKey> serials = new HashSet<>();
        List<InventoryReservation> reservations = requests.stream()
                .map(request -> {
                    assertSerialAvailable(request, serials);
                    return InventoryReservation.createActive(ownerType, ownerId, ownerCode, request);
                })
                .toList();

        requests.forEach(request -> stockService.adjust(toPayload(
                ownerId, ownerCode, request, MovementType.RESERVE)));
        reservationRepository.saveAll(reservations);
    }

    @Override
    public void release(ReservationOwnerType ownerType, Long ownerId) {
        List<InventoryReservation> reservations = requireActiveReservations(ownerType, ownerId);
        reservations.forEach(reservation -> stockService.adjust(toPayload(
                ownerId, reservation.getOwnerCode(), toRequest(reservation), MovementType.RELEASE)));
        reservations.forEach(InventoryReservation::release);
        reservationRepository.saveAll(reservations);
    }

    @Override
    public void assertActiveCoverage(ReservationOwnerType ownerType, Long ownerId,
                                     List<InventoryReservationRequest> requests) {
        List<InventoryReservation> reservations = requireActiveReservations(ownerType, ownerId);
        List<CoverageKey> actual = reservations.stream().map(this::toCoverageKey).sorted().toList();
        List<CoverageKey> expected = requests == null
                ? List.of()
                : requests.stream().map(this::toCoverageKey).sorted().toList();
        if (!actual.equals(expected)) {
            throw new DomainException("msg.error.inventory.reservation.coverage_mismatch");
        }
    }

    @Override
    public void consume(ReservationOwnerType ownerType, Long ownerId) {
        List<InventoryReservation> reservations = requireActiveReservations(ownerType, ownerId);
        reservations.forEach(InventoryReservation::consume);
        reservationRepository.saveAll(reservations);
    }

    private void assertSerialAvailable(InventoryReservationRequest request, Set<SerialKey> serials) {
        if (request.serialNumber() == null || request.serialNumber().isBlank()) {
            return;
        }
        SerialKey key = new SerialKey(request.productId(), request.serialNumber());
        if (!serials.add(key) || reservationRepository.existsActiveBySerial(key.productId(), key.serialNumber())) {
            throw new DomainException("msg.error.inventory.reservation.serial_already_reserved");
        }
    }

    private List<InventoryReservation> requireActiveReservations(ReservationOwnerType ownerType, Long ownerId) {
        List<InventoryReservation> reservations = reservationRepository.findActiveByOwner(ownerType, ownerId);
        if (reservations.isEmpty()) {
            throw new DomainException("msg.error.inventory.reservation.active_not_found");
        }
        return reservations;
    }

    private StockMovementPayload toPayload(Long ownerId, String ownerCode,
                                           InventoryReservationRequest request, MovementType movementType) {
        return StockMovementPayload.builder()
                .productId(request.productId())
                .containerId(request.containerId())
                .serialNumber(request.serialNumber())
                .quantity(request.quantity())
                .movementType(movementType)
                .referenceType(ReferenceType.PURCHASE_RETURN)
                .referenceId(ownerId)
                .referenceCode(ownerCode)
                .valuationReferenceType(request.valuationReferenceType())
                .valuationReferenceId(request.valuationReferenceId())
                .valuationReferenceLineId(request.valuationReferenceLineId())
                .build();
    }

    private InventoryReservationRequest toRequest(InventoryReservation reservation) {
        return new InventoryReservationRequest(
                reservation.getProductId(),
                reservation.getFacilityId(),
                reservation.getGridId(),
                reservation.getContainerId(),
                reservation.getSerialNumber() != null,
                reservation.getSerialNumber(),
                reservation.getValuationReferenceType(),
                reservation.getValuationReferenceId(),
                reservation.getValuationReferenceLineId(),
                reservation.getQuantity()
        );
    }

    private CoverageKey toCoverageKey(InventoryReservation reservation) {
        return toCoverageKey(toRequest(reservation));
    }

    private CoverageKey toCoverageKey(InventoryReservationRequest request) {
        return new CoverageKey(
                request.productId(),
                request.facilityId(),
                request.gridId(),
                request.containerId(),
                request.serialNumber(),
                request.valuationReferenceType(),
                request.valuationReferenceId(),
                request.valuationReferenceLineId(),
                request.quantity().stripTrailingZeros()
        );
    }

    private record SerialKey(Long productId, String serialNumber) {
    }

    private record CoverageKey(Long productId,
                               Long facilityId,
                               Long gridId,
                               Long containerId,
                               String serialNumber,
                               ReferenceType valuationReferenceType,
                               Long valuationReferenceId,
                               Long valuationReferenceLineId,
                               BigDecimal quantity) implements Comparable<CoverageKey> {

        @Override
        public int compareTo(CoverageKey other) {
            return toString().compareTo(other.toString());
        }
    }
}
