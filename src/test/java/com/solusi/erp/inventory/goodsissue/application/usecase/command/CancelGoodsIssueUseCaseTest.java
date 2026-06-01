package com.solusi.erp.inventory.goodsissue.application.usecase.command;

import com.solusi.erp.accounting.journal.application.usecase.command.JournalPostingCommand;
import com.solusi.erp.accounting.journal.application.usecase.command.PostJournalForEventUseCase;
import com.solusi.erp.accounting.journal.domain.model.JournalVariable;
import com.solusi.erp.accounting.period.application.usecase.query.EnsureOpenPeriodForDateUseCase;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssue;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssueStatus;
import com.solusi.erp.inventory.goodsissue.domain.repository.GoodsIssueRepository;
import com.solusi.erp.inventory.stock.application.dto.StockMovementPayload;
import com.solusi.erp.inventory.stock.domain.model.MovementType;
import com.solusi.erp.inventory.stock.domain.port.StockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
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
    private StockService stockService;

    @Mock
    private PostJournalForEventUseCase postJournalForEventUseCase;

    @Mock
    private GoodsIssueInUseChecker inUseChecker;

    private CancelGoodsIssueUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CancelGoodsIssueUseCaseImpl(
                repository, ensureOpenPeriodForDateUseCase, stockService, postJournalForEventUseCase, inUseChecker);
    }

    @Test
    void cancel_completedIssuePostsReversalAndSavesCancelled() {
        GoodsIssue issue = completedIssue();
        when(repository.findById(7L)).thenReturn(Optional.of(issue));

        useCase.execute(7L, "wrong return");

        ArgumentCaptor<StockMovementPayload> stockCaptor = ArgumentCaptor.forClass(StockMovementPayload.class);
        verify(stockService).adjust(stockCaptor.capture());
        assertThat(stockCaptor.getValue().getMovementType()).isEqualTo(MovementType.RECEIPT);
        assertThat(stockCaptor.getValue().getQuantity()).isEqualByComparingTo("2.0000");

        ArgumentCaptor<JournalPostingCommand> journalCaptor = ArgumentCaptor.forClass(JournalPostingCommand.class);
        verify(postJournalForEventUseCase).execute(journalCaptor.capture());
        assertThat(journalCaptor.getValue().values().get(JournalVariable.GI_COGS_AMT))
                .isEqualByComparingTo("-300.0000");
        assertThat(journalCaptor.getValue().values().get(JournalVariable.GI_INVENTORY_AMT))
                .isEqualByComparingTo("-300.0000");

        ArgumentCaptor<GoodsIssue> issueCaptor = ArgumentCaptor.forClass(GoodsIssue.class);
        verify(repository).save(issueCaptor.capture());
        assertThat(issueCaptor.getValue().getStatus()).isEqualTo(GoodsIssueStatus.CANCELLED);
    }

    @Test
    void cancel_rejectsDraftIssue() {
        GoodsIssue issue = CompleteGoodsIssueUseCaseTest.draftIssue(List.of(CompleteGoodsIssueUseCaseTest.line(false, "2.0000", null)));
        when(repository.findById(7L)).thenReturn(Optional.of(issue));

        assertThatThrownBy(() -> useCase.execute(7L, "wrong return"))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.gi.cancel.only.completed");

        verify(repository, never()).save(any());
    }

    @Test
    void cancel_rejectsIssueInUse() {
        GoodsIssue issue = completedIssue();
        when(repository.findById(7L)).thenReturn(Optional.of(issue));
        doThrow(new DomainException("msg.error.gi.in.use"))
                .when(inUseChecker).assertNotInUse(issue);

        assertThatThrownBy(() -> useCase.execute(7L, "wrong return"))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.gi.in.use");

        verify(repository, never()).save(any());
    }

    private static GoodsIssue completedIssue() {
        GoodsIssue issue = CompleteGoodsIssueUseCaseTest.draftIssue(
                List.of(CompleteGoodsIssueUseCaseTest.line(false, "2.0000", null)));
        issue.complete();
        return issue;
    }
}
