package com.solusi.erp.inventory.goodsissue.application.usecase.command;

import com.solusi.erp.accounting.journal.application.usecase.command.JournalPostingCommand;
import com.solusi.erp.accounting.journal.application.usecase.command.PostJournalForEventUseCase;
import com.solusi.erp.accounting.journal.domain.model.JournalVariable;
import com.solusi.erp.accounting.period.application.usecase.query.EnsureOpenPeriodForDateUseCase;
import com.solusi.erp.accounting.schema.domain.model.SchemaEventType;
import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssue;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssueLine;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssuePartyType;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssueReferenceType;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssueStatus;
import com.solusi.erp.inventory.goodsissue.domain.repository.GoodsIssueRepository;
import com.solusi.erp.inventory.stock.application.dto.StockMovementPayload;
import com.solusi.erp.inventory.stock.domain.model.MovementType;
import com.solusi.erp.inventory.stock.domain.model.ReferenceType;
import com.solusi.erp.inventory.stock.domain.port.StockService;
import com.solusi.erp.inventory.uomconversion.domain.port.UomConversionService;
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
class CompleteGoodsIssueUseCaseTest {

    @Mock
    private GoodsIssueRepository repository;

    @Mock
    private EnsureOpenPeriodForDateUseCase ensureOpenPeriodForDateUseCase;

    @Mock
    private StockService stockService;

    @Mock
    private UomConversionService uomConversionService;

    @Mock
    private PostJournalForEventUseCase postJournalForEventUseCase;

    private CompleteGoodsIssueUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CompleteGoodsIssueUseCaseImpl(
                repository, ensureOpenPeriodForDateUseCase, stockService, uomConversionService,
                postJournalForEventUseCase);
    }

    @Test
    void complete_postsIssueStockPayloadWithSpecificValuationReferenceAndJournal() {
        GoodsIssue issue = draftIssue(List.of(line(false, "2.0000", null)));
        when(repository.findById(7L)).thenReturn(Optional.of(issue));
        when(uomConversionService.convertToBaseUom(201L, 1L, new BigDecimal("2.0000")))
                .thenReturn(new BigDecimal("2.0000"));

        useCase.execute(7L);

        ArgumentCaptor<StockMovementPayload> stockCaptor = ArgumentCaptor.forClass(StockMovementPayload.class);
        verify(stockService).adjust(stockCaptor.capture());
        StockMovementPayload payload = stockCaptor.getValue();
        assertThat(payload.getMovementType()).isEqualTo(MovementType.ISSUE);
        assertThat(payload.getReferenceType()).isEqualTo(ReferenceType.GOODS_ISSUE);
        assertThat(payload.getReferenceId()).isEqualTo(7L);
        assertThat(payload.getValuationReferenceType()).isEqualTo(ReferenceType.GOODS_RECEIPT);
        assertThat(payload.getValuationReferenceId()).isEqualTo(301L);
        assertThat(payload.getValuationReferenceLineId()).isEqualTo(401L);
        assertThat(payload.getNetPrice()).isEqualByComparingTo("150.000000");

        ArgumentCaptor<JournalPostingCommand> journalCaptor = ArgumentCaptor.forClass(JournalPostingCommand.class);
        verify(postJournalForEventUseCase).execute(journalCaptor.capture());
        assertThat(journalCaptor.getValue().eventType()).isEqualTo(SchemaEventType.GOODS_ISSUE);
        assertThat(journalCaptor.getValue().sourceType()).isEqualTo("GOODS_ISSUE");
        assertThat(journalCaptor.getValue().sourceId()).isEqualTo(7L);
        assertThat(journalCaptor.getValue().sourceCode()).isEqualTo("GI-202606-00001");
        assertThat(journalCaptor.getValue().originalCurrencyId()).isNull();
        assertThat(journalCaptor.getValue().originalValues()).isNull();
        assertThat(journalCaptor.getValue().accountOverrides()).isNull();
        assertThat(journalCaptor.getValue().values().get(JournalVariable.GI_COGS_AMT))
                .isEqualByComparingTo("300.0000");
        assertThat(journalCaptor.getValue().values().get(JournalVariable.GI_INVENTORY_AMT))
                .isEqualByComparingTo("300.0000");

        ArgumentCaptor<GoodsIssue> issueCaptor = ArgumentCaptor.forClass(GoodsIssue.class);
        verify(repository).save(issueCaptor.capture());
        assertThat(issueCaptor.getValue().getStatus()).isEqualTo(GoodsIssueStatus.COMPLETED);
    }

    @Test
    void complete_whenPeriodClosed_doesNotSave() {
        GoodsIssue issue = draftIssue(List.of(line(false, "2.0000", null)));
        when(repository.findById(7L)).thenReturn(Optional.of(issue));
        doThrow(new DomainException("msg.error.period.closed"))
                .when(ensureOpenPeriodForDateUseCase).execute(LocalDate.of(2026, 6, 1));

        assertThatThrownBy(() -> useCase.execute(7L))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.period.closed");

        verify(repository, never()).save(any());
        assertThat(issue.getStatus()).isEqualTo(GoodsIssueStatus.DRAFT);
    }

    @Test
    void complete_whenStockFails_doesNotSaveCompletedStatus() {
        GoodsIssue issue = draftIssue(List.of(line(false, "2.0000", null)));
        when(repository.findById(7L)).thenReturn(Optional.of(issue));
        when(uomConversionService.convertToBaseUom(201L, 1L, new BigDecimal("2.0000")))
                .thenReturn(new BigDecimal("2.0000"));
        doThrow(new DomainException("msg.error.stock.insufficient"))
                .when(stockService).adjust(any(StockMovementPayload.class));

        assertThatThrownBy(() -> useCase.execute(7L))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.stock.insufficient");

        verify(repository, never()).save(any());
    }

    @Test
    void complete_serializedCsvPostsOneMovementPerSerial() {
        GoodsIssue issue = draftIssue(List.of(line(true, "2.0000", "SN-001,SN-002")));
        when(repository.findById(7L)).thenReturn(Optional.of(issue));
        when(uomConversionService.convertToBaseUom(201L, 1L, new BigDecimal("2.0000")))
                .thenReturn(new BigDecimal("2.0000"));

        useCase.execute(7L);

        ArgumentCaptor<StockMovementPayload> stockCaptor = ArgumentCaptor.forClass(StockMovementPayload.class);
        verify(stockService, org.mockito.Mockito.times(2)).adjust(stockCaptor.capture());
        assertThat(stockCaptor.getAllValues()).extracting(StockMovementPayload::getSerialNumber)
                .containsExactly("SN-001", "SN-002");
        assertThat(stockCaptor.getAllValues()).allMatch(payload -> payload.getQuantity().compareTo(BigDecimal.ONE) == 0);
    }

    @Test
    void complete_serializedRequiresWholeBaseQuantity() {
        GoodsIssue issue = draftIssue(List.of(line(true, "1.5000", "SN-001")));
        when(repository.findById(7L)).thenReturn(Optional.of(issue));
        when(uomConversionService.convertToBaseUom(201L, 1L, new BigDecimal("1.5000")))
                .thenReturn(new BigDecimal("1.5000"));

        assertThatThrownBy(() -> useCase.execute(7L))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.gi.serial.quantity.whole");

        verify(repository, never()).save(any());
    }

    static GoodsIssue draftIssue(List<GoodsIssueLine> lines) {
        return new GoodsIssue(
                new AuditMetadata(7L, 1L, null, null, null, null),
                "GI-202606-00001",
                LocalDate.of(2026, 6, 1),
                GoodsIssueReferenceType.PURCHASE_RETURN,
                70L,
                "PRTN-0070",
                11L,
                GoodsIssuePartyType.SUPPLIER,
                3L,
                1L,
                BigDecimal.ONE,
                GoodsIssueStatus.DRAFT,
                null,
                lines
        );
    }

    static GoodsIssueLine line(boolean serialized, String quantity, String serialNumber) {
        BigDecimal qty = new BigDecimal(quantity);
        BigDecimal unitCost = new BigDecimal("150.000000");
        BigDecimal amount = qty.multiply(unitCost).setScale(4);
        return GoodsIssueLine.prefill(
                101L, 201L, serialized,
                qty, 1L, qty,
                3L, 4L, 5L, serialNumber,
                unitCost, amount,
                BigDecimal.ZERO, BigDecimal.ZERO, amount,
                "GOODS_RECEIPT", 301L, 401L
        );
    }
}
