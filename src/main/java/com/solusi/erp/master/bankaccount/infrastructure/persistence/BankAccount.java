package com.solusi.erp.master.bankaccount.infrastructure.persistence;

import com.solusi.erp.core.model.BaseModel;
import com.solusi.erp.master.geographic.infrastructure.persistence.Geographic;
import com.solusi.erp.master.party.infrastructure.persistence.Party;
import com.solusi.erp.master.shared.model.PaymentType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "bank_accounts")
public class BankAccount extends BaseModel {

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "bank_name", nullable = false)
    private String bankName;

    @Column(name = "branch", nullable = false)
    private String branch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id", nullable = false)
    private Geographic city;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "party_id", nullable = false)
    private Party party;

    @Column(name = "account_name", nullable = false)
    private String accountName;

    @Column(name = "account_no", nullable = false, length = 100)
    private String accountNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false, length = 50)
    private PaymentType accountType;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "currency_id")
    private Long currencyId;

    @Column(name = "coa_id")
    private Long coaId;
}
