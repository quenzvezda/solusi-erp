package com.solusi.erp.accountspayable.vendorbill.domain.repository;

import com.solusi.erp.accountspayable.vendorbill.domain.model.VendorBill;
import com.solusi.erp.accountspayable.vendorbill.domain.model.VendorBillDocumentStatus;
import com.solusi.erp.accountspayable.vendorbill.domain.model.VendorBillSettlementStatus;
import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;

import java.util.Optional;

public interface VendorBillRepository {

    Page<VendorBill> findAll(String keyword, Long vendorId, VendorBillDocumentStatus documentStatus,
                             VendorBillSettlementStatus settlementStatus, Pageable pageable);

    VendorBill save(VendorBill bill);

    Optional<VendorBill> findById(Long id);

    void deleteById(Long id);
}
