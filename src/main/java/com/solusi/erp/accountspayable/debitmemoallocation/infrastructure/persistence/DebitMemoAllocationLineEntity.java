package com.solusi.erp.accountspayable.debitmemoallocation.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "ap_debit_memo_allocation_lines")
@Getter
@Setter
@NoArgsConstructor
public class DebitMemoAllocationLineEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "debit_memo_allocation_id", nullable = false)
    private DebitMemoAllocationEntity allocation;

    @Column(name = "vendor_bill_id", nullable = false)
    private Long vendorBillId;

    @Column(name = "vendor_bill_code", nullable = false, length = 50)
    private String vendorBillCode;

    @Column(name = "debit_memo_remaining_at_draft", nullable = false, precision = 19, scale = 4)
    private BigDecimal debitMemoRemainingAtDraft = BigDecimal.ZERO;

    @Column(name = "vendor_bill_outstanding_at_draft", nullable = false, precision = 19, scale = 4)
    private BigDecimal vendorBillOutstandingAtDraft = BigDecimal.ZERO;

    @Column(name = "applied_gross_original", nullable = false, precision = 19, scale = 4)
    private BigDecimal appliedGrossOriginal = BigDecimal.ZERO;

    @Column(name = "applied_dpp_original", nullable = false, precision = 19, scale = 4)
    private BigDecimal appliedDppOriginal = BigDecimal.ZERO;

    @Column(name = "applied_tax_original", nullable = false, precision = 19, scale = 4)
    private BigDecimal appliedTaxOriginal = BigDecimal.ZERO;

    @Column(name = "grir_reversal_base", nullable = false, precision = 19, scale = 4)
    private BigDecimal grirReversalBase = BigDecimal.ZERO;

    @Column(name = "tax_reversal_base", nullable = false, precision = 19, scale = 4)
    private BigDecimal taxReversalBase = BigDecimal.ZERO;

    @Column(name = "vendor_bill_exchange_rate", nullable = false, precision = 19, scale = 6)
    private BigDecimal vendorBillExchangeRate = BigDecimal.ONE;

    @Column(name = "ap_reduction_base", nullable = false, precision = 19, scale = 4)
    private BigDecimal apReductionBase = BigDecimal.ZERO;

    @Column(name = "fx_loss_base", nullable = false, precision = 19, scale = 4)
    private BigDecimal fxLossBase = BigDecimal.ZERO;

    @Column(name = "fx_gain_base", nullable = false, precision = 19, scale = 4)
    private BigDecimal fxGainBase = BigDecimal.ZERO;
}
