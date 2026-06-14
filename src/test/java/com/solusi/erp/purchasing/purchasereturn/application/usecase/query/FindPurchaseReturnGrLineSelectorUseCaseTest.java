package com.solusi.erp.purchasing.purchasereturn.application.usecase.query;

import com.solusi.erp.purchasing.purchasereturn.domain.port.PurchaseReturnSourceQueryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FindPurchaseReturnGrLineSelectorUseCaseTest {

    @Mock
    private PurchaseReturnSourceQueryPort queryPort;

    @Test
    void execute_mapsSelectorRowsToRequestedPage() {
        ReturnableGrLineSlice row = row("1:40");
        when(queryPort.findReturnableGrLineSlices(1L, null, List.of())).thenReturn(List.of(row, row("2:41")));

        Page<ReturnableGrLineSlice> page = new FindPurchaseReturnGrLineSlicesUseCaseImpl(queryPort)
                .execute(1L, null, List.of(), PageRequest.of(1, 1));

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).extracting(ReturnableGrLineSlice::selectionKey).containsExactly("2:41");
    }

    static ReturnableGrLineSlice row(String selectionKey) {
        return new ReturnableGrLineSlice(
                selectionKey, 1L, 1L, 10L, "Product", "P-001", false, 20L, "Piece", "PCS",
                30L, "Main", 35L, "Grid A", "A", 40L, "Bin", "BIN", java.math.BigDecimal.ONE,
                "GOODS_RECEIPT", 1L, 1L, java.math.BigDecimal.ONE, java.math.BigDecimal.ONE,
                java.math.BigDecimal.ZERO, java.math.BigDecimal.ZERO
        );
    }
}
