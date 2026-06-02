package com.solusi.erp.purchasing.purchasereturn.application.usecase.command;

import com.solusi.erp.accounting.period.application.usecase.query.EnsureOpenPeriodForDateUseCase;
import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.core.infrastructure.sequence.SequenceGeneratorService;
import com.solusi.erp.inventory.goodsissue.application.usecase.command.CompleteGoodsIssueUseCase;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssue;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssuePartyType;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssueReferenceType;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssueStatus;
import com.solusi.erp.inventory.goodsissue.domain.port.GoodsIssueSourceResolver;
import com.solusi.erp.inventory.goodsissue.domain.port.PurchaseReturnGoodsIssueSourcePort;
import com.solusi.erp.inventory.goodsissue.domain.repository.GoodsIssueRepository;
import com.solusi.erp.inventory.goodsissue.infrastructure.service.GoodsIssueSourceResolverRegistry;
import com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturn;
import com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturnStatus;
import com.solusi.erp.purchasing.purchasereturn.domain.repository.PurchaseReturnRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static com.solusi.erp.purchasing.purchasereturn.application.usecase.command.PurchaseReturnDraftTestFixtures.persisted;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfirmPurchaseReturnUseCaseTest {

    @Mock private PurchaseReturnRepository purchaseReturnRepository;
    @Mock private PurchaseReturnGoodsIssueSourcePort sourcePort;
    @Mock private GoodsIssueSourceResolverRegistry resolverRegistry;
    @Mock private GoodsIssueSourceResolver resolver;
    @Mock private SequenceGeneratorService sequenceGeneratorService;
    @Mock private GoodsIssueRepository goodsIssueRepository;
    @Mock private CompleteGoodsIssueUseCase completeGoodsIssueUseCase;
    @Mock private EnsureOpenPeriodForDateUseCase ensureOpenPeriodForDateUseCase;

    @Test
    void execute_approved_createsCompletesAndLinksOneGoodsIssue() {
        PurchaseReturn purchaseReturn = persisted(PurchaseReturnStatus.APPROVED);
        stubHappyPath(purchaseReturn);
        when(purchaseReturnRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        PurchaseReturn result = useCase().execute(1L);

        verify(ensureOpenPeriodForDateUseCase).execute(LocalDate.of(2026, 6, 1));
        verify(sequenceGeneratorService).generate("GOODS_ISSUE");
        verify(completeGoodsIssueUseCase).execute(9L);
        assertThat(result.getStatus()).isEqualTo(PurchaseReturnStatus.CONFIRMED);
        assertThat(result.getGeneratedGoodsIssueId()).isEqualTo(9L);
        verify(purchaseReturnRepository).save(purchaseReturn);
    }

    @Test
    void execute_completedGoodsIssueExists_rejectsIdempotently() {
        PurchaseReturn purchaseReturn = persisted(PurchaseReturnStatus.APPROVED);
        when(purchaseReturnRepository.findById(1L)).thenReturn(Optional.of(purchaseReturn));
        when(sourcePort.hasCompletedGoodsIssue(1L)).thenReturn(true);

        assertThatThrownBy(() -> useCase().execute(1L))
                .isInstanceOf(DomainException.class)
                .satisfies(ex -> assertThat(((DomainException) ex).getKey())
                        .isEqualTo("msg.error.purchase-return.confirm.goods-issue-exists"));

        verify(goodsIssueRepository, never()).save(any());
    }

    @Test
    void execute_closedPeriod_rejectsBeforeCreatingGoodsIssue() {
        PurchaseReturn purchaseReturn = persisted(PurchaseReturnStatus.APPROVED);
        when(purchaseReturnRepository.findById(1L)).thenReturn(Optional.of(purchaseReturn));
        doThrow(new DomainException("msg.error.period.closed"))
                .when(ensureOpenPeriodForDateUseCase).execute(LocalDate.of(2026, 6, 1));

        assertThatThrownBy(() -> useCase().execute(1L))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.period.closed");

        verify(sourcePort, never()).hasCompletedGoodsIssue(any());
        verify(goodsIssueRepository, never()).save(any());
    }

    @Test
    void execute_confirmedReturn_rejectsBeforeSideEffects() {
        PurchaseReturn purchaseReturn = persisted(PurchaseReturnStatus.APPROVED);
        purchaseReturn.confirm(9L);
        when(purchaseReturnRepository.findById(1L)).thenReturn(Optional.of(purchaseReturn));

        assertThatThrownBy(() -> useCase().execute(1L))
                .isInstanceOf(DomainException.class)
                .satisfies(ex -> assertThat(((DomainException) ex).getKey())
                        .isEqualTo("msg.error.purchase-return.confirm.invalid-status"));

        verify(ensureOpenPeriodForDateUseCase, never()).execute(any());
    }

    @Test
    void execute_goodsIssueCompletionFails_doesNotConfirmPurchaseReturn() {
        PurchaseReturn purchaseReturn = persisted(PurchaseReturnStatus.APPROVED);
        stubHappyPath(purchaseReturn);
        doThrow(new DomainException("msg.error.journal.failed"))
                .when(completeGoodsIssueUseCase).execute(9L);

        assertThatThrownBy(() -> useCase().execute(1L))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.journal.failed");

        assertThat(purchaseReturn.getStatus()).isEqualTo(PurchaseReturnStatus.APPROVED);
        verify(purchaseReturnRepository, never()).save(any());
    }

    private void stubHappyPath(PurchaseReturn purchaseReturn) {
        GoodsIssue draft = goodsIssue(null, null);
        GoodsIssue saved = goodsIssue(9L, "GI-202606-00001");
        when(purchaseReturnRepository.findById(1L)).thenReturn(Optional.of(purchaseReturn));
        when(sourcePort.hasCompletedGoodsIssue(1L)).thenReturn(false);
        when(resolverRegistry.getResolver(GoodsIssueReferenceType.PURCHASE_RETURN)).thenReturn(resolver);
        when(resolver.resolve(1L)).thenReturn(draft);
        when(sequenceGeneratorService.generate("GOODS_ISSUE")).thenReturn("GI-202606-00001");
        when(goodsIssueRepository.save(any())).thenReturn(saved);
    }

    private GoodsIssue goodsIssue(Long id, String code) {
        return new GoodsIssue(
                new AuditMetadata(id, 0L, null, null, null, null),
                code,
                LocalDate.of(2026, 6, 1),
                GoodsIssueReferenceType.PURCHASE_RETURN,
                1L,
                "PRT-001",
                3L,
                GoodsIssuePartyType.SUPPLIER,
                30L,
                5L,
                BigDecimal.ONE,
                GoodsIssueStatus.DRAFT,
                null,
                List.of()
        );
    }

    private ConfirmPurchaseReturnUseCaseImpl useCase() {
        return new ConfirmPurchaseReturnUseCaseImpl(
                purchaseReturnRepository, sourcePort, resolverRegistry, sequenceGeneratorService,
                goodsIssueRepository, completeGoodsIssueUseCase, ensureOpenPeriodForDateUseCase);
    }
}
