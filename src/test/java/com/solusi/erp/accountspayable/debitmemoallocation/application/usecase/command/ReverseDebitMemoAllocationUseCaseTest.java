package com.solusi.erp.accountspayable.debitmemoallocation.application.usecase.command;

import com.solusi.erp.accounting.journal.application.usecase.command.ReversePostedJournalCommand;
import com.solusi.erp.accounting.journal.application.usecase.command.ReversePostedJournalUseCase;
import com.solusi.erp.accounting.journal.domain.model.JournalEntry;
import com.solusi.erp.accountspayable.debitmemo.domain.model.DebitMemo;
import com.solusi.erp.accountspayable.debitmemo.domain.model.DebitMemoLine;
import com.solusi.erp.accountspayable.debitmemo.domain.model.DebitMemoSettlementStatus;
import com.solusi.erp.accountspayable.debitmemo.domain.repository.DebitMemoRepository;
import com.solusi.erp.accountspayable.debitmemoallocation.domain.model.DebitMemoAllocation;
import com.solusi.erp.accountspayable.debitmemoallocation.domain.model.DebitMemoAllocationLine;
import com.solusi.erp.accountspayable.debitmemoallocation.domain.model.DebitMemoAllocationStatus;
import com.solusi.erp.accountspayable.debitmemoallocation.domain.port.DebitMemoAllocationSourcePort;
import com.solusi.erp.accountspayable.debitmemoallocation.domain.repository.DebitMemoAllocationRepository;
import com.solusi.erp.accountspayable.vendorpayment.domain.port.VendorBillPaymentUpdatePort;
import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReverseDebitMemoAllocationUseCaseTest {

    @Mock private DebitMemoAllocationRepository repository;
    @Mock private DebitMemoRepository debitMemoRepository;
    @Mock private DebitMemoAllocationSourcePort sourcePort;
    @Mock private ReversePostedJournalUseCase reversePostedJournalUseCase;
    @Mock private VendorBillPaymentUpdatePort vendorBillPaymentUpdatePort;

    private ReverseDebitMemoAllocationUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new ReverseDebitMemoAllocationUseCaseImpl(
                repository,
                debitMemoRepository,
                sourcePort,
                reversePostedJournalUseCase,
                vendorBillPaymentUpdatePort);
    }

    @Test
    void execute_should_reverse_journal_mark_allocation_reversed_and_restore_settlement() {
        DebitMemoAllocation allocation = confirmedAllocation(900L);
        DebitMemo debitMemo = debitMemo(DebitMemoSettlementStatus.SETTLED);
        JournalEntry reversalJournal = journalEntry(901L);
        when(repository.findById(700L)).thenReturn(Optional.of(allocation));
        when(repository.sumConfirmedAppliedByDebitMemoId(100L)).thenReturn(bd("100.0000"));
        when(reversePostedJournalUseCase.execute(any())).thenReturn(reversalJournal);
        when(repository.save(any(DebitMemoAllocation.class))).thenAnswer(inv -> inv.getArgument(0));
        when(debitMemoRepository.findById(100L)).thenReturn(Optional.of(debitMemo));
        when(debitMemoRepository.save(any(DebitMemo.class))).thenAnswer(inv -> inv.getArgument(0));

        useCase.execute(new ReverseDebitMemoAllocationCommand(
                700L,
                LocalDate.of(2026, 6, 6),
                "wrong allocation"
        ));

        assertThat(allocation.getStatus()).isEqualTo(DebitMemoAllocationStatus.REVERSED);
        assertThat(allocation.getReversalJournalEntryId()).isEqualTo(901L);
        assertThat(allocation.getReversalDate()).isEqualTo(LocalDate.of(2026, 6, 6));
        assertThat(allocation.getReversalReason()).isEqualTo("wrong allocation");
        assertThat(debitMemo.getSettlementStatus()).isEqualTo(DebitMemoSettlementStatus.PARTIALLY_SETTLED);

        ArgumentCaptor<ReversePostedJournalCommand> commandCaptor =
                ArgumentCaptor.forClass(ReversePostedJournalCommand.class);
        verify(reversePostedJournalUseCase).execute(commandCaptor.capture());
        assertThat(commandCaptor.getValue().originalJournalEntryId()).isEqualTo(900L);
        assertThat(commandCaptor.getValue().reversalDate()).isEqualTo(LocalDate.of(2026, 6, 6));
        assertThat(commandCaptor.getValue().description()).isEqualTo("wrong allocation");

        InOrder inOrder = inOrder(sourcePort, reversePostedJournalUseCase, repository, debitMemoRepository,
                vendorBillPaymentUpdatePort);
        inOrder.verify(sourcePort).lockDebitMemo(100L);
        inOrder.verify(sourcePort).lockVendorBills(List.of(501L));
        inOrder.verify(reversePostedJournalUseCase).execute(any(ReversePostedJournalCommand.class));
        inOrder.verify(repository).save(allocation);
        inOrder.verify(debitMemoRepository).save(debitMemo);
        inOrder.verify(vendorBillPaymentUpdatePort).updateSettlementStatus(List.of(501L));
    }

    @Test
    void execute_should_restore_debit_memo_to_open_when_no_confirmed_consumption_remains() {
        DebitMemoAllocation allocation = confirmedAllocation(900L);
        DebitMemo debitMemo = debitMemo(DebitMemoSettlementStatus.PARTIALLY_SETTLED);
        JournalEntry reversalJournal = journalEntry(901L);
        when(repository.findById(700L)).thenReturn(Optional.of(allocation));
        when(repository.sumConfirmedAppliedByDebitMemoId(100L)).thenReturn(bd("40.0000"));
        when(reversePostedJournalUseCase.execute(any())).thenReturn(reversalJournal);
        when(repository.save(any(DebitMemoAllocation.class))).thenAnswer(inv -> inv.getArgument(0));
        when(debitMemoRepository.findById(100L)).thenReturn(Optional.of(debitMemo));
        when(debitMemoRepository.save(any(DebitMemo.class))).thenAnswer(inv -> inv.getArgument(0));

        useCase.execute(new ReverseDebitMemoAllocationCommand(700L, LocalDate.of(2026, 6, 6), "wrong allocation"));

        assertThat(debitMemo.getSettlementStatus()).isEqualTo(DebitMemoSettlementStatus.OPEN);
    }

    @Test
    void execute_should_require_reversal_date_and_reason() {
        assertThatThrownBy(() -> useCase.execute(new ReverseDebitMemoAllocationCommand(700L, null, "reason")))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.debit-memo-allocation.reversal-date-required");
        assertThatThrownBy(() -> useCase.execute(new ReverseDebitMemoAllocationCommand(700L, LocalDate.now(), " ")))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.debit-memo-allocation.reversal-reason-required");

        verifyNoInteractions(repository, reversePostedJournalUseCase, debitMemoRepository, vendorBillPaymentUpdatePort);
    }

    @Test
    void execute_should_not_save_when_reverse_journal_rejects_closed_period() {
        DebitMemoAllocation allocation = confirmedAllocation(900L);
        when(repository.findById(700L)).thenReturn(Optional.of(allocation));
        when(repository.sumConfirmedAppliedByDebitMemoId(100L)).thenReturn(bd("100.0000"));
        doThrow(new DomainException("msg.error.period.not.open"))
                .when(reversePostedJournalUseCase).execute(any(ReversePostedJournalCommand.class));

        assertThatThrownBy(() -> useCase.execute(new ReverseDebitMemoAllocationCommand(
                700L,
                LocalDate.of(2026, 6, 6),
                "wrong allocation"
        ))).isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.period.not.open");

        verify(repository, never()).save(any());
        verifyNoInteractions(debitMemoRepository, vendorBillPaymentUpdatePort);
    }

    @Test
    void execute_should_reject_already_reversed_allocation_before_journal_reversal() {
        DebitMemoAllocation allocation = allocation(DebitMemoAllocationStatus.REVERSED, 900L);
        when(repository.findById(700L)).thenReturn(Optional.of(allocation));

        assertThatThrownBy(() -> useCase.execute(new ReverseDebitMemoAllocationCommand(
                700L,
                LocalDate.of(2026, 6, 6),
                "wrong allocation"
        ))).isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.debit-memo-allocation.reverse.only-confirmed");

        verifyNoInteractions(sourcePort, reversePostedJournalUseCase, debitMemoRepository, vendorBillPaymentUpdatePort);
        verify(repository, never()).save(any());
    }

    @Test
    void execute_should_reject_missing_apply_journal_before_journal_reversal() {
        DebitMemoAllocation allocation = confirmedAllocation(null);
        when(repository.findById(700L)).thenReturn(Optional.of(allocation));

        assertThatThrownBy(() -> useCase.execute(new ReverseDebitMemoAllocationCommand(
                700L,
                LocalDate.of(2026, 6, 6),
                "wrong allocation"
        ))).isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.debit-memo-allocation.apply-journal-required");

        verifyNoInteractions(sourcePort, reversePostedJournalUseCase, debitMemoRepository, vendorBillPaymentUpdatePort);
        verify(repository, never()).save(any());
    }

    private static DebitMemoAllocation confirmedAllocation(Long applyJournalEntryId) {
        return allocation(DebitMemoAllocationStatus.CONFIRMED, applyJournalEntryId);
    }

    private static DebitMemoAllocation allocation(DebitMemoAllocationStatus status, Long applyJournalEntryId) {
        return DebitMemoAllocation.reconstitute(
                new AuditMetadata(700L, 1L, null, null, null, null),
                "DMA-001",
                100L,
                "DM-100",
                LocalDate.of(2026, 6, 5),
                status,
                applyJournalEntryId,
                null,
                null,
                null,
                "notes",
                List.of(new DebitMemoAllocationLine(
                        null,
                        501L,
                        "VB-501",
                        bd("100.0000"),
                        bd("100.0000"),
                        bd("40.0000"),
                        bd("36.0000"),
                        bd("4.0000"),
                        bd("36.0000"),
                        bd("4.0000"),
                        bd("1.250000"),
                        bd("50.0000"),
                        bd("10.0000"),
                        bd("0.0000")
                ))
        );
    }

    private static DebitMemo debitMemo(DebitMemoSettlementStatus status) {
        return DebitMemo.reconstitute(
                new AuditMetadata(100L, 1L, null, null, null, null),
                "DM-100",
                900L,
                "PR-900",
                10L,
                1L,
                LocalDate.of(2026, 6, 5),
                status,
                null,
                null,
                null,
                null,
                null,
                List.of(new DebitMemoLine(null, 901L, 301L, BigDecimal.ONE, 401L,
                        bd("90.0000"), bd("10.0000"), bd("90.0000"), bd("10.0000")))
        );
    }

    private static JournalEntry journalEntry(Long id) {
        JournalEntry journalEntry = mock(JournalEntry.class);
        when(journalEntry.getId()).thenReturn(id);
        return journalEntry;
    }

    private static BigDecimal bd(String value) {
        return new BigDecimal(value);
    }
}
