package com.solusi.erp.master.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartyRoleTypeResponse {
    private Long id;
    private String code;
    private String name;
}
