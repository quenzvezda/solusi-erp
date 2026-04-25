package com.solusi.erp.purchasing.purchaseorder.application.usecase.query;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrder;
import com.solusi.erp.purchasing.purchaseorder.domain.repository.PurchaseOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FindPurchaseOrdersUseCase Tests")
class FindPurchaseOrdersUseCaseTest {

    @Mock
    private PurchaseOrderRepository repository;

    private FindPurchaseOrdersUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new FindPurchaseOrdersUseCaseImpl(repository);
    }

    @Test
    @DisplayName("execute delegates to repository with keyword and pageable")
    void execute_delegatesToRepository() {
        Pageable pageable = Pageable.of(0, 10);
        Page<PurchaseOrder> expectedPage = new Page<>(List.of(), 0, 10, 0);
        when(repository.findAll(eq("test"), any(Pageable.class))).thenReturn(expectedPage);

        Page<PurchaseOrder> result = useCase.execute("test", pageable);

        assertThat(result).isEqualTo(expectedPage);
        verify(repository).findAll("test", pageable);
    }
}
