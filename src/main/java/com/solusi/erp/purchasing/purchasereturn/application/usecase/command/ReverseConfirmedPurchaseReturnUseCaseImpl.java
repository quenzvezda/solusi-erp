package com.solusi.erp.purchasing.purchasereturn.application.usecase.command;

import com.solusi.erp.accounting.period.application.usecase.query.EnsureOpenPeriodForDateUseCase;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturn;
import com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturnReversalLine;
import com.solusi.erp.purchasing.purchasereturn.domain.port.PurchaseReturnDebitMemoReversalPort;
import com.solusi.erp.purchasing.purchasereturn.domain.port.PurchaseReturnInventoryReversalPort;
import com.solusi.erp.purchasing.purchasereturn.domain.port.PurchaseReturnJournalReversalPort;
import com.solusi.erp.purchasing.purchasereturn.domain.port.PurchaseReturnStockReversalTarget;
import com.solusi.erp.purchasing.purchasereturn.domain.repository.PurchaseReturnRepository;

import java.util.List;

public class ReverseConfirmedPurchaseReturnUseCaseImpl implements ReverseConfirmedPurchaseReturnUseCase {

    private final PurchaseReturnRepository purchaseReturnRepository;
    private final PurchaseReturnDebitMemoReversalPort debitMemoReversalPort;
    private final PurchaseReturnInventoryReversalPort inventoryReversalPort;
    private final PurchaseReturnJournalReversalPort journalReversalPort;
    private final EnsureOpenPeriodForDateUseCase ensureOpenPeriodForDateUseCase;

    public ReverseConfirmedPurchaseReturnUseCaseImpl(
            PurchaseReturnRepository purchaseReturnRepository,
            PurchaseReturnDebitMemoReversalPort debitMemoReversalPort,
            PurchaseReturnInventoryReversalPort inventoryReversalPort,
            PurchaseReturnJournalReversalPort journalReversalPort,
            EnsureOpenPeriodForDateUseCase ensureOpenPeriodForDateUseCase) {
        this.purchaseReturnRepository = purchaseReturnRepository;
        this.debitMemoReversalPort = debitMemoReversalPort;
        this.inventoryReversalPort = inventoryReversalPort;
        this.journalReversalPort = journalReversalPort;
        this.ensureOpenPeriodForDateUseCase = ensureOpenPeriodForDateUseCase;
    }

    @Override
    public PurchaseReturn execute(PurchaseReturnReverseCommand command) {
        validateCommand(command);

        PurchaseReturn purchaseReturn = purchaseReturnRepository.findByIdForUpdate(command.purchaseReturnId())
                .orElseThrow(() -> new DomainException("msg.error.purchase-return.not-found"));
        purchaseReturn.validateReversalHeader(
                command.reversalDate(),
                command.reversalReason(),
                command.reversedByUserId()
        );

        Long debitMemoId = debitMemoReversalPort.validateReversibleAndLock(purchaseReturn.getId());
        ensureOpenPeriodForDateUseCase.execute(command.reversalDate());

        String reason = command.reversalReason().trim();
        List<PurchaseReturnReversalLine> reversalLines = inventoryReversalPort.reverseStockMovements(
                purchaseReturn,
                command.reversalDate(),
                reason,
                toStockTargets(command.linesOrEmpty())
        );
        Long reversalJournalEntryId = journalReversalPort.reverseOriginalPurchaseReturnJournal(
                purchaseReturn.getId(),
                command.reversalDate(),
                reason
        );
        inventoryReversalPort.cancelGeneratedGoodsIssue(purchaseReturn, command.reversalDate(), reason);
        debitMemoReversalPort.cancelDebitMemo(debitMemoId);
        purchaseReturn.reverse(
                command.reversalDate(),
                reason,
                command.reversedByUserId(),
                reversalJournalEntryId,
                reversalLines
        );
        return purchaseReturnRepository.save(purchaseReturn);
    }

    private void validateCommand(PurchaseReturnReverseCommand command) {
        if (command == null || command.purchaseReturnId() == null) {
            throw new DomainException("msg.error.purchase-return.not-found");
        }
        if (command.linesOrEmpty().isEmpty()) {
            throw new DomainException("msg.error.purchase-return.reverse.lines-required");
        }
    }

    private List<PurchaseReturnStockReversalTarget> toStockTargets(List<PurchaseReturnReverseLineCommand> lines) {
        return lines.stream()
                .map(line -> new PurchaseReturnStockReversalTarget(
                        line.originalMovementId(),
                        line.targetContainerId()))
                .toList();
    }
}
