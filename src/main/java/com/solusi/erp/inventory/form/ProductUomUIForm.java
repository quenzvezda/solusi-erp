package com.solusi.erp.inventory.form;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * UI Metadata for Product UoM Conversion.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductUomUIForm {
    private String productName;
    private String productCode;
    private String fromUomName;
    private String toUomName;
}
