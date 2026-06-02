package com.solusi.erp.purchasing.purchasereturn.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.inventory.stock.domain.model.InventoryReservationRequest;
import com.solusi.erp.inventory.stock.domain.model.ReservationOwnerType;
import com.solusi.erp.inventory.stock.domain.port.InventoryReservationService;
import com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturn;
import com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturnStatus;
import com.solusi.erp.purchasing.purchasereturn.domain.port.PurchaseReturnEventPublisher;
import com.solusi.erp.purchasing.purchasereturn.domain.repository.PurchaseReturnRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static com.solusi.erp.purchasing.purchasereturn.application.usecase.command.PurchaseReturnDraftTestFixtures.persisted;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubmitPurchaseReturnUseCaseTest {

    @Mock private PurchaseReturnRepository repository;
    @Mock private InventoryReservationService reservationService;
    @Mock private PurchaseReturnEventPublisher eventPublisher;

    @Test
    void execute_draft_reservesSavesAndPublishesApproval() {
        PurchaseReturn purchaseReturn = persisted(PurchaseReturnStatus.DRAFT);
        when(repository.findById(1L)).thenReturn(Optional.of(purchaseReturn));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        PurchaseReturn result = useCase().execute(1L, 99L, 77L, 55L);

        assertThat(result.getStatus()).isEqualTo(PurchaseReturnStatus.SUBMITTED);
        ArgumentCaptor<List<InventoryReservationRequest>> requests = ArgumentCaptor.forClass(List.class);
        verify(reservationService).reserve(
                org.mockito.ArgumentMatchers.eq(ReservationOwnerType.PURCHASE_RETURN),
                org.mockito.ArgumentMatchers.eq(1L),
                org.mockito.ArgumentMatchers.eq("PRT-001"),
                requests.capture());
        assertThat(requests.getValue()).hasSize(1);
        assertThat(requests.getValue().get(0).containerId()).isEqualTo(40L);
        verify(eventPublisher).publishApprovalRequested(1L, "PRT-001", 77L, 55L);
    }

    @Test
    void execute_reserveFails_doesNotSaveOrPublish() {
        PurchaseReturn purchaseReturn = persisted(PurchaseReturnStatus.DRAFT);
        when(repository.findById(1L)).thenReturn(Optional.of(purchaseReturn));
        doThrow(new DomainException("msg.error.inventory.insufficient_available"))
                .when(reservationService)
                .reserve(any(), any(), any(), anyList());

        assertThatThrownBy(() -> useCase().execute(1L, 99L, 77L, 55L))
                .isInstanceOf(DomainException.class);

        assertThat(purchaseReturn.getStatus()).isEqualTo(PurchaseReturnStatus.DRAFT);
        verify(repository, never()).save(any());
        verify(eventPublisher, never()).publishApprovalRequested(any(), any(), any(), any());
    }

    @Test
    void execute_duplicateSubmit_doesNotReserve() {
        PurchaseReturn purchaseReturn = persisted(PurchaseReturnStatus.SUBMITTED);
        when(repository.findById(1L)).thenReturn(Optional.of(purchaseReturn));

        assertThatThrownBy(() -> useCase().execute(1L, 99L, 77L, 55L))
                .isInstanceOf(DomainException.class)
                .satisfies(ex -> assertThat(((DomainException) ex).getKey())
                        .isEqualTo("msg.error.purchase-return.submit.invalid-status"));

        verify(reservationService, never()).reserve(any(), any(), any(), anyList());
    }

    @Test
    void execute_missingApprover_doesNotLoadOrReserve() {
        assertThatThrownBy(() -> useCase().execute(1L, 99L, 77L, null))
                .isInstanceOf(DomainException.class)
                .satisfies(ex -> assertThat(((DomainException) ex).getKey())
                        .isEqualTo("msg.error.purchase-return.submit.approver-required"));

        verify(repository, never()).findById(any());
        verify(reservationService, never()).reserve(any(), any(), any(), anyList());
    }

    private SubmitPurchaseReturnUseCaseImpl useCase() {
        return new SubmitPurchaseReturnUseCaseImpl(repository, reservationService, eventPublisher);
    }
}
