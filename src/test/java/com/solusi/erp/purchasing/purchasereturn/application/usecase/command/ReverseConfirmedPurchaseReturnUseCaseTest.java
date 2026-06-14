package com.solusi.erp.purchasing.purchasereturn.application.usecase.command;

import com.solusi.erp.accounting.period.application.usecase.query.EnsureOpenPeriodForDateUseCase;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturn;
import com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturnReason;
import com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturnReversalLine;
import com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturnStatus;
import com.solusi.erp.purchasing.purchasereturn.domain.port.PurchaseReturnDebitMemoReversalPort;
import com.solusi.erp.purchasing.purchasereturn.domain.port.PurchaseReturnInventoryReversalPort;
import com.solusi.erp.purchasing.purchasereturn.domain.port.PurchaseReturnJournalReversalPort;
import com.solusi.erp.purchasing.purchasereturn.domain.repository.PurchaseReturnRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static com.solusi.erp.purchasing.purchasereturn.application.usecase.command.CreatePurchaseReturnUseCaseTest.persisted;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReverseConfirmedPurchaseReturnUseCaseTest {

    @Mock private PurchaseReturnRepository purchaseReturnRepository;
    @Mock private PurchaseReturnDebitMemoReversalPort debitMemoReversalPort;
    @Mock private PurchaseReturnInventoryReversalPort inventoryReversalPort;
    @Mock private PurchaseReturnJournalReversalPort journalReversalPort;
    @Mock private EnsureOpenPeriodForDateUseCase ensureOpenPeriodForDateUseCase;

    @Test
    void execute_confirmedReturn_reversesDownstreamThenMarksPurchaseReturnReversed() {
        PurchaseReturn purchaseReturn = confirmed();
        PurchaseReturnReversalLine snapshot = reversalLine();
        when(purchaseReturnRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(purchaseReturn));
        when(debitMemoReversalPort.validateReversibleAndLock(1L)).thenReturn(70L);
        when(inventoryReversalPort.reverseStockMovements(
                eq(purchaseReturn), eq(LocalDate.of(2026, 6, 2)), eq("Full reversal"), anyList()))
                .thenReturn(List.of(snapshot));
        when(journalReversalPort.reverseOriginalPurchaseReturnJournal(1L, LocalDate.of(2026, 6, 2), "Full reversal"))
                .thenReturn(88L);
        when(purchaseReturnRepository.save(purchaseReturn)).thenReturn(purchaseReturn);

        PurchaseReturn result = useCase().execute(command());

        assertThat(result.getStatus()).isEqualTo(PurchaseReturnStatus.REVERSED);
        assertThat(result.getReversalDate()).isEqualTo(LocalDate.of(2026, 6, 2));
        assertThat(result.getReversalReason()).isEqualTo("Full reversal");
        assertThat(result.getReversedByUserId()).isEqualTo(77L);
        assertThat(result.getReversalJournalEntryId()).isEqualTo(88L);
        assertThat(result.getReversalLines()).containsExactly(snapshot);
        InOrder order = inOrder(
                debitMemoReversalPort,
                ensureOpenPeriodForDateUseCase,
                inventoryReversalPort,
                journalReversalPort,
                purchaseReturnRepository);
        order.verify(debitMemoReversalPort).validateReversibleAndLock(1L);
        order.verify(ensureOpenPeriodForDateUseCase).execute(LocalDate.of(2026, 6, 2));
        order.verify(inventoryReversalPort).reverseStockMovements(
                eq(purchaseReturn), eq(LocalDate.of(2026, 6, 2)), eq("Full reversal"), anyList());
        order.verify(journalReversalPort).reverseOriginalPurchaseReturnJournal(1L, LocalDate.of(2026, 6, 2), "Full reversal");
        verify(inventoryReversalPort).cancelGeneratedGoodsIssue(purchaseReturn, LocalDate.of(2026, 6, 2), "Full reversal");
        verify(debitMemoReversalPort).cancelDebitMemo(70L);
        order.verify(purchaseReturnRepository).save(purchaseReturn);
    }

    @Test
    void execute_invalidStatus_rejectsBeforeDownstreamGuards() {
        PurchaseReturn purchaseReturn = persisted(PurchaseReturnStatus.APPROVED);
        when(purchaseReturnRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(purchaseReturn));

        assertKey(() -> useCase().execute(command()), "msg.error.purchase-return.reverse.invalid-status");

        verifyNoDownstreamSideEffects();
    }

    @Test
    void execute_missingGeneratedGoodsIssue_rejectsBeforeDownstreamGuards() {
        PurchaseReturn purchaseReturn = confirmedWithoutGoodsIssue();
        when(purchaseReturnRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(purchaseReturn));

        assertKey(() -> useCase().execute(command()), "msg.error.purchase-return.reverse.gi-required");

        verifyNoDownstreamSideEffects();
    }

    @Test
    void execute_activeConfirmedDebitMemoAllocation_rejectsBeforeStockAndJournal() {
        PurchaseReturn purchaseReturn = confirmed();
        when(purchaseReturnRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(purchaseReturn));
        doThrow(new DomainException("msg.error.purchase-return.reverse.debit-memo-has-consumption"))
                .when(debitMemoReversalPort).validateReversibleAndLock(1L);

        assertKey(() -> useCase().execute(command()), "msg.error.purchase-return.reverse.debit-memo-has-consumption");

        verify(ensureOpenPeriodForDateUseCase, never()).execute(any());
        verifyNoStockJournalDebitMemoCancelOrSave();
    }

    @Test
    void execute_debitMemoNotFullyOpen_rejectsBeforeStockAndJournal() {
        PurchaseReturn purchaseReturn = confirmed();
        when(purchaseReturnRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(purchaseReturn));
        doThrow(new DomainException("msg.error.purchase-return.reverse.debit-memo-not-fully-open"))
                .when(debitMemoReversalPort).validateReversibleAndLock(1L);

        assertKey(() -> useCase().execute(command()), "msg.error.purchase-return.reverse.debit-memo-not-fully-open");

        verify(ensureOpenPeriodForDateUseCase, never()).execute(any());
        verifyNoStockJournalDebitMemoCancelOrSave();
    }

    @Test
    void execute_closedPeriod_rejectsBeforeStockAndJournal() {
        PurchaseReturn purchaseReturn = confirmed();
        when(purchaseReturnRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(purchaseReturn));
        when(debitMemoReversalPort.validateReversibleAndLock(1L)).thenReturn(70L);
        doThrow(new DomainException("msg.error.period.closed"))
                .when(ensureOpenPeriodForDateUseCase).execute(LocalDate.of(2026, 6, 2));

        assertKey(() -> useCase().execute(command()), "msg.error.period.closed");

        verifyNoStockJournalDebitMemoCancelOrSave();
    }

    @Test
    void execute_missingOriginalJournal_rejectsBeforeGiCancelDebitMemoCancelAndSave() {
        PurchaseReturn purchaseReturn = confirmed();
        when(purchaseReturnRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(purchaseReturn));
        when(debitMemoReversalPort.validateReversibleAndLock(1L)).thenReturn(70L);
        when(inventoryReversalPort.reverseStockMovements(any(), any(), any(), anyList()))
                .thenReturn(List.of(reversalLine()));
        doThrow(new DomainException("msg.error.purchase-return.reverse.journal-not-found"))
                .when(journalReversalPort).reverseOriginalPurchaseReturnJournal(1L, LocalDate.of(2026, 6, 2), "Full reversal");

        assertKey(() -> useCase().execute(command()), "msg.error.purchase-return.reverse.journal-not-found");

        verify(inventoryReversalPort, never()).cancelGeneratedGoodsIssue(any(), any(), any());
        verify(debitMemoReversalPort, never()).cancelDebitMemo(any());
        verify(purchaseReturnRepository, never()).save(any());
    }

    @Test
    void execute_alreadyReversedStockMovement_rejectsBeforeJournalAndCancels() {
        PurchaseReturn purchaseReturn = confirmed();
        when(purchaseReturnRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(purchaseReturn));
        when(debitMemoReversalPort.validateReversibleAndLock(1L)).thenReturn(70L);
        doThrow(new DomainException("msg.error.stock.reversal.already.reversed"))
                .when(inventoryReversalPort).reverseStockMovements(any(), any(), any(), anyList());

        assertKey(() -> useCase().execute(command()), "msg.error.stock.reversal.already.reversed");

        verify(journalReversalPort, never()).reverseOriginalPurchaseReturnJournal(any(), any(), any());
        verify(inventoryReversalPort, never()).cancelGeneratedGoodsIssue(any(), any(), any());
        verify(debitMemoReversalPort, never()).cancelDebitMemo(any());
        verify(purchaseReturnRepository, never()).save(any());
    }

    @Test
    void execute_serialAlreadyOnHand_rejectsBeforeJournalAndCancels() {
        PurchaseReturn purchaseReturn = confirmed();
        when(purchaseReturnRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(purchaseReturn));
        when(debitMemoReversalPort.validateReversibleAndLock(1L)).thenReturn(70L);
        doThrow(new DomainException("msg.error.stock.reversal.serial.on.hand"))
                .when(inventoryReversalPort).reverseStockMovements(any(), any(), any(), anyList());

        assertKey(() -> useCase().execute(command()), "msg.error.stock.reversal.serial.on.hand");

        verify(journalReversalPort, never()).reverseOriginalPurchaseReturnJournal(any(), any(), any());
        verify(inventoryReversalPort, never()).cancelGeneratedGoodsIssue(any(), any(), any());
        verify(debitMemoReversalPort, never()).cancelDebitMemo(any());
        verify(purchaseReturnRepository, never()).save(any());
    }

    private void assertKey(ThrowingCallable callable, String expectedKey) {
        assertThatThrownBy(callable::call)
                .isInstanceOf(DomainException.class)
                .satisfies(ex -> assertThat(((DomainException) ex).getKey()).isEqualTo(expectedKey));
    }

    private void verifyNoDownstreamSideEffects() {
        verify(debitMemoReversalPort, never()).validateReversibleAndLock(any());
        verify(ensureOpenPeriodForDateUseCase, never()).execute(any());
        verifyNoStockJournalDebitMemoCancelOrSave();
    }

    private void verifyNoStockJournalDebitMemoCancelOrSave() {
        verify(inventoryReversalPort, never()).reverseStockMovements(any(), any(), any(), anyList());
        verify(journalReversalPort, never()).reverseOriginalPurchaseReturnJournal(any(), any(), any());
        verify(inventoryReversalPort, never()).cancelGeneratedGoodsIssue(any(), any(), any());
        verify(debitMemoReversalPort, never()).cancelDebitMemo(any());
        verify(purchaseReturnRepository, never()).save(any());
    }

    private PurchaseReturn confirmed() {
        PurchaseReturn purchaseReturn = persisted(PurchaseReturnStatus.APPROVED);
        purchaseReturn.confirm(500L);
        return purchaseReturn;
    }

    private PurchaseReturn confirmedWithoutGoodsIssue() {
        PurchaseReturn purchaseReturn = persisted(PurchaseReturnStatus.APPROVED);
        return PurchaseReturn.reconstitute(
                purchaseReturn.getMetadata(),
                purchaseReturn.getCode(),
                purchaseReturn.getReturnDate(),
                purchaseReturn.getReferenceType(),
                purchaseReturn.getReferenceId(),
                purchaseReturn.getReferenceCode(),
                purchaseReturn.getPurchaseOrderId(),
                purchaseReturn.getPurchaseOrderCode(),
                purchaseReturn.getSupplierId(),
                purchaseReturn.getFacilityId(),
                purchaseReturn.getCurrencyId(),
                purchaseReturn.getExchangeRate(),
                PurchaseReturnStatus.CONFIRMED,
                purchaseReturn.getReason(),
                purchaseReturn.getNote(),
                purchaseReturn.getSubmittedByUserId(),
                null,
                purchaseReturn.getLines()
        );
    }

    private PurchaseReturnReverseCommand command() {
        return new PurchaseReturnReverseCommand(
                1L,
                LocalDate.of(2026, 6, 2),
                "  Full reversal  ",
                77L,
                List.of(new PurchaseReturnReverseLineCommand(900L, 40L))
        );
    }

    private PurchaseReturnReversalLine reversalLine() {
        return PurchaseReturnReversalLine.create(11L, 900L, 40L, 10L, null, BigDecimal.ONE);
    }

    private ReverseConfirmedPurchaseReturnUseCaseImpl useCase() {
        return new ReverseConfirmedPurchaseReturnUseCaseImpl(
                purchaseReturnRepository,
                debitMemoReversalPort,
                inventoryReversalPort,
                journalReversalPort,
                ensureOpenPeriodForDateUseCase
        );
    }

    private interface ThrowingCallable {
        void call();
    }
}
