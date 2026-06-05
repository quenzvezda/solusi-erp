package com.solusi.erp.accountspayable.debitmemoallocation.application.usecase.command;

import com.solusi.erp.accountspayable.debitmemoallocation.application.usecase.query.DebitMemoAllocationDetailView;
import com.solusi.erp.accountspayable.debitmemoallocation.application.usecase.query.DebitMemoAllocationViewMapper;
import com.solusi.erp.accountspayable.debitmemoallocation.domain.model.DebitMemoAllocation;
import com.solusi.erp.accountspayable.debitmemoallocation.domain.port.DebitMemoAllocationSourcePort;
import com.solusi.erp.accountspayable.debitmemoallocation.domain.repository.DebitMemoAllocationRepository;
import com.solusi.erp.accountspayable.debitmemoallocation.domain.service.DebitMemoAllocationProrationService;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.core.infrastructure.sequence.SequenceGeneratorService;

public class CreateDebitMemoAllocationUseCaseImpl implements CreateDebitMemoAllocationUseCase {

    private final DebitMemoAllocationRepository repository;
    private final DebitMemoAllocationSourcePort sourcePort;
    private final SequenceGeneratorService sequenceGeneratorService;
    private final DebitMemoAllocationDraftFactory draftFactory;

    public CreateDebitMemoAllocationUseCaseImpl(DebitMemoAllocationRepository repository,
                                                DebitMemoAllocationSourcePort sourcePort,
                                                SequenceGeneratorService sequenceGeneratorService,
                                                DebitMemoAllocationProrationService prorationService) {
        this.repository = repository;
        this.sourcePort = sourcePort;
        this.sequenceGeneratorService = sequenceGeneratorService;
        this.draftFactory = new DebitMemoAllocationDraftFactory(sourcePort, prorationService);
    }

    @Override
    public DebitMemoAllocationDetailView execute(CreateDebitMemoAllocationCommand command) {
        DebitMemoAllocationSourcePort.DebitMemoSnapshot debitMemo = sourcePort.findDebitMemoSnapshot(command.debitMemoId())
                .orElseThrow(() -> new DomainException("msg.error.debit-memo.not-found"));
        DebitMemoAllocationDraftFactory.DraftLines draftLines = draftFactory.buildLines(debitMemo, command.lines());
        DebitMemoAllocation allocation = DebitMemoAllocation.createNew(
                sequenceGeneratorService.generate("DEBIT_MEMO_ALLOCATION"),
                debitMemo.id(),
                debitMemo.code(),
                command.allocationDate(),
                command.notes(),
                draftLines.lines()
        );
        return DebitMemoAllocationViewMapper.toDetail(repository.save(allocation));
    }
}
