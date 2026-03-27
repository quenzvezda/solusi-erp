package com.solusi.erp.inventory.report.application.usecase;

import com.solusi.erp.inventory.dto.ProductStockSummaryResponse;
import com.solusi.erp.inventory.report.application.usecase.query.GetOnHandSummaryUseCaseImpl;
import com.solusi.erp.inventory.repository.StockBalanceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetOnHandSummaryUseCase Tests")
class GetOnHandSummaryUseCaseTest {

    @Mock
    private StockBalanceRepository stockBalanceRepository;

    private GetOnHandSummaryUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetOnHandSummaryUseCaseImpl(stockBalanceRepository);
    }

    @Test
    @DisplayName("execute forwards keyword and pageable to repository")
    void execute_forwardsToRepository() {
        String keyword = "widget";
        Pageable pageable = PageRequest.of(0, 10);
        ProductStockSummaryResponse summary = ProductStockSummaryResponse.builder()
                .productId(1L)
                .productCode("P001")
                .productName("Widget")
                .build();
        Page<ProductStockSummaryResponse> expected = new PageImpl<>(List.of(summary), pageable, 1);

        when(stockBalanceRepository.getOnHandSummary(keyword, pageable)).thenReturn(expected);

        Page<ProductStockSummaryResponse> result = useCase.execute(keyword, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1L);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getProductCode()).isEqualTo("P001");
        verify(stockBalanceRepository).getOnHandSummary(keyword, pageable);
    }

    @Test
    @DisplayName("execute passes null keyword to repository")
    void execute_withNullKeyword() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ProductStockSummaryResponse> expected = new PageImpl<>(List.of(), pageable, 0);

        when(stockBalanceRepository.getOnHandSummary(null, pageable)).thenReturn(expected);

        Page<ProductStockSummaryResponse> result = useCase.execute(null, pageable);

        assertThat(result.getTotalElements()).isZero();
        verify(stockBalanceRepository).getOnHandSummary(null, pageable);
    }
}
