package com.solusi.erp.accountspayable.debitmemoallocation.application.usecase.command;

import com.solusi.erp.accountspayable.debitmemoallocation.application.usecase.query.DebitMemoAllocationDetailView;
import com.solusi.erp.accountspayable.debitmemoallocation.application.usecase.query.DebitMemoAllocationViewMapper;
import com.solusi.erp.accountspayable.debitmemoallocation.domain.model.DebitMemoAllocation;
import com.solusi.erp.accountspayable.debitmemoallocation.domain.port.DebitMemoAllocationSourcePort;
import com.solusi.erp.accountspayable.debitmemoallocation.domain.repository.DebitMemoAllocationRepository;
import com.solusi.erp.accountspayable.debitmemoallocation.domain.service.DebitMemoAllocationProrationService;
import com.solusi.erp.core.exception.DomainException;

public class UpdateDebitMemoAllocationUseCaseImpl implements UpdateDebitMemoAllocationUseCase {

    private final DebitMemoAllocationRepository repository;
    private final DebitMemoAllocationSourcePort sourcePort;
    private final DebitMemoAllocationDraftFactory draftFactory;

    public UpdateDebitMemoAllocationUseCaseImpl(DebitMemoAllocationRepository repository,
                                                DebitMemoAllocationSourcePort sourcePort,
                                                DebitMemoAllocationProrationService prorationService) {
        this.repository = repository;
        this.sourcePort = sourcePort;
        this.draftFactory = new DebitMemoAllocationDraftFactory(sourcePort, prorationService);
    }

    @Override
    public DebitMemoAllocationDetailView execute(UpdateDebitMemoAllocationCommand command) {
        DebitMemoAllocation allocation = repository.findById(command.id())
                .orElseThrow(() -> new DomainException("msg.error.debit-memo-allocation.not-found"));
        DebitMemoAllocationSourcePort.DebitMemoSnapshot debitMemo = sourcePort.findDebitMemoSnapshot(allocation.getDebitMemoId())
                .orElseThrow(() -> new DomainException("msg.error.debit-memo.not-found"));
        DebitMemoAllocationDraftFactory.DraftLines draftLines = draftFactory.buildLines(debitMemo, command.lines());
        allocation.update(command.allocationDate(), command.notes(), draftLines.lines());
        return DebitMemoAllocationViewMapper.toDetail(repository.save(allocation));
    }
}
