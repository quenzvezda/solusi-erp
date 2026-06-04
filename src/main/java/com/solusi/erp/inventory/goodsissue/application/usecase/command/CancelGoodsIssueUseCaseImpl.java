package com.solusi.erp.inventory.goodsissue.application.usecase.command;

import com.solusi.erp.accounting.journal.application.usecase.command.ReversePostedJournalCommand;
import com.solusi.erp.accounting.journal.application.usecase.command.ReversePostedJournalUseCase;
import com.solusi.erp.accounting.journal.domain.model.JournalEntry;
import com.solusi.erp.accounting.journal.domain.repository.JournalEntryRepository;
import com.solusi.erp.accounting.period.application.usecase.query.EnsureOpenPeriodForDateUseCase;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssue;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssueReferenceType;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssueStatus;
import com.solusi.erp.inventory.goodsissue.domain.repository.GoodsIssueRepository;
import com.solusi.erp.inventory.stock.domain.model.MovementType;
import com.solusi.erp.inventory.stock.domain.model.ReferenceType;
import com.solusi.erp.inventory.stock.domain.model.StockMovementReversalRequest;
import com.solusi.erp.inventory.stock.domain.port.StockMovementReversalService;
import com.solusi.erp.inventory.stock.infrastructure.persistence.InventoryMovementEntity;
import com.solusi.erp.inventory.stock.infrastructure.persistence.InventoryMovementJpaRepository;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CancelGoodsIssueUseCaseImpl implements CancelGoodsIssueUseCase {

    private static final String GOODS_ISSUE_SOURCE_TYPE = "GOODS_ISSUE";

    private final GoodsIssueRepository goodsIssueRepository;
    private final EnsureOpenPeriodForDateUseCase ensureOpenPeriodForDateUseCase;
    private final StockMovementReversalService stockMovementReversalService;
    private final InventoryMovementJpaRepository movementRepository;
    private final JournalEntryRepository journalEntryRepository;
    private final ReversePostedJournalUseCase reversePostedJournalUseCase;
    private final GoodsIssueInUseChecker inUseChecker;

    public CancelGoodsIssueUseCaseImpl(GoodsIssueRepository goodsIssueRepository,
                                       EnsureOpenPeriodForDateUseCase ensureOpenPeriodForDateUseCase,
                                       StockMovementReversalService stockMovementReversalService,
                                       InventoryMovementJpaRepository movementRepository,
                                       JournalEntryRepository journalEntryRepository,
                                       ReversePostedJournalUseCase reversePostedJournalUseCase,
                                       GoodsIssueInUseChecker inUseChecker) {
        this.goodsIssueRepository = goodsIssueRepository;
        this.ensureOpenPeriodForDateUseCase = ensureOpenPeriodForDateUseCase;
        this.stockMovementReversalService = stockMovementReversalService;
        this.movementRepository = movementRepository;
        this.journalEntryRepository = journalEntryRepository;
        this.reversePostedJournalUseCase = reversePostedJournalUseCase;
        this.inUseChecker = inUseChecker;
    }

    @Override
    public void execute(GoodsIssueCancelCommand command) {
        if (command == null || command.goodsIssueId() == null) {
            throw new DomainException("msg.error.gi.notfound");
        }
        if (command.reversalDate() == null) {
            throw new DomainException("msg.error.gi.cancel.reversal.date.required");
        }

        GoodsIssue issue = goodsIssueRepository.findById(command.goodsIssueId())
                .orElseThrow(() -> new DomainException("msg.error.gi.notfound"));
        if (issue.getStatus() != GoodsIssueStatus.COMPLETED) {
            throw new DomainException("msg.error.gi.cancel.only.completed");
        }
        if (issue.getReferenceType() != GoodsIssueReferenceType.MANUAL) {
            throw new DomainException("msg.error.gi.cancel.source.owned");
        }
        ensureOpenPeriodForDateUseCase.execute(command.reversalDate());
        inUseChecker.assertNotInUse(issue);

        List<InventoryMovementEntity> originalMovements = movementRepository
                .findByReferenceTypeAndReferenceIdOrderByIdAsc(ReferenceType.GOODS_ISSUE, issue.getId())
                .stream()
                .filter(this::isOutboundMovement)
                .toList();
        if (originalMovements.isEmpty()) {
            throw new DomainException("msg.error.gi.cancel.movements.notfound");
        }
        stockMovementReversalService.reverse(toReversalRequests(originalMovements, command));

        JournalEntry originalJournal = journalEntryRepository.findBySource(GOODS_ISSUE_SOURCE_TYPE, issue.getId())
                .orElseThrow(() -> new DomainException("msg.error.gi.cancel.journal.notfound"));
        reversePostedJournalUseCase.execute(new ReversePostedJournalCommand(
                originalJournal.getId(),
                command.reversalDate(),
                reversalDescription(issue, command.reason())
        ));

        issue.cancel(command.reversalDate(), command.reason());
        goodsIssueRepository.save(issue);
    }

    private List<StockMovementReversalRequest> toReversalRequests(List<InventoryMovementEntity> originalMovements,
                                                                  GoodsIssueCancelCommand command) {
        Map<Long, GoodsIssueCancelLineCommand> lineByMovementId = command.linesOrEmpty().stream()
                .filter(line -> line.originalMovementId() != null)
                .collect(Collectors.toMap(
                        GoodsIssueCancelLineCommand::originalMovementId,
                        Function.identity(),
                        (left, right) -> right
                ));
        return originalMovements.stream()
                .map(movement -> {
                    GoodsIssueCancelLineCommand line = lineByMovementId.get(movement.getId());
                    Long targetContainerId = line != null && line.targetContainerId() != null
                            ? line.targetContainerId()
                            : movement.getContainerId();
                    return new StockMovementReversalRequest(
                            movement.getId(),
                            targetContainerId,
                            command.reversalDate(),
                            command.reason()
                    );
                })
                .toList();
    }

    private boolean isOutboundMovement(InventoryMovementEntity movement) {
        if (movement.getMovementType() == MovementType.ADJUSTMENT) {
            return movement.getQuantity() != null && movement.getQuantity().signum() < 0;
        }
        return movement.getMovementType() == MovementType.ISSUE
                || movement.getMovementType() == MovementType.ISSUE_RESERVED
                || movement.getMovementType() == MovementType.TRANSFER_OUT;
    }

    private String reversalDescription(GoodsIssue issue, String reason) {
        if (reason != null && !reason.isBlank()) {
            return reason;
        }
        return "Cancel goods issue " + issue.getCode();
    }
}
