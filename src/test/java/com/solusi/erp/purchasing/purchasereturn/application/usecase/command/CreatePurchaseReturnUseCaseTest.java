package com.solusi.erp.purchasing.purchasereturn.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.core.infrastructure.sequence.SequenceGeneratorService;
import com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturn;
import com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturnReason;
import com.solusi.erp.purchasing.purchasereturn.domain.port.PurchaseReturnSourceQueryPort;
import com.solusi.erp.purchasing.purchasereturn.domain.repository.PurchaseReturnRepository;
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

import static com.solusi.erp.purchasing.purchasereturn.application.usecase.command.PurchaseReturnDraftTestFixtures.command;
import static com.solusi.erp.purchasing.purchasereturn.application.usecase.command.PurchaseReturnDraftTestFixtures.serial;
import static com.solusi.erp.purchasing.purchasereturn.application.usecase.command.PurchaseReturnDraftTestFixtures.slice;
import static com.solusi.erp.purchasing.purchasereturn.application.usecase.command.PurchaseReturnDraftTestFixtures.source;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreatePurchaseReturnUseCaseTest {

    @Mock private PurchaseReturnRepository repository;
    @Mock private SequenceGeneratorService sequenceGeneratorService;
    @Mock private PurchaseReturnSourceQueryPort queryPort;

    private CreatePurchaseReturnUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreatePurchaseReturnUseCaseImpl(repository, sequenceGeneratorService, queryPort);
    }

    @Test
    void execute_validDraft_rebuildsSnapshotsAndSaves() {
        stubNonSerialSource();
        when(sequenceGeneratorService.generate("PURCHASE_RETURN")).thenReturn("PRT-001");
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        PurchaseReturn result = useCase.execute(
                1L, LocalDate.of(2026, 6, 2), PurchaseReturnReason.DAMAGED, null,
                List.of(command(BigDecimal.valueOf(5))));

        assertThat(result.getCode()).isEqualTo("PRT-001");
        assertThat(result.getLines()).singleElement().satisfies(line -> {
            assertThat(line.getContainerId()).isEqualTo(40L);
            assertThat(line.getInventoryAmount()).isEqualByComparingTo("500");
            assertThat(line.getTaxReversalAmount()).isEqualByComparingTo("50");
        });
    }

    @Test
    void execute_staleSource_rejects() {
        when(queryPort.findEligibleGoodsReceiptById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> create(List.of(command(BigDecimal.ONE))))
                .isInstanceOf(DomainException.class)
                .hasMessage("msg.error.purchase-return.source.ineligible");

        verify(repository, never()).save(any());
    }

    @Test
    void execute_lineFromDifferentGr_rejects() {
        when(queryPort.findEligibleGoodsReceiptById(1L)).thenReturn(Optional.of(source()));
        when(queryPort.findReturnableGrLineSlices(1L, null, List.of())).thenReturn(List.of(slice()));

        assertThatThrownBy(() -> create(List.of(new PurchaseReturnLineCommand(
                999L, false, BigDecimal.ONE, BigDecimal.ONE, 40L, null,
                PurchaseReturnReason.DAMAGED, null))))
                .isInstanceOf(DomainException.class)
                .hasMessage("msg.error.purchase-return.line.stale");
    }

    @Test
    void execute_quantityAboveSliceAvailable_rejects() {
        stubNonSerialSource();

        assertThatThrownBy(() -> create(List.of(command(BigDecimal.valueOf(11)))))
                .isInstanceOf(DomainException.class)
                .hasMessage("msg.error.purchase-return.line.quantity-exceeds-returnable");
    }

    @Test
    void execute_serialSelectionNoLongerAvailable_rejects() {
        when(queryPort.findEligibleGoodsReceiptById(1L)).thenReturn(Optional.of(source()));
        when(queryPort.findReturnableSerials(1L, 12L, null, List.of())).thenReturn(List.of(serial()));
        PurchaseReturnLineCommand command = new PurchaseReturnLineCommand(
                12L, true, BigDecimal.ONE, BigDecimal.ONE, 41L, "SER-STALE",
                PurchaseReturnReason.DAMAGED, null);

        assertThatThrownBy(() -> create(List.of(command)))
                .isInstanceOf(DomainException.class)
                .hasMessage("msg.error.purchase-return.line.serial-stale");
    }

    @Test
    void execute_zeroRowsFiltered_rejectsEmptyDraft() {
        when(queryPort.findEligibleGoodsReceiptById(1L)).thenReturn(Optional.of(source()));

        assertThatThrownBy(() -> create(List.of(command(BigDecimal.ZERO))))
                .isInstanceOf(DomainException.class)
                .hasMessage("msg.error.purchase-return.lines-required");
    }

    private void create(List<PurchaseReturnLineCommand> commands) {
        useCase.execute(1L, LocalDate.of(2026, 6, 2), PurchaseReturnReason.DAMAGED, null, commands);
    }

    private void stubNonSerialSource() {
        when(queryPort.findEligibleGoodsReceiptById(1L)).thenReturn(Optional.of(source()));
        when(queryPort.findReturnableGrLineSlices(1L, null, List.of())).thenReturn(List.of(slice()));
    }
}
