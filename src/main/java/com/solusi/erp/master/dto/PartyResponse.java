package com.solusi.erp.master.dto;

import com.solusi.erp.master.model.PartyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartyResponse {
    private Long id;
    private String salutation;
    private String code;
    private String name;
    private PartyType type;
    private String notes;
    private Boolean isActive;
    private String email;
    private String phone;

    private Set<String> roleCodes;
    private Set<String> roleNames;

    private List<PartyIdentificationResponse> identifications;
    private List<PartyAddressResponse> addresses;
    private List<PartyContactResponse> contacts;

    private LocalDateTime createdDate;
    private String createdBy;
    private LocalDateTime updatedDate;
    private String updatedBy;
    private Integer version;
}
