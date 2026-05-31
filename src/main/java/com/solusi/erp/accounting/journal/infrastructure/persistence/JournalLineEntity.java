package com.solusi.erp.accounting.journal.infrastructure.persistence;

import com.solusi.erp.core.model.BaseModel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "acc_journal_lines")
@Getter
@Setter
public class JournalLineEntity extends BaseModel {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "journal_entry_id", nullable = false)
    private JournalEntryEntity journalEntry;

    @Column(name = "line_no", nullable = false)
    private Integer lineNo;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "debit_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal debitAmount;

    @Column(name = "credit_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal creditAmount;

    @Column(name = "original_currency_id")
    private Long originalCurrencyId;

    @Column(name = "exchange_rate", precision = 19, scale = 6)
    private BigDecimal exchangeRate;

    @Column(name = "original_debit_amount", precision = 19, scale = 4)
    private BigDecimal originalDebitAmount;

    @Column(name = "original_credit_amount", precision = 19, scale = 4)
    private BigDecimal originalCreditAmount;

    @Column(length = 255)
    private String description;
}
