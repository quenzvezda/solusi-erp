package com.solusi.erp.purchasing.purchasereturn.application.usecase.command;

import com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturn;
import com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturnStatus;
import com.solusi.erp.purchasing.purchasereturn.domain.repository.PurchaseReturnRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.solusi.erp.purchasing.purchasereturn.application.usecase.command.CreatePurchaseReturnUseCaseTest.draft;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CancelDraftPurchaseReturnUseCaseTest {

    @Mock private PurchaseReturnRepository repository;

    @Test
    void execute_draft_marksCancelledAndSaves() {
        PurchaseReturn purchaseReturn = draft();
        when(repository.findById(1L)).thenReturn(Optional.of(purchaseReturn));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        PurchaseReturn result = new CancelDraftPurchaseReturnUseCaseImpl(repository).execute(1L);

        assertThat(result.getStatus()).isEqualTo(PurchaseReturnStatus.CANCELLED);
    }
}
