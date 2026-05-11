package com.solusi.erp.accountspayable.vendorbill.application.usecase.command;

import com.solusi.erp.accountspayable.vendorbill.domain.model.VendorBill;
import com.solusi.erp.accountspayable.vendorbill.domain.model.VendorBillStatus;
import com.solusi.erp.accountspayable.vendorbill.domain.repository.VendorBillRepository;
import com.solusi.erp.core.exception.DomainException;

public class DeleteVendorBillUseCaseImpl implements DeleteVendorBillUseCase {

    private final VendorBillRepository vendorBillRepository;

    public DeleteVendorBillUseCaseImpl(VendorBillRepository vendorBillRepository) {
        this.vendorBillRepository = vendorBillRepository;
    }

    @Override
    public void execute(Long id) {
        VendorBill bill = vendorBillRepository.findById(id)
                .orElseThrow(() -> new DomainException("msg.error.vb.not.found"));

        if (bill.getStatus() != VendorBillStatus.DRAFT) {
            throw new DomainException("msg.error.vb.only.draft.editable");
        }

        vendorBillRepository.deleteById(id);
    }
}
