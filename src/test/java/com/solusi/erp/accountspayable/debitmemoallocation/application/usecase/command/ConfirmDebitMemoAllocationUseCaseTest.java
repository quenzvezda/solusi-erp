package com.solusi.erp.accountspayable.debitmemoallocation.application.usecase.command;

import com.solusi.erp.accounting.journal.application.usecase.command.JournalPostingCommand;
import com.solusi.erp.accounting.journal.application.usecase.command.PostJournalForEventUseCase;
import com.solusi.erp.accounting.journal.domain.model.JournalEntry;
import com.solusi.erp.accounting.journal.domain.model.JournalVariable;
import com.solusi.erp.accounting.journal.domain.repository.JournalEntryRepository;
import com.solusi.erp.accounting.period.application.usecase.query.EnsureOpenPeriodForDateUseCase;
import com.solusi.erp.accounting.schema.domain.model.SchemaEventType;
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
class ConfirmDebitMemoAllocationUseCaseTest {

    @Mock private DebitMemoAllocationRepository repository;
    @Mock private DebitMemoRepository debitMemoRepository;
    @Mock private DebitMemoAllocationSourcePort sourcePort;
    @Mock private PostJournalForEventUseCase postJournalForEventUseCase;
    @Mock private JournalEntryRepository journalEntryRepository;
    @Mock private VendorBillPaymentUpdatePort vendorBillPaymentUpdatePort;
    @Mock private EnsureOpenPeriodForDateUseCase ensureOpenPeriodForDateUseCase;

    private ConfirmDebitMemoAllocationUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new ConfirmDebitMemoAllocationUseCaseImpl(
                repository,
                debitMemoRepository,
                sourcePort,
                postJournalForEventUseCase,
                journalEntryRepository,
                vendorBillPaymentUpdatePort,
                ensureOpenPeriodForDateUseCase);
    }

    @Test
    void execute_should_confirm_post_journal_and_update_settlement_statuses() {
        DebitMemoAllocation allocation = draftAllocation();
        DebitMemo debitMemo = debitMemo(DebitMemoSettlementStatus.OPEN);
        JournalEntry applyJournal = journalEntry(900L);
        when(repository.findById(700L)).thenReturn(Optional.of(allocation));
        when(sourcePort.findDebitMemoSnapshot(100L)).thenReturn(Optional.of(debitMemoSnapshot("100.0000", 10L, 1L)));
        when(sourcePort.findVendorBillSnapshot(501L)).thenReturn(Optional.of(vendorBillSnapshot("100.0000", 10L, 1L)));
        when(repository.sumConfirmedAppliedByDebitMemoId(100L)).thenReturn(bd("0.0000"));
        when(journalEntryRepository.findBySource("DEBIT_MEMO_ALLOCATION", 700L))
                .thenReturn(Optional.of(applyJournal));
        when(repository.save(any(DebitMemoAllocation.class))).thenAnswer(inv -> inv.getArgument(0));
        when(debitMemoRepository.findById(100L)).thenReturn(Optional.of(debitMemo));

        useCase.execute(700L);

        assertThat(allocation.getStatus()).isEqualTo(DebitMemoAllocationStatus.CONFIRMED);
        assertThat(allocation.getApplyJournalEntryId()).isEqualTo(900L);
        assertThat(debitMemo.getSettlementStatus()).isEqualTo(DebitMemoSettlementStatus.PARTIALLY_SETTLED);
        verify(vendorBillPaymentUpdatePort).updateSettlementStatus(List.of(501L));

        ArgumentCaptor<JournalPostingCommand> captor = ArgumentCaptor.forClass(JournalPostingCommand.class);
        verify(postJournalForEventUseCase).execute(captor.capture());
        JournalPostingCommand command = captor.getValue();
        assertThat(command.eventType()).isEqualTo(SchemaEventType.DEBIT_MEMO_APPLICATION);
        assertThat(command.sourceType()).isEqualTo("DEBIT_MEMO_ALLOCATION");
        assertThat(command.sourceId()).isEqualTo(700L);
        assertThat(command.sourceCode()).isEqualTo("DMA-001");
        assertThat(command.postingDate()).isEqualTo(LocalDate.of(2026, 6, 5));
        assertThat(command.values().get(JournalVariable.DMA_AP_AMT)).isEqualByComparingTo("50.0000");
        assertThat(command.values().get(JournalVariable.DMA_GRIR_CLEARING_AMT)).isEqualByComparingTo("36.0000");
        assertThat(command.values().get(JournalVariable.DMA_TAX_AMT)).isEqualByComparingTo("4.0000");
        assertThat(command.values().get(JournalVariable.DMA_FX_LOSS_AMT)).isEqualByComparingTo("10.0000");
        assertThat(command.values().get(JournalVariable.DMA_FX_GAIN_AMT)).isEqualByComparingTo("0.0000");

        InOrder inOrder = inOrder(sourcePort, postJournalForEventUseCase, journalEntryRepository, repository,
                debitMemoRepository, vendorBillPaymentUpdatePort);
        inOrder.verify(sourcePort).lockDebitMemo(100L);
        inOrder.verify(sourcePort).lockVendorBills(List.of(501L));
        inOrder.verify(sourcePort).findDebitMemoSnapshot(100L);
        inOrder.verify(sourcePort).findVendorBillSnapshot(501L);
        inOrder.verify(postJournalForEventUseCase).execute(any(JournalPostingCommand.class));
        inOrder.verify(journalEntryRepository).findBySource("DEBIT_MEMO_ALLOCATION", 700L);
        inOrder.verify(repository).save(allocation);
        inOrder.verify(debitMemoRepository).updateSettlementStatus(100L, DebitMemoSettlementStatus.PARTIALLY_SETTLED);
        inOrder.verify(vendorBillPaymentUpdatePort).updateSettlementStatus(List.of(501L));
    }

    @Test
    void execute_should_mark_debit_memo_settled_when_confirmed_consumption_reaches_gross_amount() {
        DebitMemoAllocation allocation = draftAllocation();
        DebitMemo debitMemo = debitMemo(DebitMemoSettlementStatus.OPEN);
        JournalEntry applyJournal = journalEntry(900L);
        when(repository.findById(700L)).thenReturn(Optional.of(allocation));
        when(sourcePort.findDebitMemoSnapshot(100L)).thenReturn(Optional.of(debitMemoSnapshot("40.0000", 10L, 1L)));
        when(sourcePort.findVendorBillSnapshot(501L)).thenReturn(Optional.of(vendorBillSnapshot("100.0000", 10L, 1L)));
        when(repository.sumConfirmedAppliedByDebitMemoId(100L)).thenReturn(bd("60.0000"));
        when(journalEntryRepository.findBySource("DEBIT_MEMO_ALLOCATION", 700L))
                .thenReturn(Optional.of(applyJournal));
        when(repository.save(any(DebitMemoAllocation.class))).thenAnswer(inv -> inv.getArgument(0));
        when(debitMemoRepository.findById(100L)).thenReturn(Optional.of(debitMemo));

        useCase.execute(700L);

        assertThat(debitMemo.getSettlementStatus()).isEqualTo(DebitMemoSettlementStatus.SETTLED);
        verify(debitMemoRepository).updateSettlementStatus(100L, DebitMemoSettlementStatus.SETTLED);
    }

    @Test
    void execute_should_reject_closed_period_before_any_external_side_effect() {
        DebitMemoAllocation allocation = draftAllocation();
        when(repository.findById(700L)).thenReturn(Optional.of(allocation));
        doThrow(new DomainException("msg.error.accounting-period.closed"))
                .when(ensureOpenPeriodForDateUseCase).execute(LocalDate.of(2026, 6, 5));

        assertThatThrownBy(() -> useCase.execute(700L))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.accounting-period.closed");

        verifyNoInteractions(sourcePort, postJournalForEventUseCase, journalEntryRepository, debitMemoRepository,
                vendorBillPaymentUpdatePort);
        verify(repository, never()).save(any());
    }

    @Test
    void execute_should_reject_stale_debit_memo_remaining_before_journal_posting() {
        DebitMemoAllocation allocation = draftAllocation();
        when(repository.findById(700L)).thenReturn(Optional.of(allocation));
        when(sourcePort.findDebitMemoSnapshot(100L)).thenReturn(Optional.of(debitMemoSnapshot("39.9999", 10L, 1L)));

        assertThatThrownBy(() -> useCase.execute(700L))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.debit-memo.remaining.changed");

        verifyNoJournalOrSave();
    }

    @Test
    void execute_should_reject_stale_vendor_bill_outstanding_before_journal_posting() {
        DebitMemoAllocation allocation = draftAllocation();
        when(repository.findById(700L)).thenReturn(Optional.of(allocation));
        when(sourcePort.findDebitMemoSnapshot(100L)).thenReturn(Optional.of(debitMemoSnapshot("100.0000", 10L, 1L)));
        when(sourcePort.findVendorBillSnapshot(501L)).thenReturn(Optional.of(vendorBillSnapshot("39.9999", 10L, 1L)));

        assertThatThrownBy(() -> useCase.execute(700L))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.vendor-bill.outstanding.changed");

        verifyNoJournalOrSave();
    }

    @Test
    void execute_should_reject_vendor_mismatch_before_journal_posting() {
        DebitMemoAllocation allocation = draftAllocation();
        when(repository.findById(700L)).thenReturn(Optional.of(allocation));
        when(sourcePort.findDebitMemoSnapshot(100L)).thenReturn(Optional.of(debitMemoSnapshot("100.0000", 10L, 1L)));
        when(sourcePort.findVendorBillSnapshot(501L)).thenReturn(Optional.of(vendorBillSnapshot("100.0000", 99L, 1L)));

        assertThatThrownBy(() -> useCase.execute(700L))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.debit-memo-allocation.vendor-mismatch");

        verifyNoJournalOrSave();
    }

    @Test
    void execute_should_reject_currency_mismatch_before_journal_posting() {
        DebitMemoAllocation allocation = draftAllocation();
        when(repository.findById(700L)).thenReturn(Optional.of(allocation));
        when(sourcePort.findDebitMemoSnapshot(100L)).thenReturn(Optional.of(debitMemoSnapshot("100.0000", 10L, 1L)));
        when(sourcePort.findVendorBillSnapshot(501L)).thenReturn(Optional.of(vendorBillSnapshot("100.0000", 10L, 2L)));

        assertThatThrownBy(() -> useCase.execute(700L))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.debit-memo-allocation.currency-mismatch");

        verifyNoJournalOrSave();
    }

    @Test
    void execute_should_not_post_journal_when_allocation_is_not_draft() {
        DebitMemoAllocation allocation = confirmedAllocation();
        when(repository.findById(700L)).thenReturn(Optional.of(allocation));

        assertThatThrownBy(() -> useCase.execute(700L))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.debit-memo-allocation.confirm.only-draft");

        verifyNoJournalOrSave();
    }

    private void verifyNoJournalOrSave() {
        verifyNoInteractions(postJournalForEventUseCase, journalEntryRepository, debitMemoRepository,
                vendorBillPaymentUpdatePort);
        verify(repository, never()).save(any());
    }

    private static JournalEntry journalEntry(Long id) {
        JournalEntry journalEntry = mock(JournalEntry.class);
        when(journalEntry.getId()).thenReturn(id);
        return journalEntry;
    }

    private static DebitMemoAllocation draftAllocation() {
        return allocation(DebitMemoAllocationStatus.DRAFT);
    }

    private static DebitMemoAllocation confirmedAllocation() {
        return allocation(DebitMemoAllocationStatus.CONFIRMED);
    }

    private static DebitMemoAllocation allocation(DebitMemoAllocationStatus status) {
        return DebitMemoAllocation.reconstitute(
                new AuditMetadata(700L, 1L, null, null, null, null),
                "DMA-001",
                100L,
                "DM-100",
                LocalDate.of(2026, 6, 5),
                status,
                null,
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

    private static DebitMemoAllocationSourcePort.DebitMemoSnapshot debitMemoSnapshot(
            String remainingAmountOriginal,
            Long vendorId,
            Long currencyId) {
        return new DebitMemoAllocationSourcePort.DebitMemoSnapshot(
                100L,
                "DM-100",
                vendorId,
                currencyId,
                bd("100.0000"),
                bd("90.0000"),
                bd("10.0000"),
                bd("90.0000"),
                bd("10.0000"),
                bd(remainingAmountOriginal),
                bd("0.0000"),
                bd("0.0000"),
                bd("0.0000"),
                bd("0.0000"),
                bd("0.0000")
        );
    }

    private static DebitMemoAllocationSourcePort.VendorBillSnapshot vendorBillSnapshot(
            String outstandingAmount,
            Long vendorId,
            Long currencyId) {
        return new DebitMemoAllocationSourcePort.VendorBillSnapshot(
                501L,
                "VB-501",
                vendorId,
                currencyId,
                bd("100.0000"),
                bd("1.250000"),
                bd(outstandingAmount)
        );
    }

    private static BigDecimal bd(String value) {
        return new BigDecimal(value);
    }
}
