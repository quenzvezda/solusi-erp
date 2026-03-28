package com.solusi.erp.master.party.web.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import com.solusi.erp.master.model.PartyType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PartySummaryResponse extends BaseAuditResponse {
    private String code;
    private String name;
    private PartyType type;
    private List<String> roleNames;
    private Boolean isActive;
}
