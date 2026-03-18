package com.solusi.erp.inventory.dto;

import lombok.Data;
import java.time.LocalDate;

/**
 * Filter DTO for Stock Card report.
 */
@Data
public class StockCardFilter {
    private Long productId;
    private Long containerId;
    private LocalDate startDate;
    private LocalDate endDate;
}
