package com.solusi.erp.master.dto;

import com.solusi.erp.master.model.AddressType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartyAddressRequest {
    private Long id;
    private Set<AddressType> types;
    private String addressLine1;
    private Long cityId;
    private String postalCode;
    @Builder.Default
    private Boolean isActive = true;
    @Builder.Default
    private Boolean isDefault = false;
}
