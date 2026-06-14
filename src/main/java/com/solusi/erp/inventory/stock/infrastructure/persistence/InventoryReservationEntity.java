package com.solusi.erp.inventory.stock.infrastructure.persistence;

import com.solusi.erp.core.model.BaseModel;
import com.solusi.erp.inventory.stock.domain.model.InventoryReservationStatus;
import com.solusi.erp.inventory.stock.domain.model.ReferenceType;
import com.solusi.erp.inventory.stock.domain.model.ReservationOwnerType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "inv_stock_reservations")
@Getter
@Setter
public class InventoryReservationEntity extends BaseModel {

    @Enumerated(EnumType.STRING)
    @Column(name = "owner_ref_type", nullable = false, length = 50)
    private ReservationOwnerType ownerType;

    @Column(name = "owner_ref_id", nullable = false)
    private Long ownerId;

    @Column(name = "owner_ref_code", nullable = false, length = 60)
    private String ownerCode;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "facility_id", nullable = false)
    private Long facilityId;

    @Column(name = "grid_id", nullable = false)
    private Long gridId;

    @Column(name = "container_id", nullable = false)
    private Long containerId;

    @Column(name = "serial_number", length = 100)
    private String serialNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "valuation_ref_type", nullable = false, length = 50)
    private ReferenceType valuationReferenceType;

    @Column(name = "valuation_ref_id", nullable = false)
    private Long valuationReferenceId;

    @Column(name = "valuation_ref_line_id", nullable = false)
    private Long valuationReferenceLineId;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private InventoryReservationStatus status;
}
