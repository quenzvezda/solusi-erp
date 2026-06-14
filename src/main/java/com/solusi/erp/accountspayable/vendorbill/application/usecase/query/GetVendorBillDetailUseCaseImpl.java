package com.solusi.erp.accountspayable.vendorbill.application.usecase.query;

import com.solusi.erp.accountspayable.vendorbill.domain.model.VendorBill;
import com.solusi.erp.accountspayable.vendorbill.domain.model.VendorBillGrRef;
import com.solusi.erp.accountspayable.vendorbill.domain.model.VendorBillLine;
import com.solusi.erp.accountspayable.vendorbill.domain.model.VendorBillSettlementStatus;
import com.solusi.erp.accountspayable.vendorbill.domain.port.VendorBillSettlementSummaryPort;
import com.solusi.erp.accountspayable.vendorbill.domain.repository.VendorBillRepository;
import com.solusi.erp.core.exception.DomainException;

import java.math.BigDecimal;

public class GetVendorBillDetailUseCaseImpl implements GetVendorBillDetailUseCase {

    private final VendorBillRepository vendorBillRepository;
    private final VendorBillSettlementSummaryPort settlementSummaryPort;

    public GetVendorBillDetailUseCaseImpl(VendorBillRepository vendorBillRepository,
                                          VendorBillSettlementSummaryPort settlementSummaryPort) {
        this.vendorBillRepository = vendorBillRepository;
        this.settlementSummaryPort = settlementSummaryPort;
    }

    @Override
    public VendorBillDetailView execute(Long id) {
        return vendorBillRepository.findById(id)
                .map(this::toDetail)
                .orElseThrow(() -> new DomainException("msg.err.vb.notfound"));
    }

    private VendorBillDetailView toDetail(VendorBill bill) {
        VendorBillSettlementSummaryPort.SettlementSummary summary = settlementSummaryPort.getSettlementSummary(bill.getId());
        BigDecimal paidAmount = summary != null ? summary.paidAmount() : BigDecimal.ZERO;
        BigDecimal debitMemoAppliedAmount = summary != null ? summary.debitMemoAppliedAmount() : BigDecimal.ZERO;
        BigDecimal outstandingAmount = summary != null ? summary.outstandingAmount() : bill.getTotalAmount();
        VendorBillSettlementStatus settlementStatus = summary != null ? summary.settlementStatus() : bill.getSettlementStatus();
        return new VendorBillDetailView(
                bill.getId(),
                bill.getCode(),
                bill.getVendorId(),
                bill.getVendorInvoiceNumber(),
                bill.getBillDate(),
                bill.getDueDate(),
                bill.getCurrencyId(),
                bill.getExchangeRate(),
                bill.getDocumentStatus(),
                settlementStatus,
                bill.getSubtotal(),
                bill.getTaxAmount(),
                bill.getTotalAmount(),
                paidAmount,
                debitMemoAppliedAmount,
                outstandingAmount,
                bill.getNotes(),
                bill.getGrRefs().stream().map(VendorBillGrRef::grId).toList(),
                bill.getLines().stream().map(this::toLine).toList()
        );
    }

    private VendorBillLineView toLine(VendorBillLine line) {
        return new VendorBillLineView(
                line.getId(),
                line.getGrLineId(),
                line.getProductId(),
                line.getProductName(),
                line.getDescription(),
                line.getQtyBilled(),
                line.getUomId(),
                line.getUomName(),
                line.getUnitPrice(),
                line.getInventoryAmount(),
                line.getTaxAmount(),
                line.getLineTotal()
        );
    }
}
