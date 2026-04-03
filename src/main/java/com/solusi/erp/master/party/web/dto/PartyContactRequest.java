package com.solusi.erp.master.party.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PartyContactRequest {
    private Long id;
    private String label;
    private String mobile;
    private String phone;
    private String email;
    private Boolean isActive = true;
    private Boolean isDefault = false;
}
