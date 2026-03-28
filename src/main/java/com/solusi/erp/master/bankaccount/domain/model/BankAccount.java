package com.solusi.erp.master.bankaccount.domain.model;

import com.solusi.erp.core.domain.model.AuditMetadata;

/**
 * Aggregate Root: BankAccount.
 * 100% Pure Java Domain Model — no Spring, JPA, or Lombok dependencies.
 */
public class BankAccount {

    private final AuditMetadata metadata;
    private String code;
    private String bankName;
    private String branch;
    private String accountName;
    private String accountNo;
    private String accountType;
    private String note;
    private Long cityId;
    private String cityName;
    private Long partyId;
    private String partyName;
    private Boolean isActive;

    public BankAccount(AuditMetadata metadata, String code, String bankName, String branch,
                       String accountName, String accountNo, String accountType, String note,
                       Long cityId, String cityName, Long partyId, String partyName,
                       Boolean isActive) {
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
    }

    public static BankAccount createNew(String code, String bankName, String branch,
                                        String accountName, String accountNo, String accountType,
                                        String note, Long cityId, Long partyId, Boolean isActive) {
        return new BankAccount(AuditMetadata.empty(), code, bankName, branch,
                accountName, accountNo, accountType, note,
                cityId, null, partyId, null, isActive);
    }

    public void update(String bankName, String branch, String accountName, String accountNo,
                       String accountType, String note, Long cityId, Long partyId, Boolean isActive) {
        this.bankName = bankName;
        this.branch = branch;
        this.accountName = accountName;
        this.accountNo = accountNo;
        this.accountType = accountType;
        this.note = note;
        this.cityId = cityId;
        this.partyId = partyId;
        this.isActive = isActive;
    }

    public Long getId() { return metadata.id(); }
    public AuditMetadata getMetadata() { return metadata; }
    public String getCode() { return code; }
    public String getBankName() { return bankName; }
    public String getBranch() { return branch; }
    public String getAccountName() { return accountName; }
    public String getAccountNo() { return accountNo; }
    public String getAccountType() { return accountType; }
    public String getNote() { return note; }
    public Long getCityId() { return cityId; }
    public String getCityName() { return cityName; }
    public Long getPartyId() { return partyId; }
    public String getPartyName() { return partyName; }
    public Boolean getIsActive() { return isActive; }
}
