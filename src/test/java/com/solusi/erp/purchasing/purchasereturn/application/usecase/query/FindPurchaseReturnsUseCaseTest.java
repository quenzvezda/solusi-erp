package com.solusi.erp.purchasing.purchasereturn.application.usecase.query;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturn;
import com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturnStatus;
import com.solusi.erp.purchasing.purchasereturn.domain.repository.PurchaseReturnRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FindPurchaseReturnsUseCaseTest {

    @Mock private PurchaseReturnRepository repository;

    @Test
    void execute_forwardsKeywordStatusAndPaging() {
        Pageable pageable = Pageable.of(0, 10);
        Page<PurchaseReturn> expected = new Page<>(List.of(), 0, 10, 0);
        when(repository.findAll("PRT", PurchaseReturnStatus.DRAFT, pageable)).thenReturn(expected);

        Page<PurchaseReturn> result = new FindPurchaseReturnsUseCaseImpl(repository)
                .execute("PRT", PurchaseReturnStatus.DRAFT, pageable);

        assertThat(result).isSameAs(expected);
    }
}
