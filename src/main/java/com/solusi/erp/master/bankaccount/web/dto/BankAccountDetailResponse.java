package com.solusi.erp.master.bankaccount.web.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Response DTO for BankAccount detail/audit view.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BankAccountDetailResponse extends BaseAuditResponse {
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
}
