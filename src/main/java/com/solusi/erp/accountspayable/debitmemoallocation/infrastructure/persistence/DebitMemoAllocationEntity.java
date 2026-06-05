package com.solusi.erp.accountspayable.debitmemoallocation.infrastructure.persistence;

import com.solusi.erp.accountspayable.debitmemoallocation.domain.model.DebitMemoAllocationStatus;
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
@Table(name = "ap_debit_memo_allocations")
@Getter
@Setter
@NoArgsConstructor
public class DebitMemoAllocationEntity extends BaseModel {

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "debit_memo_id", nullable = false)
    private Long debitMemoId;

    @Column(name = "debit_memo_code", nullable = false, length = 50)
    private String debitMemoCode;

    @Column(name = "allocation_date", nullable = false)
    private LocalDate allocationDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DebitMemoAllocationStatus status;

    @Column(name = "total_applied_gross_original", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalAppliedGrossOriginal = BigDecimal.ZERO;

    @Column(name = "total_dpp_original", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalDppOriginal = BigDecimal.ZERO;

    @Column(name = "total_tax_original", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalTaxOriginal = BigDecimal.ZERO;

    @Column(name = "total_grir_reversal_base", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalGrirReversalBase = BigDecimal.ZERO;

    @Column(name = "total_tax_reversal_base", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalTaxReversalBase = BigDecimal.ZERO;

    @Column(name = "total_ap_reduction_base", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalApReductionBase = BigDecimal.ZERO;

    @Column(name = "total_fx_loss_base", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalFxLossBase = BigDecimal.ZERO;

    @Column(name = "total_fx_gain_base", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalFxGainBase = BigDecimal.ZERO;

    @Column(name = "apply_journal_entry_id")
    private Long applyJournalEntryId;

    @Column(name = "reversal_journal_entry_id")
    private Long reversalJournalEntryId;

    @Column(name = "reversal_date")
    private LocalDate reversalDate;

    @Column(name = "reversal_reason", length = 500)
    private String reversalReason;

    @Column(length = 500)
    private String notes;

    @OneToMany(mappedBy = "allocation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DebitMemoAllocationLineEntity> lines = new ArrayList<>();
}
