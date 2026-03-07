package com.solusi.erp.master.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartyContactResponse {
    private Long id;
    private String label;
    private String mobile;
    private String phone;
    private String email;
    private Boolean isActive;
    private Boolean isDefault;
}
