package com.solusi.erp.accountspayable.debitmemoallocation.application.usecase.command;

import com.solusi.erp.accountspayable.debitmemoallocation.application.usecase.query.DebitMemoAllocationDetailView;

public interface UpdateDebitMemoAllocationUseCase {
    DebitMemoAllocationDetailView execute(UpdateDebitMemoAllocationCommand command);
}
