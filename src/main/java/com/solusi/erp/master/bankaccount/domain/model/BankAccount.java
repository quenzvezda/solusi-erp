package com.solusi.erp.master.bankaccount.domain.model;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.master.shared.model.PaymentType;

public class BankAccount {

    private final AuditMetadata metadata;
    private String code;
    private String bankName;
    private String branch;
    private String accountName;
    private String accountNo;
    private PaymentType accountType;
    private String note;
    private Long cityId;
    private String cityName;
    private Long partyId;
    private String partyName;
    private Boolean isActive;
    private Long currencyId;
    private Long coaId;

    public BankAccount(AuditMetadata metadata, String code, String bankName, String branch,
                       String accountName, String accountNo, PaymentType accountType, String note,
                       Long cityId, String cityName, Long partyId, String partyName,
                       Boolean isActive, Long currencyId, Long coaId) {
        this.metadata = metadata;
        this.code = code;
        this.bankName = bankName;
        this.branch = branch;
        this.accountName = accountName;
        this.accountNo = accountNo;
        this.accountType = accountType;
        this.note = note;
        this.cityId = cityId;
        this.cityName = cityName;
        this.partyId = partyId;
        this.partyName = partyName;
        this.isActive = isActive;
        this.currencyId = currencyId;
        this.coaId = coaId;
    }

    public static BankAccount createNew(String code, String bankName, String branch,
                                        String accountName, String accountNo, PaymentType accountType,
                                        String note, Long cityId, Long partyId, Boolean isActive,
                                        Long currencyId, Long coaId) {
        return new BankAccount(AuditMetadata.empty(), code, bankName, branch,
                accountName, accountNo, accountType, note,
                cityId, null, partyId, null, isActive, currencyId, coaId);
    }

    public void update(String bankName, String branch, String accountName, String accountNo,
                       PaymentType accountType, String note, Long cityId, Long partyId,
                       Boolean isActive, Long currencyId, Long coaId) {
        this.bankName = bankName;
        this.branch = branch;
        this.accountName = accountName;
        this.accountNo = accountNo;
        this.accountType = accountType;
        this.note = note;
        this.cityId = cityId;
        this.partyId = partyId;
        this.isActive = isActive;
        this.currencyId = currencyId;
        this.coaId = coaId;
    }

    public void softDelete() {
        this.isActive = false;
    }

    public Long getId() { return metadata.id(); }
    public AuditMetadata getMetadata() { return metadata; }
    public String getCode() { return code; }
    public String getBankName() { return bankName; }
    public String getBranch() { return branch; }
    public String getAccountName() { return accountName; }
    public String getAccountNo() { return accountNo; }
    public PaymentType getAccountType() { return accountType; }
    public String getNote() { return note; }
    public Long getCityId() { return cityId; }
    public String getCityName() { return cityName; }
    public Long getPartyId() { return partyId; }
    public String getPartyName() { return partyName; }
    public Boolean getIsActive() { return isActive; }
    public Long getCurrencyId() { return currencyId; }
    public Long getCoaId() { return coaId; }
}
