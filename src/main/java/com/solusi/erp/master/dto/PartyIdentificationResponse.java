package com.solusi.erp.master.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartyIdentificationResponse {
    private Long id;
    private Long typeId;
    private String typeCode;
    private String typeName;
    private String idNumber;
    private LocalDate issuedDate;
    private LocalDate expiryDate;
}
