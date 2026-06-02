package com.solusi.erp.inventory.stock.infrastructure.adapter;

import com.solusi.erp.inventory.stock.domain.model.InventoryReservation;
import com.solusi.erp.inventory.stock.domain.model.InventoryReservationStatus;
import com.solusi.erp.inventory.stock.domain.model.ReservationOwnerType;
import com.solusi.erp.inventory.stock.domain.repository.InventoryReservationRepository;
import com.solusi.erp.inventory.stock.infrastructure.persistence.InventoryReservationJpaRepository;
import com.solusi.erp.inventory.stock.infrastructure.persistence.InventoryReservationPersistenceMapper;

import java.util.List;

public class InventoryReservationRepositoryImpl implements InventoryReservationRepository {

    private final InventoryReservationJpaRepository jpaRepository;
    private final InventoryReservationPersistenceMapper mapper;

    public InventoryReservationRepositoryImpl(InventoryReservationJpaRepository jpaRepository,
                                              InventoryReservationPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public List<InventoryReservation> saveAll(List<InventoryReservation> reservations) {
        return jpaRepository.saveAll(reservations.stream().map(mapper::toEntity).toList())
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<InventoryReservation> findActiveByOwner(ReservationOwnerType ownerType, Long ownerId) {
        return jpaRepository.findByOwnerTypeAndOwnerIdAndStatus(ownerType, ownerId, InventoryReservationStatus.ACTIVE)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsActiveByOwner(ReservationOwnerType ownerType, Long ownerId) {
        return jpaRepository.existsByOwnerTypeAndOwnerIdAndStatus(ownerType, ownerId, InventoryReservationStatus.ACTIVE);
    }

    @Override
    public boolean existsActiveBySerial(Long productId, String serialNumber) {
        return jpaRepository.existsByProductIdAndSerialNumberAndStatus(
                productId, serialNumber, InventoryReservationStatus.ACTIVE);
    }
}
