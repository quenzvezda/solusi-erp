package com.solusi.erp.master.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import com.solusi.erp.master.model.PartyType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * DTO for creating or updating a Party.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PartyRequest extends BaseAuditResponse {

    private String salutation;

    @Size(max = 50, message = "{validation.size}")
    private String code;

    @NotBlank(message = "{validation.notblank}")
    @Size(max = 255, message = "{validation.size}")
    private String name;

    @NotNull(message = "{validation.notnull}")
    private PartyType type;

    private String notes;

    @Builder.Default
    private Boolean isActive = true;

    @Size(max = 100, message = "{validation.size}")
    private String email;

    @Size(max = 50, message = "{validation.size}")
    private String phone;

    @Builder.Default
    private Set<Long> roleIds = new HashSet<>();

    @Builder.Default
    private List<PartyIdentificationRequest> identifications = new ArrayList<>();

    @Builder.Default
    private List<PartyAddressRequest> addresses = new ArrayList<>();

    @Builder.Default
    private List<PartyContactRequest> contacts = new ArrayList<>();
}
