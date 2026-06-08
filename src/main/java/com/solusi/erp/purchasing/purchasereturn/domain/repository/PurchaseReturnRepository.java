package com.solusi.erp.purchasing.purchasereturn.domain.repository;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturn;
import com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturnStatus;

import java.util.Optional;

public interface PurchaseReturnRepository {

    PurchaseReturn save(PurchaseReturn purchaseReturn);

    Optional<PurchaseReturn> findById(Long id);

    Optional<PurchaseReturn> findByIdForUpdate(Long id);

    Page<PurchaseReturn> findAll(String keyword, PurchaseReturnStatus status, Pageable pageable);

    boolean existsByCode(String code);

    boolean existsConfirmedOrOpenBySource(String referenceType, Long referenceId);

    Optional<PurchaseReturn> findByGeneratedGoodsIssueId(Long generatedGoodsIssueId);
}
