package com.solusi.erp.master.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * DTO for displaying Currency information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CurrencyResponse extends BaseAuditResponse {
    private String symbol;
    private String alias;
    private String name;
    private String note;
    private Boolean isActive;
    private Boolean isDefault;
}
