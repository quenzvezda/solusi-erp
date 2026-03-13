package com.solusi.erp.master.dto;

import com.solusi.erp.master.model.AccountType;
import lombok.Data;

@Data
public class BankAccountResponseDto {
    private Long id;
    private String code;
    private String bankName;
    private String branch;
    private Long cityId;
    private String cityName;
    private Long partyId;
    private String partyName;
    private String accountName;
    private String accountNo;
    private AccountType accountType;
    private String note;
}
