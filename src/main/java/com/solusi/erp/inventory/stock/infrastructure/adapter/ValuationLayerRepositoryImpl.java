package com.solusi.erp.inventory.stock.infrastructure.adapter;

import com.solusi.erp.inventory.stock.domain.model.ReferenceType;
import com.solusi.erp.inventory.stock.domain.model.ValuationLayer;
import com.solusi.erp.inventory.stock.domain.repository.ValuationLayerRepository;
import com.solusi.erp.inventory.stock.infrastructure.persistence.ValuationLayerEntity;
import com.solusi.erp.inventory.stock.infrastructure.persistence.ValuationLayerJpaRepository;
import com.solusi.erp.inventory.stock.infrastructure.persistence.ValuationLayerPersistenceMapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Infrastructure adapter bridging domain ValuationLayerRepository to JPA.
 */
public class ValuationLayerRepositoryImpl implements ValuationLayerRepository {

    private final ValuationLayerJpaRepository jpaRepository;
    private final ValuationLayerPersistenceMapper mapper;

    public ValuationLayerRepositoryImpl(ValuationLayerJpaRepository jpaRepository,
                                        ValuationLayerPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public void save(ValuationLayer domain) {
        ValuationLayerEntity entity = mapper.toEntity(domain);
        jpaRepository.save(entity);
    }

    @Override
    public List<ValuationLayer> findAvailableLayers(Long productId, Long containerId, BigDecimal minQuantity) {
        return jpaRepository
                .findByProductIdAndContainerIdAndRemainingQuantityGreaterThanOrderByCreatedDateAsc(
                        productId, containerId, minQuantity)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ValuationLayer> findAvailableLayersBySerial(Long productId, Long containerId,
                                                             String serialNumber, BigDecimal minQuantity) {
        return jpaRepository
                .findByProductIdAndContainerIdAndSerialNumberAndRemainingQuantityGreaterThan(
                        productId, containerId, serialNumber, minQuantity)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ValuationLayer> findAvailableLayersByReference(Long productId, Long containerId,
                                                               ReferenceType referenceType, Long referenceId,
                                                               Long referenceLineId, BigDecimal minQuantity) {
        return jpaRepository
                .findByProductIdAndContainerIdAndReferenceTypeAndReferenceIdAndReferenceLineIdAndRemainingQuantityGreaterThanOrderByCreatedDateAsc(
                        productId, containerId, referenceType, referenceId, referenceLineId, minQuantity)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ValuationLayer> findAvailableLayersByReferenceAndSerial(Long productId, Long containerId,
                                                                        String serialNumber,
                                                                        ReferenceType referenceType, Long referenceId,
                                                                        Long referenceLineId, BigDecimal minQuantity) {
        return jpaRepository
                .findByProductIdAndContainerIdAndSerialNumberAndReferenceTypeAndReferenceIdAndReferenceLineIdAndRemainingQuantityGreaterThanOrderByCreatedDateAsc(
                        productId, containerId, serialNumber, referenceType, referenceId, referenceLineId, minQuantity)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ValuationLayer> findByReversalOfMovementId(Long reversalOfMovementId) {
        return jpaRepository.findByReversalOfMovementId(reversalOfMovementId)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
