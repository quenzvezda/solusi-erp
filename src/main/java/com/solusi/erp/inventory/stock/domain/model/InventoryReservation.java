package com.solusi.erp.inventory.stock.domain.model;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;

import java.math.BigDecimal;

public class InventoryReservation {

    private final AuditMetadata metadata;
    private final ReservationOwnerType ownerType;
    private final Long ownerId;
    private final String ownerCode;
    private final Long productId;
    private final Long facilityId;
    private final Long gridId;
    private final Long containerId;
    private final String serialNumber;
    private final ReferenceType valuationReferenceType;
    private final Long valuationReferenceId;
    private final Long valuationReferenceLineId;
    private final BigDecimal quantity;
    private InventoryReservationStatus status;

    public InventoryReservation(AuditMetadata metadata,
                                ReservationOwnerType ownerType,
                                Long ownerId,
                                String ownerCode,
                                Long productId,
                                Long facilityId,
                                Long gridId,
                                Long containerId,
                                String serialNumber,
                                ReferenceType valuationReferenceType,
                                Long valuationReferenceId,
                                Long valuationReferenceLineId,
                                BigDecimal quantity,
                                InventoryReservationStatus status) {
        this.metadata = metadata;
        this.ownerType = ownerType;
        this.ownerId = ownerId;
        this.ownerCode = ownerCode;
        this.productId = productId;
        this.facilityId = facilityId;
        this.gridId = gridId;
        this.containerId = containerId;
        this.serialNumber = serialNumber;
        this.valuationReferenceType = valuationReferenceType;
        this.valuationReferenceId = valuationReferenceId;
        this.valuationReferenceLineId = valuationReferenceLineId;
        this.quantity = quantity;
        this.status = status;
    }

    public static InventoryReservation createActive(ReservationOwnerType ownerType,
                                                    Long ownerId,
                                                    String ownerCode,
                                                    InventoryReservationRequest request) {
        require(ownerType, "msg.error.inventory.reservation.owner_type_required");
        require(ownerId, "msg.error.inventory.reservation.owner_id_required");
        requireText(ownerCode, "msg.error.inventory.reservation.owner_code_required");
        require(request, "msg.error.inventory.reservation.request_required");
        require(request.productId(), "msg.error.inventory.reservation.product_required");
        require(request.facilityId(), "msg.error.inventory.reservation.facility_required");
        require(request.gridId(), "msg.error.inventory.reservation.grid_required");
        require(request.containerId(), "msg.error.inventory.reservation.container_required");
        require(request.valuationReferenceType(), "msg.error.inventory.reservation.valuation_reference_required");
        require(request.valuationReferenceId(), "msg.error.inventory.reservation.valuation_reference_required");
        require(request.valuationReferenceLineId(), "msg.error.inventory.reservation.valuation_reference_required");

        if (request.quantity() == null || request.quantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainException("msg.error.inventory.reservation.quantity_positive");
        }
        if (request.serialized() && isBlank(request.serialNumber())) {
            throw new DomainException("msg.error.inventory.reservation.serial_required");
        }
        if (request.serialized() && request.quantity().compareTo(BigDecimal.ONE) != 0) {
            throw new DomainException("msg.error.inventory.reservation.serial_quantity_one");
        }

        return new InventoryReservation(
                AuditMetadata.empty(),
                ownerType,
                ownerId,
                ownerCode,
                request.productId(),
                request.facilityId(),
                request.gridId(),
                request.containerId(),
                request.serialNumber(),
                request.valuationReferenceType(),
                request.valuationReferenceId(),
                request.valuationReferenceLineId(),
                request.quantity(),
                InventoryReservationStatus.ACTIVE
        );
    }

    public void release() {
        requireActive();
        status = InventoryReservationStatus.RELEASED;
    }

    public void consume() {
        requireActive();
        status = InventoryReservationStatus.CONSUMED;
    }

    private void requireActive() {
        if (status != InventoryReservationStatus.ACTIVE) {
            throw new DomainException("msg.error.inventory.reservation.not_active");
        }
    }

    private static void require(Object value, String key) {
        if (value == null) {
            throw new DomainException(key);
        }
    }

    private static void requireText(String value, String key) {
        if (isBlank(value)) {
            throw new DomainException(key);
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public Long getId() { return metadata.id(); }
    public AuditMetadata getMetadata() { return metadata; }
    public ReservationOwnerType getOwnerType() { return ownerType; }
    public Long getOwnerId() { return ownerId; }
    public String getOwnerCode() { return ownerCode; }
    public Long getProductId() { return productId; }
    public Long getFacilityId() { return facilityId; }
    public Long getGridId() { return gridId; }
    public Long getContainerId() { return containerId; }
    public String getSerialNumber() { return serialNumber; }
    public ReferenceType getValuationReferenceType() { return valuationReferenceType; }
    public Long getValuationReferenceId() { return valuationReferenceId; }
    public Long getValuationReferenceLineId() { return valuationReferenceLineId; }
    public BigDecimal getQuantity() { return quantity; }
    public InventoryReservationStatus getStatus() { return status; }

}
