package com.solusi.erp.inventory.goodsissue.web.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class GoodsIssueDetailResponse extends BaseAuditResponse {
    private String code;
    private LocalDate issueDate;
    private String referenceType;
    private Long referenceId;
    private String referenceCode;
    private Long partyId;
    private String partyType;
    private String partyName;
    private Long facilityId;
    private String facilityName;
    private Long currencyId;
    private String currencyCode;
    private BigDecimal exchangeRate;
    private String status;
    private String notes;
    private List<GoodsIssueLineDetailResponse> lines;
}
