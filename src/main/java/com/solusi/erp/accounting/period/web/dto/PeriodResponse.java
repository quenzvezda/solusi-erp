package com.solusi.erp.accounting.period.web.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class PeriodResponse {
    private Long id;
    private String code;
    private String name;
    private Integer periodNumber;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
}
