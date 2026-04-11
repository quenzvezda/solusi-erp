package com.solusi.erp.purchasing.purchaserequisition.domain.repository;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.purchasing.purchaserequisition.domain.model.PurchaseRequisition;

import java.util.Optional;

public interface PurchaseRequisitionRepository {

    PurchaseRequisition save(PurchaseRequisition purchaseRequisition);

    Optional<PurchaseRequisition> findById(Long id);

    Page<PurchaseRequisition> findAll(String keyword, Pageable pageable);

    void deleteById(Long id);
}
