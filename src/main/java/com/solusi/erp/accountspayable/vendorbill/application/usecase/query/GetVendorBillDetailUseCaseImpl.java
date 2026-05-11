package com.solusi.erp.accountspayable.vendorbill.application.usecase.query;

import com.solusi.erp.accountspayable.vendorbill.domain.model.VendorBill;
import com.solusi.erp.accountspayable.vendorbill.domain.model.VendorBillGrRef;
import com.solusi.erp.accountspayable.vendorbill.domain.model.VendorBillLine;
import com.solusi.erp.accountspayable.vendorbill.domain.repository.VendorBillRepository;
import com.solusi.erp.core.exception.DomainException;

public class GetVendorBillDetailUseCaseImpl implements GetVendorBillDetailUseCase {

    private final VendorBillRepository vendorBillRepository;

    public GetVendorBillDetailUseCaseImpl(VendorBillRepository vendorBillRepository) {
        this.vendorBillRepository = vendorBillRepository;
    }

    @Override
    public VendorBillDetailView execute(Long id) {
        return vendorBillRepository.findById(id)
                .map(this::toDetail)
                .orElseThrow(() -> new DomainException("msg.err.vb.notfound"));
    }

    private VendorBillDetailView toDetail(VendorBill bill) {
        return new VendorBillDetailView(
                bill.getId(),
                bill.getCode(),
                bill.getVendorId(),
                bill.getVendorInvoiceNumber(),
                bill.getBillDate(),
                bill.getDueDate(),
                bill.getCurrencyId(),
                bill.getStatus(),
                bill.getSubtotal(),
                bill.getTaxAmount(),
                bill.getTotalAmount(),
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
