package com.solusi.erp.accountspayable.debitmemoallocation.application.usecase.query;

import com.solusi.erp.accountspayable.debitmemoallocation.domain.port.DebitMemoAllocationSourcePort;
import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;

public class DebitMemoAllocationSelectorUseCaseImpl implements DebitMemoAllocationSelectorUseCase {

    private final DebitMemoAllocationSourcePort sourcePort;

    public DebitMemoAllocationSelectorUseCaseImpl(DebitMemoAllocationSourcePort sourcePort) {
        this.sourcePort = sourcePort;
    }

    @Override
    public Page<DebitMemoAllocationSourcePort.EligibleVendorBill> eligibleVendorBills(Long debitMemoId, String keyword, Pageable pageable) {
        return sourcePort.findEligibleVendorBills(debitMemoId, keyword, pageable);
    }

    @Override
    public Page<DebitMemoAllocationSourcePort.EligibleDebitMemo> eligibleDebitMemos(Long vendorBillId, String keyword, Pageable pageable) {
        return sourcePort.findEligibleDebitMemos(vendorBillId, keyword, pageable);
    }
}
