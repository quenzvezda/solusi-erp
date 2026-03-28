package com.solusi.erp.master.currency.web.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CurrencyDetailResponse extends BaseAuditResponse {
    private String symbol;
    private String alias;
    private String name;
    private String note;
    private Boolean isDefault;
    private Boolean isActive;
}
