package com.solusi.erp.accountspayable.debitmemo.web.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class DebitMemoMetadataRequest {

    @Size(max = 100, message = "{label.debit-memo.supplier-memo-number} {validation.size.suffix}")
    private String supplierMemoNumber;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate supplierMemoDate;

    @Size(max = 100, message = "{label.debit-memo.tax-document-number} {validation.size.suffix}")
    private String taxDocumentNumber;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate taxDocumentDate;

    @Size(max = 500, message = "{label.notes} {validation.size.suffix}")
    private String notes;
}
