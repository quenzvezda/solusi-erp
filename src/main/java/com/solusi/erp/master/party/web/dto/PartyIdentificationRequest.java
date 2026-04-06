package com.solusi.erp.master.party.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PartyIdentificationRequest {
    private Long id;
    private Long typeId;
    private String idNumber;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate issuedDate;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate expiryDate;
    private Boolean isActive = true;
    private Boolean isDefault = false;
}
