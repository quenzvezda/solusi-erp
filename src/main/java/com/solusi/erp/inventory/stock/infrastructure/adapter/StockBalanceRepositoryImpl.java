package com.solusi.erp.inventory.stock.infrastructure.adapter;

import com.solusi.erp.inventory.stock.domain.model.StockBalance;
import com.solusi.erp.inventory.stock.domain.repository.StockBalanceRepository;
import com.solusi.erp.inventory.stock.infrastructure.persistence.StockBalanceEntity;
import com.solusi.erp.inventory.stock.infrastructure.persistence.StockBalanceJpaRepository;
import com.solusi.erp.inventory.stock.infrastructure.persistence.StockBalancePersistenceMapper;

import java.util.Optional;

/**
 * Infrastructure adapter bridging domain StockBalanceRepository to JPA.
 */
public class StockBalanceRepositoryImpl implements StockBalanceRepository {

    private final StockBalanceJpaRepository jpaRepository;
    private final StockBalancePersistenceMapper mapper;

    public StockBalanceRepositoryImpl(StockBalanceJpaRepository jpaRepository,
                                      StockBalancePersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public StockBalance save(StockBalance domain) {
        StockBalanceEntity entity = mapper.toEntity(domain);
        StockBalanceEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<StockBalance> findByProductContainerSerial(Long productId, Long containerId, String serialNumber) {
        return jpaRepository.findByProductIdAndContainerIdAndSerialNumber(productId, containerId, serialNumber)
                .map(mapper::toDomain);
    }
}
