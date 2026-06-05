package com.solusi.erp.accountspayable.debitmemoallocation.application.usecase.command;

import java.time.LocalDate;
import java.util.List;

public record UpdateDebitMemoAllocationCommand(
        Long id,
        LocalDate allocationDate,
        String notes,
        List<DebitMemoAllocationLineCommand> lines
) {
}
