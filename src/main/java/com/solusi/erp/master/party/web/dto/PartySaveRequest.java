package com.solusi.erp.master.party.web.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import com.solusi.erp.master.shared.model.PartyType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PartySaveRequest extends BaseAuditResponse {

    @Size(max = 100)
    private String code;

    private String salutation;

    @NotBlank
    @Size(max = 255)
    private String name;

    @NotNull
    private PartyType type;

    private String notes;

    private Boolean isActive = true;

    @Size(max = 100)
    private String email;

    @Size(max = 50)
    private String phone;

    private Set<Long> roleIds = new HashSet<>();

    private List<PartyContactRequest> contacts = new ArrayList<>();

    private List<PartyAddressRequest> addresses = new ArrayList<>();

    private List<PartyIdentificationRequest> identifications = new ArrayList<>();
}

