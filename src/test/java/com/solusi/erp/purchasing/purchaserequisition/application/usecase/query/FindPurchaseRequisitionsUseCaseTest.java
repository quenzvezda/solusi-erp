package com.solusi.erp.purchasing.purchaserequisition.application.usecase.query;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.purchasing.purchaserequisition.domain.model.PurchaseRequisition;
import com.solusi.erp.purchasing.purchaserequisition.domain.repository.PurchaseRequisitionRepository;
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
@DisplayName("FindPurchaseRequisitionsUseCase Tests")
class FindPurchaseRequisitionsUseCaseTest {

    @Mock
    private PurchaseRequisitionRepository repository;

    private FindPurchaseRequisitionsUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new FindPurchaseRequisitionsUseCaseImpl(repository);
    }

    @Test
    @DisplayName("execute returns page from repository")
    void execute_returnsPageFromRepository() {
        Pageable pageable = new Pageable(0, 10);
        Page<PurchaseRequisition> expectedPage = new Page<>(List.of(), 0, 10, 0L);

        when(repository.findAll(eq("test"), any(Pageable.class))).thenReturn(expectedPage);

        Page<PurchaseRequisition> result = useCase.execute("test", pageable);

        assertThat(result).isEqualTo(expectedPage);
        verify(repository).findAll("test", pageable);
    }
}
