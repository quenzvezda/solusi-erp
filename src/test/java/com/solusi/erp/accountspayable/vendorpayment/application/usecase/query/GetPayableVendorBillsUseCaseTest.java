package com.solusi.erp.accountspayable.vendorpayment.application.usecase.query;

import com.solusi.erp.accountspayable.vendorpayment.domain.port.PayableVendorBillQueryPort;
import com.solusi.erp.accountspayable.vendorpayment.domain.port.PayableVendorBillQueryPort.PayableVendorBillView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetPayableVendorBillsUseCase Tests")
class GetPayableVendorBillsUseCaseTest {

    @Mock private PayableVendorBillQueryPort queryPort;

    private GetPayableVendorBillsUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetPayableVendorBillsUseCaseImpl(queryPort);
    }

    @Test
    @DisplayName("execute returns outstanding vendor bills for vendor and currency")
    void execute_returnsOutstandingBills() {
        List<PayableVendorBillView> expected = List.of(
                new PayableVendorBillView(1L, "VB-001", new BigDecimal("1000.00"),
                        new BigDecimal("300.00"), new BigDecimal("700.00")),
                new PayableVendorBillView(2L, "VB-002", new BigDecimal("500.00"),
                        BigDecimal.ZERO, new BigDecimal("500.00"))
        );
        when(queryPort.findPayableVendorBills(1L, 1L)).thenReturn(expected);

        List<PayableVendorBillView> result = useCase.execute(1L, 1L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).outstandingAmount()).isEqualByComparingTo("700.00");
        assertThat(result.get(1).outstandingAmount()).isEqualByComparingTo("500.00");
    }

    @Test
    @DisplayName("execute returns empty list when no outstanding bills")
    void execute_returnsEmptyWhenNoBills() {
        when(queryPort.findPayableVendorBills(1L, 1L)).thenReturn(List.of());

        List<PayableVendorBillView> result = useCase.execute(1L, 1L);

        assertThat(result).isEmpty();
    }
}
