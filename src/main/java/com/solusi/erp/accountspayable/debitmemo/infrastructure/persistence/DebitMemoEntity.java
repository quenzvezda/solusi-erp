package com.solusi.erp.accountspayable.debitmemo.infrastructure.persistence;

import com.solusi.erp.accountspayable.debitmemo.domain.model.DebitMemoSettlementStatus;
import com.solusi.erp.core.model.BaseModel;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ap_debit_memos")
@Getter
@Setter
@NoArgsConstructor
public class DebitMemoEntity extends BaseModel {

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "purchase_return_id", nullable = false, unique = true)
    private Long purchaseReturnId;

    @Column(name = "purchase_return_code", nullable = false, length = 60)
    private String purchaseReturnCode;

    @Column(name = "vendor_id", nullable = false)
    private Long vendorId;

    @Column(name = "currency_id", nullable = false)
    private Long currencyId;

    @Column(name = "memo_date", nullable = false)
    private LocalDate memoDate;

    @Column(name = "gross_amount_original", nullable = false, precision = 19, scale = 4)
    private BigDecimal grossAmountOriginal = BigDecimal.ZERO;

    @Column(name = "dpp_amount_original", nullable = false, precision = 19, scale = 4)
    private BigDecimal dppAmountOriginal = BigDecimal.ZERO;

    @Column(name = "tax_amount_original", nullable = false, precision = 19, scale = 4)
    private BigDecimal taxAmountOriginal = BigDecimal.ZERO;

    @Column(name = "gross_amount_base", nullable = false, precision = 19, scale = 4)
    private BigDecimal grossAmountBase = BigDecimal.ZERO;

    @Column(name = "dpp_amount_base", nullable = false, precision = 19, scale = 4)
    private BigDecimal dppAmountBase = BigDecimal.ZERO;

    @Column(name = "tax_amount_base", nullable = false, precision = 19, scale = 4)
    private BigDecimal taxAmountBase = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "settlement_status", nullable = false, length = 30)
    private DebitMemoSettlementStatus settlementStatus;

    @Column(name = "supplier_memo_number", length = 100)
    private String supplierMemoNumber;

    @Column(name = "supplier_memo_date")
    private LocalDate supplierMemoDate;

    @Column(name = "tax_document_number", length = 100)
    private String taxDocumentNumber;

    @Column(name = "tax_document_date")
    private LocalDate taxDocumentDate;

    @Column(name = "notes", length = 500)
    private String notes;

    @OneToMany(mappedBy = "debitMemo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DebitMemoLineEntity> lines = new ArrayList<>();
}

