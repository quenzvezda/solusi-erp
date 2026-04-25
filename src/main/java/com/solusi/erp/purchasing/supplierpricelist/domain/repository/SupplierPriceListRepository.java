package com.solusi.erp.purchasing.supplierpricelist.domain.repository;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.purchasing.supplierpricelist.domain.model.SupplierPriceList;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SupplierPriceListRepository {

    SupplierPriceList save(SupplierPriceList supplierPriceList);

    Optional<SupplierPriceList> findById(Long id);

    Page<SupplierPriceList> findAll(String keyword, Pageable pageable);

    List<SupplierPriceList> search(String keyword, int limit);

    void delete(Long id);

    boolean existsOverlapping(Long supplierId, Long productId, Long uomId, Long currencyId,
                               LocalDate effectiveFrom, LocalDate effectiveTo, Long excludeId);

    /**
     * Find the most recent active SPL for the given supplier+product+uom+currency combo
     * that is valid on the given date.
     * 
     * Priority: Latest effectiveFrom among all matching active SPLs
     */
    Optional<SupplierPriceList> findMostRecentActiveSPL(Long supplierId, Long productId,
                                                        Long uomId, Long currencyId,
                                                        LocalDate asOfDate);
}
