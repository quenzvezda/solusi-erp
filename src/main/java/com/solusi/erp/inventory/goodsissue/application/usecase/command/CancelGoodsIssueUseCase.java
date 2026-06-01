package com.solusi.erp.inventory.goodsissue.application.usecase.command;

@FunctionalInterface
public interface CancelGoodsIssueUseCase {
    void execute(Long id, String reason);
}
