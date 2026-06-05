package com.solusi.erp.accountspayable.debitmemoallocation.application.usecase.command;

import com.solusi.erp.accountspayable.debitmemoallocation.application.usecase.query.DebitMemoAllocationDetailView;

public interface CreateDebitMemoAllocationUseCase {
    DebitMemoAllocationDetailView execute(CreateDebitMemoAllocationCommand command);
}
