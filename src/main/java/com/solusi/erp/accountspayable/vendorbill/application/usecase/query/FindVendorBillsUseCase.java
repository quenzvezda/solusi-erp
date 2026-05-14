package com.solusi.erp.accountspayable.vendorbill.application.usecase.query;

import com.solusi.erp.accountspayable.vendorbill.domain.model.VendorBillStatus;
import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;

@FunctionalInterface
public interface FindVendorBillsUseCase {
    Page<VendorBillSummaryView> execute(String keyword, Long vendorId, VendorBillStatus status, Pageable pageable);
}
