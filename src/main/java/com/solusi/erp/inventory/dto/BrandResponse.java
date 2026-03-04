package com.solusi.erp.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for displaying Brand data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrandResponse {
    private Long id;
    private String code;
    private String name;
    private String note;
    private LocalDateTime createdDate;
    private String createdBy;
    private LocalDateTime updatedDate;
    private String updatedBy;
    private Integer version;
}
