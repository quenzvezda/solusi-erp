package com.solusi.erp.core.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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

    public String getCreatedInitials() {
        return getInitials(createdByName);
    }

    public String getUpdatedInitials() {
        return getInitials(updatedByName);
    }

    public String getFormattedCreatedDate() {
        return formatDate(createdDate);
    }

    public String getFormattedUpdatedDate() {
        return formatDate(updatedDate);
    }

    private String getInitials(String name) {
        if (name == null || name.isBlank()) return "?";
        String[] parts = name.split("[._ ]");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                sb.append(part.charAt(0));
            }
            if (sb.length() >= 2) break;
        }
        return sb.length() > 0 ? sb.toString().toUpperCase() : "?";
    }

    private String formatDate(LocalDateTime date) {
        if (date == null) return "-";
        return date.format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm"));
    }
}
