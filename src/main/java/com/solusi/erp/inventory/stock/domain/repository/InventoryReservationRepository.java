package com.solusi.erp.inventory.stock.domain.repository;

import com.solusi.erp.inventory.stock.domain.model.InventoryReservation;
import com.solusi.erp.inventory.stock.domain.model.ReservationOwnerType;

import java.util.List;

public interface InventoryReservationRepository {

    List<InventoryReservation> saveAll(List<InventoryReservation> reservations);

    List<InventoryReservation> findActiveByOwner(ReservationOwnerType ownerType, Long ownerId);

    boolean existsActiveByOwner(ReservationOwnerType ownerType, Long ownerId);

    boolean existsActiveBySerial(Long productId, String serialNumber);
}
