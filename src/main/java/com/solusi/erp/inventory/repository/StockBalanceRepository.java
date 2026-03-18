package com.solusi.erp.inventory.repository;

import com.solusi.erp.inventory.model.StockBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Stock Balance.
 */
@Repository
public interface StockBalanceRepository extends JpaRepository<StockBalance, Long> {
    
    Optional<StockBalance> findByProductIdAndContainerIdAndSerialNumber(Long productId, Long containerId, String serialNumber);
}
