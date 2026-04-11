package com.solusi.erp.accounting.coa.domain.model;

import com.solusi.erp.core.domain.model.AuditMetadata;

public class ChartOfAccount {
    private final AuditMetadata metadata;
    private String code;
    private String name;
    private AccountType accountType;
    private NormalBalance normalBalance;
    private Long parentId;
    private Integer level;
    private Boolean isHeader;
    private String note;
    private Boolean isActive;

    public ChartOfAccount(AuditMetadata metadata, String code, String name,
                          AccountType accountType, NormalBalance normalBalance,
                          Long parentId, Integer level, Boolean isHeader,
                          String note, Boolean isActive) {
        this.metadata = metadata;
        this.code = code;
        this.name = name;
        this.accountType = accountType;
        this.normalBalance = normalBalance;
        this.parentId = parentId;
        this.level = level;
        this.isHeader = isHeader;
        this.note = note;
        this.isActive = isActive;
    }

    public static ChartOfAccount createNew(String code, String name,
                                            AccountType accountType,
                                            Long parentId, Integer level,
                                            Boolean isHeader, String note,
                                            Boolean isActive) {
        return new ChartOfAccount(
                AuditMetadata.empty(), code, name, accountType,
                accountType.getDefaultNormalBalance(),
                parentId,
                level != null ? level : 1,
                isHeader != null ? isHeader : false,
                note,
                isActive != null ? isActive : true
        );
    }

    public void update(String name, AccountType accountType,
                       Long parentId, Integer level,
                       Boolean isHeader, String note, Boolean isActive) {
        this.name = name;
        this.accountType = accountType;
        this.normalBalance = accountType.getDefaultNormalBalance();
        this.parentId = parentId;
        this.level = level != null ? level : 1;
        this.isHeader = isHeader != null ? isHeader : false;
        this.note = note;
        this.isActive = isActive != null ? isActive : true;
    }

    public void softDelete() {
        this.isActive = false;
    }

    public Long getId() { return metadata.id(); }
    public AuditMetadata getMetadata() { return metadata; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public AccountType getAccountType() { return accountType; }
    public NormalBalance getNormalBalance() { return normalBalance; }
    public Long getParentId() { return parentId; }
    public Integer getLevel() { return level; }
    public Boolean getIsHeader() { return isHeader; }
    public String getNote() { return note; }
    public Boolean getIsActive() { return isActive; }
}
