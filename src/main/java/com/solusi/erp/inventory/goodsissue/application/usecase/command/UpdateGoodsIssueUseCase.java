package com.solusi.erp.inventory.goodsissue.application.usecase.command;

import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssue;

import java.time.LocalDate;
import java.util.List;

@FunctionalInterface
public interface UpdateGoodsIssueUseCase {
    GoodsIssue execute(Long id, LocalDate issueDate, String note, List<GoodsIssueLineCommand> lines);
}
