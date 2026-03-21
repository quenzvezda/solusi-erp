package com.solusi.erp.inventory.form;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * UI-only DTO for Product.
 * Holds labels and codes needed for display persistence in forms.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductUIForm {
    private String categoryName;
    private String categoryCode;
    private String categoryType;
    
    private String brandName;
    private String brandCode;
    
    private String uomName;
}
