package com.solusi.erp.purchasing.purchaseorder.domain.repository;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrder;

import java.util.Optional;

public interface PurchaseOrderRepository {

    PurchaseOrder save(PurchaseOrder purchaseOrder);

    Optional<PurchaseOrder> findById(Long id);

    Page<PurchaseOrder> findAll(String keyword, Pageable pageable);

    void deleteById(Long id);
}
