package com.solusi.erp.accountspayable.vendorbill.application.usecase.query;

import com.solusi.erp.accountspayable.vendorbill.domain.model.VendorBill;
import com.solusi.erp.accountspayable.vendorbill.domain.model.VendorBillDocumentStatus;
import com.solusi.erp.accountspayable.vendorbill.domain.model.VendorBillSettlementStatus;
import com.solusi.erp.accountspayable.vendorbill.domain.port.VendorBillPaymentSummaryPort;
import com.solusi.erp.accountspayable.vendorbill.domain.repository.VendorBillRepository;
import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class FindVendorBillsUseCaseImpl implements FindVendorBillsUseCase {

    private final VendorBillRepository vendorBillRepository;
    private final VendorBillPaymentSummaryPort paymentSummaryPort;

    public FindVendorBillsUseCaseImpl(VendorBillRepository vendorBillRepository,
                                      VendorBillPaymentSummaryPort paymentSummaryPort) {
        this.vendorBillRepository = vendorBillRepository;
        this.paymentSummaryPort = paymentSummaryPort;
    }

    @Override
    public Page<VendorBillSummaryView> execute(String keyword, Long vendorId, VendorBillDocumentStatus documentStatus,
                                               VendorBillSettlementStatus settlementStatus, Pageable pageable) {
        Page<VendorBill> page = vendorBillRepository.findAll(keyword, vendorId, documentStatus, settlementStatus, pageable);
        List<Long> billIds = page.content().stream().map(VendorBill::getId).toList();
        Map<Long, VendorBillPaymentSummaryPort.PaymentSummary> summaries = paymentSummaryPort.getPaymentSummaries(billIds);
        return new Page<>(
                page.content().stream().map(bill -> toSummary(bill, summaries.get(bill.getId()))).toList(),
                page.page(),
                page.size(),
                page.totalElements()
        );
    }

    private VendorBillSummaryView toSummary(VendorBill bill, VendorBillPaymentSummaryPort.PaymentSummary summary) {
        BigDecimal paidAmount = summary != null ? summary.paidAmount() : BigDecimal.ZERO;
        BigDecimal outstandingAmount = summary != null ? summary.outstandingAmount() : bill.getTotalAmount();
        return new VendorBillSummaryView(
                bill.getId(),
                bill.getCode(),
                bill.getVendorId(),
                bill.getVendorInvoiceNumber(),
                bill.getBillDate(),
                bill.getDueDate(),
                bill.getDocumentStatus(),
                bill.getTotalAmount(),
                paidAmount,
                outstandingAmount
        );
    }
}
