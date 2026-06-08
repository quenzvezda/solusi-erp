package com.solusi.erp.purchasing.purchasereturn.infrastructure.persistence;

import com.solusi.erp.core.model.BaseModel;
import com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturnReason;
import com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturnStatus;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "pur_purchase_returns")
@Getter
@Setter
@NoArgsConstructor
public class PurchaseReturnEntity extends BaseModel {

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Column(name = "return_date", nullable = false)
    private LocalDate returnDate;

    @Column(name = "reference_type", nullable = false, length = 40)
    private String referenceType;

    @Column(name = "reference_id", nullable = false)
    private Long referenceId;

    @Column(name = "reference_code", nullable = false, length = 60)
    private String referenceCode;

    @Column(name = "purchase_order_id")
    private Long purchaseOrderId;

    @Column(name = "purchase_order_code", length = 60)
    private String purchaseOrderCode;

    @Column(name = "supplier_id", nullable = false)
    private Long supplierId;

    @Column(name = "facility_id", nullable = false)
    private Long facilityId;

    @Column(name = "currency_id", nullable = false)
    private Long currencyId;

    @Column(name = "exchange_rate", nullable = false, precision = 19, scale = 6)
    private BigDecimal exchangeRate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PurchaseReturnStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason_code", nullable = false, length = 40)
    private PurchaseReturnReason reason;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(name = "submitted_by_user_id")
    private Long submittedByUserId;

    @Column(name = "generated_gi_id", unique = true)
    private Long generatedGoodsIssueId;

    @Column(name = "reversal_date")
    private LocalDate reversalDate;

    @Column(name = "reversal_reason", length = 500)
    private String reversalReason;

    @Column(name = "reversed_by_user_id")
    private Long reversedByUserId;

    @Column(name = "reversal_journal_entry_id")
    private Long reversalJournalEntryId;

    @OneToMany(mappedBy = "header", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PurchaseReturnLineEntity> lines = new ArrayList<>();

    @OneToMany(mappedBy = "purchaseReturn", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PurchaseReturnReversalLineEntity> reversalLines = new LinkedHashSet<>();
}
