package com.solusi.erp.inventory.goodsissue.application.usecase.command;

import com.solusi.erp.accounting.journal.application.usecase.command.ReversePostedJournalCommand;
import com.solusi.erp.accounting.journal.application.usecase.command.ReversePostedJournalUseCase;
import com.solusi.erp.accounting.journal.domain.model.JournalEntry;
import com.solusi.erp.accounting.journal.domain.model.JournalLine;
import com.solusi.erp.accounting.journal.domain.model.JournalStatus;
import com.solusi.erp.accounting.journal.domain.repository.JournalEntryRepository;
import com.solusi.erp.accounting.period.application.usecase.query.EnsureOpenPeriodForDateUseCase;
import com.solusi.erp.accounting.schema.domain.model.SchemaEventType;
import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssue;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssueStatus;
import com.solusi.erp.inventory.goodsissue.domain.repository.GoodsIssueRepository;
import com.solusi.erp.inventory.stock.domain.model.MovementType;
import com.solusi.erp.inventory.stock.domain.model.ReferenceType;
import com.solusi.erp.inventory.stock.domain.model.StockMovementReversalRequest;
import com.solusi.erp.inventory.stock.domain.port.StockMovementReversalService;
import com.solusi.erp.inventory.stock.infrastructure.persistence.InventoryMovementEntity;
import com.solusi.erp.inventory.stock.infrastructure.persistence.InventoryMovementJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CancelGoodsIssueUseCaseTest {

    @Mock
    private GoodsIssueRepository repository;

    @Mock
    private EnsureOpenPeriodForDateUseCase ensureOpenPeriodForDateUseCase;

    @Mock
    private StockMovementReversalService stockMovementReversalService;

    @Mock
    private InventoryMovementJpaRepository movementRepository;

    @Mock
    private JournalEntryRepository journalEntryRepository;

    @Mock
    private ReversePostedJournalUseCase reversePostedJournalUseCase;

    @Mock
    private GoodsIssueInUseChecker inUseChecker;

    private CancelGoodsIssueUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CancelGoodsIssueUseCaseImpl(
                repository,
                ensureOpenPeriodForDateUseCase,
                stockMovementReversalService,
                movementRepository,
                journalEntryRepository,
                reversePostedJournalUseCase,
                inUseChecker);
    }

    @Test
    void cancel_completedManualIssueReversesStockAndJournalThenSavesCancelled() {
        GoodsIssue issue = completedIssue();
        InventoryMovementEntity movement = movement(700L, 5L);
        JournalEntry journal = postedGoodsIssueJournal(900L);
        when(repository.findById(7L)).thenReturn(Optional.of(issue));
        when(movementRepository.findByReferenceTypeAndReferenceIdOrderByIdAsc(ReferenceType.GOODS_ISSUE, 7L))
                .thenReturn(List.of(movement));
        when(journalEntryRepository.findBySource("GOODS_ISSUE", 7L)).thenReturn(Optional.of(journal));

        useCase.execute(command(List.of(new GoodsIssueCancelLineCommand(
                null, 700L, 8L, "Product", null, new BigDecimal("2.0000")))));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<StockMovementReversalRequest>> stockCaptor = ArgumentCaptor.forClass(List.class);
        verify(stockMovementReversalService).reverse(stockCaptor.capture());
        assertThat(stockCaptor.getValue()).singleElement().satisfies(request -> {
            assertThat(request.originalMovementId()).isEqualTo(700L);
            assertThat(request.targetContainerId()).isEqualTo(8L);
            assertThat(request.reversalDate()).isEqualTo(LocalDate.of(2026, 6, 4));
            assertThat(request.reason()).isEqualTo("wrong return");
        });

        ArgumentCaptor<ReversePostedJournalCommand> journalCaptor =
                ArgumentCaptor.forClass(ReversePostedJournalCommand.class);
        verify(reversePostedJournalUseCase).execute(journalCaptor.capture());
        assertThat(journalCaptor.getValue().originalJournalEntryId()).isEqualTo(900L);
        assertThat(journalCaptor.getValue().reversalDate()).isEqualTo(LocalDate.of(2026, 6, 4));
        assertThat(journalCaptor.getValue().description()).isEqualTo("wrong return");

        ArgumentCaptor<GoodsIssue> issueCaptor = ArgumentCaptor.forClass(GoodsIssue.class);
        verify(repository).save(issueCaptor.capture());
        assertThat(issueCaptor.getValue().getStatus()).isEqualTo(GoodsIssueStatus.CANCELLED);
        assertThat(issueCaptor.getValue().getCancelledDate()).isEqualTo(LocalDate.of(2026, 6, 4));
        assertThat(issueCaptor.getValue().getCancelReason()).isEqualTo("wrong return");
    }

    @Test
    void cancel_requiresReversalDate() {
        assertThatThrownBy(() -> useCase.execute(new GoodsIssueCancelCommand(7L, null, "wrong return", null)))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.gi.cancel.reversal.date.required");

        verify(repository, never()).findById(any());
    }

    @Test
    void cancel_rejectsDraftIssue() {
        GoodsIssue issue = CompleteGoodsIssueUseCaseTest.draftIssue(
                List.of(CompleteGoodsIssueUseCaseTest.line(false, "2.0000", null)));
        when(repository.findById(7L)).thenReturn(Optional.of(issue));

        assertThatThrownBy(() -> useCase.execute(command(List.of())))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.gi.cancel.only.completed");

        verify(repository, never()).save(any());
    }

    @Test
    void cancel_rejectsSourceOwnedIssue() {
        GoodsIssue issue = CompleteGoodsIssueUseCaseTest.purchaseReturnIssue(
                List.of(CompleteGoodsIssueUseCaseTest.line(false, "2.0000", null)));
        issue.complete();
        when(repository.findById(7L)).thenReturn(Optional.of(issue));

        assertThatThrownBy(() -> useCase.execute(command(List.of())))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.gi.cancel.source.owned");

        verify(stockMovementReversalService, never()).reverse(any());
        verify(repository, never()).save(any());
    }

    @Test
    void cancel_whenPeriodClosed_doesNotReverseStock() {
        GoodsIssue issue = completedIssue();
        when(repository.findById(7L)).thenReturn(Optional.of(issue));
        doThrow(new DomainException("msg.error.period.not.open"))
                .when(ensureOpenPeriodForDateUseCase).execute(LocalDate.of(2026, 6, 4));

        assertThatThrownBy(() -> useCase.execute(command(List.of())))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.period.not.open");

        verify(stockMovementReversalService, never()).reverse(any());
        verify(repository, never()).save(any());
    }

    @Test
    void cancel_rejectsIssueInUse() {
        GoodsIssue issue = completedIssue();
        when(repository.findById(7L)).thenReturn(Optional.of(issue));
        doThrow(new DomainException("msg.error.gi.in.use"))
                .when(inUseChecker).assertNotInUse(issue);

        assertThatThrownBy(() -> useCase.execute(command(List.of())))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.gi.in.use");

        verify(stockMovementReversalService, never()).reverse(any());
        verify(repository, never()).save(any());
    }

    @Test
    void cancel_propagatesAlreadyReversedMovement() {
        GoodsIssue issue = completedIssue();
        InventoryMovementEntity movement = movement(700L, 5L);
        when(repository.findById(7L)).thenReturn(Optional.of(issue));
        when(movementRepository.findByReferenceTypeAndReferenceIdOrderByIdAsc(ReferenceType.GOODS_ISSUE, 7L))
                .thenReturn(List.of(movement));
        doThrow(new DomainException("msg.error.stock.reversal.already.reversed"))
                .when(stockMovementReversalService).reverse(any());

        assertThatThrownBy(() -> useCase.execute(command(List.of())))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.stock.reversal.already.reversed");

        verify(reversePostedJournalUseCase, never()).execute(any());
        verify(repository, never()).save(any());
    }

    @Test
    void cancel_whenJournalReversalFails_doesNotSaveCancelledIssue() {
        GoodsIssue issue = completedIssue();
        InventoryMovementEntity movement = movement(700L, 5L);
        JournalEntry journal = postedGoodsIssueJournal(900L);
        when(repository.findById(7L)).thenReturn(Optional.of(issue));
        when(movementRepository.findByReferenceTypeAndReferenceIdOrderByIdAsc(ReferenceType.GOODS_ISSUE, 7L))
                .thenReturn(List.of(movement));
        when(journalEntryRepository.findBySource("GOODS_ISSUE", 7L)).thenReturn(Optional.of(journal));
        doThrow(new DomainException("msg.error.journal.failed"))
                .when(reversePostedJournalUseCase).execute(any());

        assertThatThrownBy(() -> useCase.execute(command(List.of())))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.journal.failed");

        verify(repository, never()).save(any());
        assertThat(issue.getStatus()).isEqualTo(GoodsIssueStatus.COMPLETED);
    }

    private GoodsIssueCancelCommand command(List<GoodsIssueCancelLineCommand> lines) {
        return new GoodsIssueCancelCommand(7L, LocalDate.of(2026, 6, 4), "wrong return", lines);
    }

    private static GoodsIssue completedIssue() {
        GoodsIssue issue = CompleteGoodsIssueUseCaseTest.draftIssue(
                List.of(CompleteGoodsIssueUseCaseTest.line(false, "2.0000", null)));
        issue.complete();
        return issue;
    }

    private static InventoryMovementEntity movement(Long id, Long containerId) {
        InventoryMovementEntity entity = new InventoryMovementEntity();
        entity.setId(id);
        entity.setProductId(201L);
        entity.setContainerId(containerId);
        entity.setQuantity(new BigDecimal("2.0000"));
        entity.setMovementType(MovementType.ISSUE);
        entity.setReferenceType(ReferenceType.GOODS_ISSUE);
        entity.setReferenceId(7L);
        entity.setReferenceCode("GI-202606-00001");
        return entity;
    }

    private static JournalEntry postedGoodsIssueJournal(Long id) {
        return new JournalEntry(
                new AuditMetadata(id, 1L, null, null, null, null),
                SchemaEventType.GOODS_ISSUE,
                "GOODS_ISSUE",
                7L,
                "GI-202606-00001",
                LocalDate.of(2026, 6, 1),
                "Auto journal for goods issue GI-202606-00001",
                JournalStatus.POSTED,
                List.of(
                        JournalLine.debit(10L, new BigDecimal("300.0000")),
                        JournalLine.credit(11L, new BigDecimal("300.0000"))
                )
        );
    }
}
