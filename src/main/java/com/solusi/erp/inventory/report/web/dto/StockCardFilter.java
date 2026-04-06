package com.solusi.erp.inventory.report.web.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * Filter DTO for Stock Card report.
 */
@Data
public class StockCardFilter {
    private Long productId;
    private Long containerId;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
}
