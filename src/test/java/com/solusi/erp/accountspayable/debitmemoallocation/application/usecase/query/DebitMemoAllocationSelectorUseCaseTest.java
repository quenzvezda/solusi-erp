package com.solusi.erp.accountspayable.debitmemoallocation.application.usecase.query;

import com.solusi.erp.accountspayable.debitmemoallocation.domain.port.DebitMemoAllocationSourcePort;
import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DebitMemoAllocationSelectorUseCaseTest {

    @Mock private DebitMemoAllocationSourcePort sourcePort;

    @Test
    void eligibleVendorBills_should_delegate_to_source_port() {
        Pageable pageable = Pageable.of(0, 20);
        when(sourcePort.findEligibleVendorBills(100L, "VB", pageable)).thenReturn(new Page<>(List.of(
                new DebitMemoAllocationSourcePort.EligibleVendorBill(501L, "VB-501", bd("100.0000"), bd("40.0000"), bd("1.000000"))
        ), 0, 20, 1));

        Page<DebitMemoAllocationSourcePort.EligibleVendorBill> result =
                new DebitMemoAllocationSelectorUseCaseImpl(sourcePort).eligibleVendorBills(100L, "VB", pageable);

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().getFirst().outstandingAmount()).isEqualByComparingTo("40.0000");
    }

    @Test
    void eligibleDebitMemos_should_delegate_to_source_port() {
        Pageable pageable = Pageable.of(0, 20);
        when(sourcePort.findEligibleDebitMemos(501L, "DM", pageable)).thenReturn(new Page<>(List.of(
                new DebitMemoAllocationSourcePort.EligibleDebitMemo(100L, "DM-100", bd("100.0000"), bd("60.0000"))
        ), 0, 20, 1));

        Page<DebitMemoAllocationSourcePort.EligibleDebitMemo> result =
                new DebitMemoAllocationSelectorUseCaseImpl(sourcePort).eligibleDebitMemos(501L, "DM", pageable);

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().getFirst().remainingAmountOriginal()).isEqualByComparingTo("60.0000");
    }

    private static BigDecimal bd(String value) {
        return new BigDecimal(value);
    }
}
