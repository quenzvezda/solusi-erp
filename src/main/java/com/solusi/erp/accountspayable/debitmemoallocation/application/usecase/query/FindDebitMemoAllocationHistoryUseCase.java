package com.solusi.erp.accountspayable.debitmemoallocation.application.usecase.query;

import java.util.List;

public interface FindDebitMemoAllocationHistoryUseCase {
    List<DebitMemoAllocationHistoryView> byDebitMemoId(Long debitMemoId);

    List<DebitMemoAllocationHistoryView> byVendorBillId(Long vendorBillId);
}
