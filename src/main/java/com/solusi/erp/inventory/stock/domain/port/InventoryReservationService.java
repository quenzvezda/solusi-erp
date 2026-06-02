package com.solusi.erp.inventory.stock.domain.port;

import com.solusi.erp.inventory.stock.domain.model.InventoryReservationRequest;
import com.solusi.erp.inventory.stock.domain.model.ReservationOwnerType;

import java.util.List;

public interface InventoryReservationService {

    void reserve(ReservationOwnerType ownerType, Long ownerId, String ownerCode,
                 List<InventoryReservationRequest> requests);

    void release(ReservationOwnerType ownerType, Long ownerId);

    void assertActiveCoverage(ReservationOwnerType ownerType, Long ownerId,
                              List<InventoryReservationRequest> requests);

    void consume(ReservationOwnerType ownerType, Long ownerId);
}
