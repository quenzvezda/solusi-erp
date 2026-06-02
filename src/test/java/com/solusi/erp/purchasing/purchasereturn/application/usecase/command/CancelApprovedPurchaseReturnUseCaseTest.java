package com.solusi.erp.purchasing.purchasereturn.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.inventory.stock.domain.model.ReservationOwnerType;
import com.solusi.erp.inventory.stock.domain.port.InventoryReservationService;
import com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturn;
import com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturnStatus;
import com.solusi.erp.purchasing.purchasereturn.domain.repository.PurchaseReturnRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.solusi.erp.purchasing.purchasereturn.application.usecase.command.CreatePurchaseReturnUseCaseTest.persisted;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CancelApprovedPurchaseReturnUseCaseTest {

    @Mock private PurchaseReturnRepository repository;
    @Mock private InventoryReservationService reservationService;

    @Test
    void execute_approved_releasesReservationAndCancels() {
        PurchaseReturn purchaseReturn = persisted(PurchaseReturnStatus.APPROVED);
        when(repository.findById(1L)).thenReturn(Optional.of(purchaseReturn));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        PurchaseReturn result = new CancelApprovedPurchaseReturnUseCaseImpl(repository, reservationService)
                .execute(1L);

        assertThat(result.getStatus()).isEqualTo(PurchaseReturnStatus.CANCELLED);
        verify(reservationService).release(ReservationOwnerType.PURCHASE_RETURN, 1L);
    }

    @Test
    void execute_confirmed_rejectsBeforeRelease() {
        PurchaseReturn purchaseReturn = persisted(PurchaseReturnStatus.APPROVED);
        purchaseReturn.confirm(500L);
        when(repository.findById(1L)).thenReturn(Optional.of(purchaseReturn));

        assertThatThrownBy(() -> new CancelApprovedPurchaseReturnUseCaseImpl(repository, reservationService)
                .execute(1L))
                .isInstanceOf(DomainException.class)
                .satisfies(ex -> assertThat(((DomainException) ex).getKey())
                        .isEqualTo("msg.error.purchase-return.cancel-approved.invalid-status"));

        verify(reservationService, never()).release(any(), any());
    }
}
