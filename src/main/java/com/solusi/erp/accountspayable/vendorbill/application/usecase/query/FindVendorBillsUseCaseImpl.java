package com.solusi.erp.accountspayable.vendorbill.application.usecase.query;

import com.solusi.erp.accountspayable.vendorbill.domain.model.VendorBill;
import com.solusi.erp.accountspayable.vendorbill.domain.model.VendorBillStatus;
import com.solusi.erp.accountspayable.vendorbill.domain.repository.VendorBillRepository;
import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;

public class FindVendorBillsUseCaseImpl implements FindVendorBillsUseCase {

    private final VendorBillRepository vendorBillRepository;

    public FindVendorBillsUseCaseImpl(VendorBillRepository vendorBillRepository) {
        this.vendorBillRepository = vendorBillRepository;
    }

    @Override
    public Page<VendorBillSummaryView> execute(String keyword, Long vendorId, VendorBillStatus status, Pageable pageable) {
        Page<VendorBill> page = vendorBillRepository.findAll(keyword, vendorId, status, pageable);
        return new Page<>(
                page.content().stream().map(this::toSummary).toList(),
                page.page(),
                page.size(),
                page.totalElements()
        );
    }

    private VendorBillSummaryView toSummary(VendorBill bill) {
        return new VendorBillSummaryView(
                bill.getId(),
                bill.getCode(),
                bill.getVendorId(),
                bill.getVendorInvoiceNumber(),
                bill.getBillDate(),
                bill.getDueDate(),
                bill.getStatus(),
                bill.getTotalAmount()
        );
    }
}
