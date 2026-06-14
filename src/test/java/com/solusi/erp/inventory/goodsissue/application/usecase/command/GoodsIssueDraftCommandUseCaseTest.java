package com.solusi.erp.inventory.goodsissue.application.usecase.command;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.core.infrastructure.sequence.SequenceGeneratorService;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssue;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssueLine;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssuePartyType;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssueReferenceType;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssueStatus;
import com.solusi.erp.inventory.goodsissue.domain.repository.GoodsIssueRepository;
import com.solusi.erp.inventory.goodsissue.infrastructure.service.GoodsIssueSourceResolverRegistry;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GoodsIssueDraftCommandUseCaseTest {

    @Mock
    private GoodsIssueRepository repository;

    @Mock
    private SequenceGeneratorService sequenceGeneratorService;

    @Mock
    private GoodsIssueSourceResolverRegistry resolverRegistry;

    private CreateGoodsIssueUseCase createUseCase;
    private UpdateGoodsIssueUseCase updateUseCase;
    private DeleteGoodsIssueUseCase deleteUseCase;

    @BeforeEach
    void setUp() {
        createUseCase = new CreateGoodsIssueUseCaseImpl(repository, sequenceGeneratorService, resolverRegistry);
        updateUseCase = new UpdateGoodsIssueUseCaseImpl(repository);
        deleteUseCase = new DeleteGoodsIssueUseCaseImpl(repository);
    }

    @Test
    void create_generatesCodeBuildsDraftAndSaves() {
        when(sequenceGeneratorService.generate("GOODS_ISSUE")).thenReturn("GI-202606-00001");
        when(repository.save(any(GoodsIssue.class))).thenAnswer(invocation -> invocation.getArgument(0));

        GoodsIssue result = createUseCase.execute(
                LocalDate.of(2026, 6, 1),
                GoodsIssueReferenceType.MANUAL,
                null,
                "MANUAL",
                null,
                GoodsIssuePartyType.INTERNAL,
                3L,
                1L,
                BigDecimal.ONE,
                "manual issue",
                List.of(lineCommand())
        );

        assertThat(result.getCode()).isEqualTo("GI-202606-00001");
        assertThat(result.getStatus()).isEqualTo(GoodsIssueStatus.DRAFT);
        assertThat(result.getNote()).isEqualTo("manual issue");
        assertThat(result.getLines()).hasSize(1);
        assertThat(result.getLines().getFirst().getQuantityIssued()).isEqualByComparingTo("2.0000");
        verify(repository).save(result);
    }

    @Test
    void update_preservesHeaderReferenceAndReplacesLines() {
        GoodsIssue issue = draftIssue();
        when(repository.findById(7L)).thenReturn(Optional.of(issue));
        when(repository.save(any(GoodsIssue.class))).thenAnswer(invocation -> invocation.getArgument(0));

        GoodsIssue result = updateUseCase.execute(7L, LocalDate.of(2026, 6, 2), "updated", List.of(lineCommand()));

        assertThat(result.getReferenceType()).isEqualTo(GoodsIssueReferenceType.PURCHASE_RETURN);
        assertThat(result.getReferenceId()).isEqualTo(70L);
        assertThat(result.getIssueDate()).isEqualTo(LocalDate.of(2026, 6, 2));
        assertThat(result.getNote()).isEqualTo("updated");
        assertThat(result.getLines()).hasSize(1);
    }

    @Test
    void delete_rejectsCompletedIssue() {
        GoodsIssue issue = draftIssue();
        issue.complete();
        when(repository.findById(7L)).thenReturn(Optional.of(issue));

        assertThatThrownBy(() -> deleteUseCase.execute(7L))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.gi.completed.immutable");
    }

    @Test
    void delete_allowsDraftIssue() {
        GoodsIssue issue = draftIssue();
        when(repository.findById(7L)).thenReturn(Optional.of(issue));

        deleteUseCase.execute(7L);

        ArgumentCaptor<GoodsIssue> captor = ArgumentCaptor.forClass(GoodsIssue.class);
        verify(repository).delete(captor.capture());
        assertThat(captor.getValue()).isSameAs(issue);
    }

    private static GoodsIssue draftIssue() {
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
                List.of(activeLine())
        );
    }

    private static GoodsIssueLine activeLine() {
        return GoodsIssueLine.prefill(
                101L, 201L, false,
                new BigDecimal("2.0000"), 1L, new BigDecimal("2.0000"),
                3L, 4L, 5L, null,
                new BigDecimal("150.000000"), new BigDecimal("300.0000"),
                BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("300.0000"),
                "GOODS_RECEIPT", 301L, 401L
        );
    }

    private static GoodsIssueLineCommand lineCommand() {
        return new GoodsIssueLineCommand(
                null,
                101L,
                201L,
                false,
                new BigDecimal("2.0000"),
                1L,
                new BigDecimal("2.0000"),
                3L,
                4L,
                5L,
                null,
                new BigDecimal("150.000000"),
                "GOODS_RECEIPT",
                301L,
                401L
        );
    }
}
