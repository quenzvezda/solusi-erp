package com.solusi.erp.inventory.adjustment.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockAdjustmentLineJpaRepository extends JpaRepository<StockAdjustmentLineEntity, Long> {

    List<StockAdjustmentLineEntity> findByHeaderId(Long headerId);
    boolean existsByGridId(Long gridId);
    boolean existsByContainerId(Long containerId);
}
