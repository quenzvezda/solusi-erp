package com.solusi.erp.accountspayable.vendorbill.application.usecase.query;

import com.solusi.erp.accountspayable.vendorbill.domain.model.VendorBill;
import com.solusi.erp.accountspayable.vendorbill.domain.model.VendorBillGrRef;
import com.solusi.erp.accountspayable.vendorbill.domain.model.VendorBillLine;
import com.solusi.erp.accountspayable.vendorbill.domain.port.VendorBillPaymentSummaryPort;
import com.solusi.erp.accountspayable.vendorbill.domain.repository.VendorBillRepository;
import com.solusi.erp.core.exception.DomainException;

import java.math.BigDecimal;

public class GetVendorBillDetailUseCaseImpl implements GetVendorBillDetailUseCase {

    private final VendorBillRepository vendorBillRepository;
    private final VendorBillPaymentSummaryPort paymentSummaryPort;

    public GetVendorBillDetailUseCaseImpl(VendorBillRepository vendorBillRepository,
                                          VendorBillPaymentSummaryPort paymentSummaryPort) {
        this.vendorBillRepository = vendorBillRepository;
        this.paymentSummaryPort = paymentSummaryPort;
    }

    @Override
    public VendorBillDetailView execute(Long id) {
        return vendorBillRepository.findById(id)
                .map(this::toDetail)
                .orElseThrow(() -> new DomainException("msg.err.vb.notfound"));
    }

    private VendorBillDetailView toDetail(VendorBill bill) {
        VendorBillPaymentSummaryPort.PaymentSummary summary = paymentSummaryPort.getPaymentSummary(bill.getId());
        BigDecimal paidAmount = summary != null ? summary.paidAmount() : BigDecimal.ZERO;
        BigDecimal outstandingAmount = summary != null ? summary.outstandingAmount() : bill.getTotalAmount();
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
                bill.getSubtotal(),
                bill.getTaxAmount(),
                bill.getTotalAmount(),
                paidAmount,
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
