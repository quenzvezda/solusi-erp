package com.solusi.erp.inventory.goodsissue.application.usecase.command;

public interface CancelGoodsIssueUseCase {

    void execute(GoodsIssueCancelCommand command);

    default void execute(Long id, String reason) {
        execute(new GoodsIssueCancelCommand(id, null, reason, null));
    }
}
