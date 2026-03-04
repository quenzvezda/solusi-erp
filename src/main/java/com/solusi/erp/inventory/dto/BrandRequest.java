package com.solusi.erp.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating or updating a Brand.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrandRequest {

    private Long id;

    @Size(max = 50, message = "{validation.size}")
    private String code;

    @NotBlank(message = "{validation.notblank}")
    @Size(max = 100, message = "{validation.size}")
    private String name;

    private String note;
}
