package com.solusi.erp.accountspayable.vendorbill.application.usecase.query;

import com.solusi.erp.accountspayable.vendorbill.domain.model.VendorBill;
import com.solusi.erp.accountspayable.vendorbill.domain.model.VendorBillDocumentStatus;
import com.solusi.erp.accountspayable.vendorbill.domain.model.VendorBillSettlementStatus;
import com.solusi.erp.accountspayable.vendorbill.domain.port.VendorBillSettlementSummaryPort;
import com.solusi.erp.accountspayable.vendorbill.domain.repository.VendorBillRepository;
import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class FindVendorBillsUseCaseImpl implements FindVendorBillsUseCase {

    private final VendorBillRepository vendorBillRepository;
    private final VendorBillSettlementSummaryPort settlementSummaryPort;

    public FindVendorBillsUseCaseImpl(VendorBillRepository vendorBillRepository,
                                      VendorBillSettlementSummaryPort settlementSummaryPort) {
        this.vendorBillRepository = vendorBillRepository;
        this.settlementSummaryPort = settlementSummaryPort;
    }

    @Override
    public Page<VendorBillSummaryView> execute(String keyword, Long vendorId, VendorBillDocumentStatus documentStatus,
                                               VendorBillSettlementStatus settlementStatus, Pageable pageable) {
        Page<VendorBill> page = vendorBillRepository.findAll(keyword, vendorId, documentStatus, settlementStatus, pageable);
        List<Long> billIds = page.content().stream().map(VendorBill::getId).toList();
        Map<Long, VendorBillSettlementSummaryPort.SettlementSummary> summaries = settlementSummaryPort.getSettlementSummaries(billIds);
        return new Page<>(
                page.content().stream().map(bill -> toSummary(bill, summaries.get(bill.getId()))).toList(),
                page.page(),
                page.size(),
                page.totalElements()
        );
    }

    private VendorBillSummaryView toSummary(VendorBill bill, VendorBillSettlementSummaryPort.SettlementSummary summary) {
        BigDecimal paidAmount = summary != null ? summary.paidAmount() : BigDecimal.ZERO;
        BigDecimal debitMemoAppliedAmount = summary != null ? summary.debitMemoAppliedAmount() : BigDecimal.ZERO;
        BigDecimal outstandingAmount = summary != null ? summary.outstandingAmount() : bill.getTotalAmount();
        VendorBillSettlementStatus settlementStatus = summary != null ? summary.settlementStatus() : bill.getSettlementStatus();
        return new VendorBillSummaryView(
                bill.getId(),
                bill.getCode(),
                bill.getVendorId(),
                bill.getVendorInvoiceNumber(),
                bill.getBillDate(),
                bill.getDueDate(),
                bill.getDocumentStatus(),
                settlementStatus,
                bill.getTotalAmount(),
                paidAmount,
                debitMemoAppliedAmount,
                outstandingAmount
        );
    }
}
