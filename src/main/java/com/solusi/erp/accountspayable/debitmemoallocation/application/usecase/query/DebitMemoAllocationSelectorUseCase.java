package com.solusi.erp.accountspayable.debitmemoallocation.application.usecase.query;

import com.solusi.erp.accountspayable.debitmemoallocation.domain.port.DebitMemoAllocationSourcePort;
import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;

public interface DebitMemoAllocationSelectorUseCase {
    Page<DebitMemoAllocationSourcePort.EligibleVendorBill> eligibleVendorBills(Long debitMemoId, String keyword, Pageable pageable);

    Page<DebitMemoAllocationSourcePort.EligibleDebitMemo> eligibleDebitMemos(Long vendorBillId, String keyword, Pageable pageable);
}
