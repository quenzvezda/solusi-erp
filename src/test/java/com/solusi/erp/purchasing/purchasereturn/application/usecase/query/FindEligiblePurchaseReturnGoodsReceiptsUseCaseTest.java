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
class FindEligiblePurchaseReturnGoodsReceiptsUseCaseTest {

    @Mock
    private PurchaseReturnSourceQueryPort queryPort;

    @Test
    void execute_emptyResult_returnsEmptyPage() {
        when(queryPort.findEligibleGoodsReceipts(null, null, null, null, null)).thenReturn(List.of());

        Page<EligibleGoodsReceiptRow> page = new FindEligiblePurchaseReturnGoodsReceiptsUseCaseImpl(queryPort)
                .execute(null, null, null, null, null, PageRequest.of(0, 10));

        assertThat(page).isEmpty();
    }
}
