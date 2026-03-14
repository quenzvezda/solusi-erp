package com.solusi.erp.core.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Base abstract class for Response DTOs that expose audit fields.
 * Eliminates boilerplate id, version, and auditing fields from individual DTOs.
 */
@Getter
@Setter
public abstract class BaseAuditResponse {
    private Long id;
    private Integer version;
    
    private String createdByName;
    private LocalDateTime createdDate;
    
    private String updatedByName;
    private LocalDateTime updatedDate;
}
