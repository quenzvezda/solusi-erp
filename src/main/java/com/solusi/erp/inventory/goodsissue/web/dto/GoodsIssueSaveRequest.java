package com.solusi.erp.inventory.goodsissue.web.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssuePartyType;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssueReferenceType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class GoodsIssueSaveRequest extends BaseAuditResponse {
    @NotNull(message = "{label.gi.issueDate} {validation.notnull.suffix}")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate issueDate;
    private GoodsIssueReferenceType referenceType;
    private Long referenceId;
    private String referenceCode;
    private Long partyId;
    private GoodsIssuePartyType partyType;
    private String partyName;
    private Long facilityId;
    private String facilityName;
    private Long currencyId;
    private String currencyCode;
    private BigDecimal exchangeRate;
    private String status;
    private String notes;
    @Valid
    private List<GoodsIssueSaveLineRequest> lines = new ArrayList<>();
}
