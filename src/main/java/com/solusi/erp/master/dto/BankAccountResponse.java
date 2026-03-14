package com.solusi.erp.master.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import com.solusi.erp.master.model.AccountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * DTO for displaying Bank Account information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BankAccountResponse extends BaseAuditResponse {
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
