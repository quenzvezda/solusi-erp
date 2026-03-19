package com.solusi.erp.inventory.repository;

import com.solusi.erp.inventory.dto.LocationStockDetailResponse;
import com.solusi.erp.inventory.dto.ProductStockSummaryResponse;
import com.solusi.erp.inventory.model.StockBalance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Stock Balance.
 */
@Repository
public interface StockBalanceRepository extends JpaRepository<StockBalance, Long> {
    
    Optional<StockBalance> findByProductIdAndContainerIdAndSerialNumber(Long productId, Long containerId, String serialNumber);

    @Query("SELECT new com.solusi.erp.inventory.dto.ProductStockSummaryResponse(" +
           "p.id, p.code, p.name, u.code, " +
           "SUM(sb.quantity), SUM(sb.reservedQuantity), SUM(sb.quantity - sb.reservedQuantity), SUM(sb.inTransitQuantity)) " +
           "FROM StockBalance sb " +
           "JOIN sb.product p " +
           "JOIN p.uom u " +
           "WHERE (:keyword IS NULL OR p.code LIKE %:keyword% OR p.name LIKE %:keyword%) " +
           "GROUP BY p.id, p.code, p.name, u.code")
    Page<ProductStockSummaryResponse> getOnHandSummary(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT new com.solusi.erp.inventory.dto.LocationStockDetailResponse(" +
           "f.name, g.code, c.code, sb.serialNumber, " +
           "sb.quantity, sb.reservedQuantity, (sb.quantity - sb.reservedQuantity), sb.inTransitQuantity) " +
           "FROM StockBalance sb " +
           "JOIN sb.container c " +
           "JOIN c.grid g " +
           "JOIN g.facility f " +
           "WHERE sb.product.id = :productId")
    List<LocationStockDetailResponse> getOnHandDetail(@Param("productId") Long productId);
}
