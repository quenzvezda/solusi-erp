package com.solusi.erp.accountspayable.debitmemo.application.usecase.command;

import com.solusi.erp.accountspayable.debitmemo.domain.model.DebitMemo;

import java.time.LocalDate;

public interface UpdateDebitMemoMetadataUseCase {

    DebitMemo execute(Long id,
                      String supplierMemoNumber,
                      LocalDate supplierMemoDate,
                      String taxDocumentNumber,
                      LocalDate taxDocumentDate,
                      String notes);
}

