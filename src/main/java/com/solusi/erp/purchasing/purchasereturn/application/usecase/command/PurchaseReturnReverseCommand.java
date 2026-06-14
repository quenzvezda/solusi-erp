package com.solusi.erp.purchasing.purchasereturn.application.usecase.command;

import java.time.LocalDate;
import java.util.List;

public record PurchaseReturnReverseCommand(
        Long purchaseReturnId,
        LocalDate reversalDate,
        String reversalReason,
        Long reversedByUserId,
        List<PurchaseReturnReverseLineCommand> lines
) {
    public List<PurchaseReturnReverseLineCommand> linesOrEmpty() {
        return lines == null ? List.of() : List.copyOf(lines);
    }
}
