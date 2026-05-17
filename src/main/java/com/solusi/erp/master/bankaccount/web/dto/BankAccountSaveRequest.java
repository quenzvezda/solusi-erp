package com.solusi.erp.master.bankaccount.web.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Intent-Based DTO for Saving (Create/Update) BankAccount.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BankAccountSaveRequest extends BaseAuditResponse {

    @Size(max = 50, message = "{label.bank-account.code} {validation.size.suffix}")
    private String code;

    @NotBlank(message = "{label.bank-account.bank-name} {validation.notblank.suffix}")
    @Size(max = 255, message = "{label.bank-account.bank-name} {validation.size.suffix}")
    private String bankName;

    @NotBlank(message = "{label.bank-account.branch} {validation.notblank.suffix}")
    @Size(max = 255, message = "{label.bank-account.branch} {validation.size.suffix}")
    private String branch;

    @NotBlank(message = "{label.bank-account.account-name} {validation.notblank.suffix}")
    @Size(max = 255, message = "{label.bank-account.account-name} {validation.size.suffix}")
    private String accountName;

    @NotBlank(message = "{label.bank-account.account-no} {validation.notblank.suffix}")
    @Size(max = 100, message = "{label.bank-account.account-no} {validation.size.suffix}")
    private String accountNo;

    @NotBlank(message = "{label.bank-account.account-type} {validation.notblank.suffix}")
    private String accountType;

    private String note;

    @NotNull(message = "{label.bank-account.city} {validation.notnull.suffix}")
    private Long cityId;

    @NotNull(message = "{label.bank-account.party} {validation.notnull.suffix}")
    private Long partyId;

    private Boolean isActive = true;

    private Long currencyId;

    private Long coaId;
}
