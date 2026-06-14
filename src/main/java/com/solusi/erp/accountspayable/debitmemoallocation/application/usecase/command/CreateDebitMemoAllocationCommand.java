package com.solusi.erp.accountspayable.debitmemoallocation.application.usecase.command;

import java.time.LocalDate;
import java.util.List;

public record CreateDebitMemoAllocationCommand(
        Long debitMemoId,
        LocalDate allocationDate,
        String notes,
        List<DebitMemoAllocationLineCommand> lines
) {
}
