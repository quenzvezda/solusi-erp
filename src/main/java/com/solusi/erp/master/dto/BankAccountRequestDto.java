package com.solusi.erp.master.dto;

import com.solusi.erp.master.model.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BankAccountRequestDto {

    private String code;

    @NotBlank(message = "{master.bank-account.bank-name.required}")
    private String bankName;

    @NotBlank(message = "{master.bank-account.branch.required}")
    private String branch;

    @NotNull(message = "{master.bank-account.city.required}")
    private Long cityId;

    @NotNull(message = "{master.bank-account.party.required}")
    private Long partyId;

    @NotBlank(message = "{master.bank-account.account-name.required}")
    private String accountName;

    @NotBlank(message = "{master.bank-account.account-no.required}")
    private String accountNo;

    @NotNull(message = "{master.bank-account.account-type.required}")
    private AccountType accountType;

    private String note;
}
