package com.solusi.erp.accountspayable.debitmemo.application.usecase.command;

import com.solusi.erp.accountspayable.debitmemo.domain.model.DebitMemo;
import com.solusi.erp.accountspayable.debitmemo.domain.repository.DebitMemoRepository;
import com.solusi.erp.core.exception.DomainException;

import java.time.LocalDate;

public class UpdateDebitMemoMetadataUseCaseImpl implements UpdateDebitMemoMetadataUseCase {

    private final DebitMemoRepository debitMemoRepository;

    public UpdateDebitMemoMetadataUseCaseImpl(DebitMemoRepository debitMemoRepository) {
        this.debitMemoRepository = debitMemoRepository;
    }

    @Override
    public DebitMemo execute(Long id,
                             String supplierMemoNumber,
                             LocalDate supplierMemoDate,
                             String taxDocumentNumber,
                             LocalDate taxDocumentDate,
                             String notes) {
        DebitMemo debitMemo = debitMemoRepository.findById(id)
                .orElseThrow(() -> new DomainException("msg.error.debit-memo.not-found"));
        String normalizedSupplierMemoNumber = blankToNull(supplierMemoNumber);
        String normalizedTaxDocumentNumber = blankToNull(taxDocumentNumber);

        if (normalizedSupplierMemoNumber != null
                && debitMemoRepository.existsSupplierMemoNumber(
                debitMemo.getVendorId(), normalizedSupplierMemoNumber, debitMemo.getId())) {
            throw new DomainException("msg.error.debit-memo.supplier-memo-number-duplicate");
        }
        if (normalizedTaxDocumentNumber != null
                && debitMemoRepository.existsTaxDocumentNumber(normalizedTaxDocumentNumber, debitMemo.getId())) {
            throw new DomainException("msg.error.debit-memo.tax-document-number-duplicate");
        }

        debitMemo.updateMetadata(
                normalizedSupplierMemoNumber,
                supplierMemoDate,
                normalizedTaxDocumentNumber,
                taxDocumentDate,
                notes
        );
        return debitMemoRepository.save(debitMemo);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}

