package com.solusi.erp.inventory.stock.infrastructure.persistence;

import com.solusi.erp.inventory.stock.domain.model.InventoryReservationStatus;
import com.solusi.erp.inventory.stock.domain.model.ReservationOwnerType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryReservationJpaRepository extends JpaRepository<InventoryReservationEntity, Long> {

    List<InventoryReservationEntity> findByOwnerTypeAndOwnerIdAndStatus(
            ReservationOwnerType ownerType, Long ownerId, InventoryReservationStatus status);

    boolean existsByOwnerTypeAndOwnerIdAndStatus(
            ReservationOwnerType ownerType, Long ownerId, InventoryReservationStatus status);

    boolean existsByProductIdAndSerialNumberAndStatus(
            Long productId, String serialNumber, InventoryReservationStatus status);
}
