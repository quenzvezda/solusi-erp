package com.solusi.erp.accountspayable.debitmemoallocation.application.usecase.command;

import com.solusi.erp.accountspayable.debitmemoallocation.domain.model.DebitMemoAllocation;
import com.solusi.erp.accountspayable.debitmemoallocation.domain.model.DebitMemoAllocationLine;
import com.solusi.erp.accountspayable.debitmemoallocation.domain.model.DebitMemoAllocationStatus;
import com.solusi.erp.accountspayable.debitmemoallocation.domain.repository.DebitMemoAllocationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CancelDebitMemoAllocationUseCaseTest {

    @Mock private DebitMemoAllocationRepository repository;

    @Test
    void execute_should_cancel_draft() {
        DebitMemoAllocation draft = DebitMemoAllocation.createNew(
                "DMA-001", 100L, "DM-100", LocalDate.of(2026, 6, 5), null,
                List.of(new DebitMemoAllocationLine(null, 501L, "VB-501", bd("100.0000"), bd("100.0000"),
                        bd("20.0000"), bd("18.0000"), bd("2.0000"), bd("18.0000"), bd("2.0000"),
                        bd("1.000000"), bd("20.0000"), bd("0.0000"), bd("0.0000"))));
        when(repository.findById(10L)).thenReturn(Optional.of(draft));

        new CancelDebitMemoAllocationUseCaseImpl(repository).execute(10L);

        ArgumentCaptor<DebitMemoAllocation> captor = ArgumentCaptor.forClass(DebitMemoAllocation.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(DebitMemoAllocationStatus.CANCELLED);
    }

    private static BigDecimal bd(String value) {
        return new BigDecimal(value);
    }
}
