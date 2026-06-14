package com.solusi.erp.inventory.goodsissue.web.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class GoodsIssueSummaryResponse extends BaseAuditResponse {
    private String code;
    private LocalDate issueDate;
    private String referenceType;
    private String referenceCode;
    private String partyName;
    private String facilityName;
    private String status;
    private int lineCount;
    private BigDecimal totalAmount;
}
