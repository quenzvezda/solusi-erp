package com.solusi.erp.inventory.dto;

import com.solusi.erp.inventory.model.UomType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for displaying Unit of Measure data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnitOfMeasureResponse {
    private Long id;
    private String code;
    private String name;
    private UomType type;
    private LocalDateTime createdDate;
    private String createdBy;
    private LocalDateTime updatedDate;
    private String updatedBy;
    private Integer version;
}
