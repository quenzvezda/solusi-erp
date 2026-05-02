package com.solusi.erp.inventory.goodsreceipt.application.usecase.query;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.inventory.goodsreceipt.domain.model.GoodsReceipt;
import com.solusi.erp.inventory.goodsreceipt.domain.model.GoodsReceiptReferenceType;
import com.solusi.erp.inventory.goodsreceipt.domain.repository.GoodsReceiptRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FindGoodsReceiptsUseCase Tests")
class FindGoodsReceiptsUseCaseTest {

    @Mock
    private GoodsReceiptRepository goodsReceiptRepository;

    private FindGoodsReceiptsUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new FindGoodsReceiptsUseCaseImpl(goodsReceiptRepository);
    }

    @Test
    void execute_delegatesToRepository() {
        Pageable pageable = Pageable.of(0, 10, "receiptDate", "desc");
        Page<GoodsReceipt> expected = new Page<>(List.of(), 0, 10, 0);
        when(goodsReceiptRepository.findAll("PO-0007", GoodsReceiptReferenceType.PURCHASE_ORDER, 7L, pageable))
            .thenReturn(expected);

        Page<GoodsReceipt> result = useCase.execute("PO-0007", GoodsReceiptReferenceType.PURCHASE_ORDER, 7L, pageable);

        assertThat(result).isSameAs(expected);
        verify(goodsReceiptRepository).findAll("PO-0007", GoodsReceiptReferenceType.PURCHASE_ORDER, 7L, pageable);
    }
}
