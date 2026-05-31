package com.solusi.erp.accounting.journal.web.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class ReverseJournalRequest {
    @NotNull
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate postingDate;
}
