package com.solusi.erp.purchasing.purchasereturn.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturnStatus;
import com.solusi.erp.purchasing.purchasereturn.domain.port.PurchaseReturnSourceQueryPort;
import com.solusi.erp.purchasing.purchasereturn.domain.repository.PurchaseReturnRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static com.solusi.erp.purchasing.purchasereturn.application.usecase.command.PurchaseReturnDraftTestFixtures.command;
import static com.solusi.erp.purchasing.purchasereturn.application.usecase.command.PurchaseReturnDraftTestFixtures.withStatus;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdatePurchaseReturnUseCaseTest {

    @Mock private PurchaseReturnRepository repository;
    @Mock private PurchaseReturnSourceQueryPort queryPort;

    @Test
    void execute_nonDraft_rejectsBeforeRefreshingSource() {
        when(repository.findById(1L)).thenReturn(Optional.of(withStatus(PurchaseReturnStatus.SUBMITTED)));

        assertThatThrownBy(() -> new UpdatePurchaseReturnUseCaseImpl(repository, queryPort).execute(
                1L, LocalDate.now(), com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturnReason.DAMAGED,
                null, List.of(command(BigDecimal.ONE))))
                .isInstanceOf(DomainException.class)
                .hasMessage("msg.error.purchase-return.update.not-draft");
    }
}
