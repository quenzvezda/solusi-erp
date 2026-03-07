package com.solusi.erp.master.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartyRoleTypeRequest {
    private Long id;
    private String code;
    private String name;
}
