package com.solusi.erp.master.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PartyIdentificationResponse extends BaseAuditResponse {
    private Long typeId;
    private String typeCode;
    private String typeName;
    private String idNumber;
    private LocalDate issuedDate;
    private LocalDate expiryDate;
    private Boolean isActive;
    private Boolean isDefault;
}

