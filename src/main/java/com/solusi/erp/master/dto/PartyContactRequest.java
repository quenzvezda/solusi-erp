package com.solusi.erp.master.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartyContactRequest {
    private Long id;
    private String label;
    private String mobile;
    private String phone;
    private String email;
    @Builder.Default
    private Boolean isActive = true;
    @Builder.Default
    private Boolean isDefault = false;
}
