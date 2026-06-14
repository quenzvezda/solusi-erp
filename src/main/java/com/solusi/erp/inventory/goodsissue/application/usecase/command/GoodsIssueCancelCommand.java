package com.solusi.erp.inventory.goodsissue.application.usecase.command;

import java.time.LocalDate;
import java.util.List;

public record GoodsIssueCancelCommand(
        Long goodsIssueId,
        LocalDate reversalDate,
        String reason,
        List<GoodsIssueCancelLineCommand> lines
) {
    public List<GoodsIssueCancelLineCommand> linesOrEmpty() {
        return lines == null ? List.of() : List.copyOf(lines);
    }
}
