package com.solusi.erp.purchasing.purchaseorder.domain.repository;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrder;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface PurchaseOrderRepository {

    PurchaseOrder save(PurchaseOrder purchaseOrder);

    Optional<PurchaseOrder> findById(Long id);

    Page<PurchaseOrder> findAll(String keyword, Pageable pageable);

    Map<Long, BigDecimal> sumCommittedQuantityByPrLineIds(Set<Long> prLineIds);

    void deleteById(Long id);
}
