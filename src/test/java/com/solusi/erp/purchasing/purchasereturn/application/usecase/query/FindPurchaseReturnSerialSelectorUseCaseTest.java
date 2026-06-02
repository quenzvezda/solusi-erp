package com.solusi.erp.purchasing.purchasereturn.application.usecase.query;

import com.solusi.erp.purchasing.purchasereturn.domain.port.PurchaseReturnSourceQueryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FindPurchaseReturnSerialSelectorUseCaseTest {

    @Mock
    private PurchaseReturnSourceQueryPort queryPort;

    @Test
    void execute_mapsSerialRowsToPage() {
        ReturnableSerialRow row = new ReturnableSerialRow(
                "1:40:SER-001", 1L, 1L, 10L, "Product", "P-001", 20L, "Piece", "PCS",
                30L, "Main", 35L, "A", 40L, "BIN", "SER-001", "GOODS_RECEIPT", 1L, 1L,
                BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO
        );
        when(queryPort.findReturnableSerials(1L, 1L, null, List.of())).thenReturn(List.of(row));

        Page<ReturnableSerialRow> page = new FindPurchaseReturnSerialsUseCaseImpl(queryPort)
                .execute(1L, 1L, null, List.of(), PageRequest.of(0, 10));

        assertThat(page.getContent()).containsExactly(row);
    }
}
