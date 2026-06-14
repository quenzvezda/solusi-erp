package com.solusi.erp.accountspayable.debitmemoallocation.application.usecase.command;

import java.time.LocalDate;

public record ReverseDebitMemoAllocationCommand(
        Long id,
        LocalDate reversalDate,
        String reversalReason
) {
}
