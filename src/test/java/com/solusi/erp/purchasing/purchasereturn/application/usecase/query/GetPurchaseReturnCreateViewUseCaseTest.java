package com.solusi.erp.purchasing.purchasereturn.application.usecase.query;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.purchasing.purchasereturn.domain.port.PurchaseReturnSourceQueryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static com.solusi.erp.purchasing.purchasereturn.application.usecase.query.FindPurchaseReturnGrLineSelectorUseCaseTest.row;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetPurchaseReturnCreateViewUseCaseTest {

    @Mock
    private PurchaseReturnSourceQueryPort queryPort;

    @Test
    void execute_eligibleSource_returnsHeaderAndPrefillSlices() {
        EligibleGoodsReceiptRow source = source();
        when(queryPort.findEligibleGoodsReceiptById(1L)).thenReturn(Optional.of(source));
        when(queryPort.findReturnableGrLineSlices(1L, null, List.of())).thenReturn(List.of(row("1:40")));

        GetPurchaseReturnCreateViewUseCase.PurchaseReturnCreateView view =
                new GetPurchaseReturnCreateViewUseCaseImpl(queryPort).execute(1L);

        assertThat(view.source()).isEqualTo(source);
        assertThat(view.lines()).hasSize(1);
    }

    @Test
    void execute_staleSource_rejects() {
        when(queryPort.findEligibleGoodsReceiptById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> new GetPurchaseReturnCreateViewUseCaseImpl(queryPort).execute(1L))
                .isInstanceOf(DomainException.class)
                .hasMessage("msg.error.purchase-return.source.ineligible");
    }

    private EligibleGoodsReceiptRow source() {
        return new EligibleGoodsReceiptRow(
                1L, "GR-001", 2L, "PO-001", 3L, "Supplier", LocalDate.of(2026, 6, 1),
                4L, "Main", 5L, "IDR", BigDecimal.ONE, 1, BigDecimal.TEN
        );
    }
}
