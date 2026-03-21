package com.solusi.erp.inventory.form;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacilityUIForm {
    private String ownerName;
    private String ownerCode;
    private String cityName;
    private String cityCode;
}
