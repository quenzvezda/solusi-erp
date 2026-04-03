package com.solusi.erp.master.bankaccount.web.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Response DTO for BankAccount list view.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BankAccountSummaryResponse extends BaseAuditResponse {
    private String code;
    private String bankName;
    private String accountName;
    private String accountNo;
    private String accountType;
    private String partyName;
}
