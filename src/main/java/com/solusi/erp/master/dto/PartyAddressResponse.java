package com.solusi.erp.master.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import com.solusi.erp.master.model.AddressType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PartyAddressResponse extends BaseAuditResponse {
    private Set<AddressType> types;
    private String addressLine1;
    private Long cityId;
    private String cityName;
    private String provinceName;
    private String countryName;
    private String postalCode;
    private Boolean isActive;
    private Boolean isDefault;
}

