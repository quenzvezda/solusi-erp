package com.solusi.erp.accountspayable.debitmemo.domain.repository;

import com.solusi.erp.accountspayable.debitmemo.domain.model.DebitMemo;
import com.solusi.erp.accountspayable.debitmemo.domain.model.DebitMemoSettlementStatus;
import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;

import java.time.LocalDate;
import java.util.Optional;

public interface DebitMemoRepository {

    Page<DebitMemo> findAll(String keyword,
                            Long vendorId,
                            DebitMemoSettlementStatus settlementStatus,
                            LocalDate memoDateFrom,
                            LocalDate memoDateTo,
                            Pageable pageable);

    DebitMemo save(DebitMemo debitMemo);

    Optional<DebitMemo> findById(Long id);

    Optional<DebitMemo> findByPurchaseReturnId(Long purchaseReturnId);

    boolean existsByPurchaseReturnId(Long purchaseReturnId);

    boolean existsSupplierMemoNumber(Long vendorId, String supplierMemoNumber, Long excludedId);

    boolean existsTaxDocumentNumber(String taxDocumentNumber, Long excludedId);
}

