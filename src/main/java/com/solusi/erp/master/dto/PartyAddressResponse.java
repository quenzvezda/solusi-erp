package com.solusi.erp.master.dto;

import com.solusi.erp.master.model.AddressType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartyAddressResponse {
    private Long id;
    private AddressType type;
    private String addressLine1;
    private String city;
    private String province;
    private String postalCode;
    private String country;
    private Boolean isActive;
    private Boolean isDefault;
}
