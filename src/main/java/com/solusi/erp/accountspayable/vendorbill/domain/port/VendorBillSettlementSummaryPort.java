package com.solusi.erp.accountspayable.vendorbill.domain.port;

import com.solusi.erp.accountspayable.vendorbill.domain.model.VendorBillSettlementStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface VendorBillSettlementSummaryPort {
    SettlementSummary getSettlementSummary(Long vendorBillId);

    Map<Long, SettlementSummary> getSettlementSummaries(List<Long> vendorBillIds);

    record SettlementSummary(
            Long vendorBillId,
            BigDecimal paidAmount,
            BigDecimal debitMemoAppliedAmount,
            BigDecimal outstandingAmount,
            VendorBillSettlementStatus settlementStatus
    ) {}
}
