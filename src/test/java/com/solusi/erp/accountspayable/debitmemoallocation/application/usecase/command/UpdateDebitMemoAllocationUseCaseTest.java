package com.solusi.erp.accountspayable.debitmemoallocation.application.usecase.command;

import com.solusi.erp.accountspayable.debitmemoallocation.domain.model.DebitMemoAllocation;
import com.solusi.erp.accountspayable.debitmemoallocation.domain.port.DebitMemoAllocationSourcePort;
import com.solusi.erp.accountspayable.debitmemoallocation.domain.repository.DebitMemoAllocationRepository;
import com.solusi.erp.accountspayable.debitmemoallocation.domain.service.DebitMemoAllocationProrationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static com.solusi.erp.accountspayable.debitmemoallocation.application.usecase.command.CreateDebitMemoAllocationUseCaseTest.debitMemo;
import static com.solusi.erp.accountspayable.debitmemoallocation.application.usecase.command.CreateDebitMemoAllocationUseCaseTest.vendorBill;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateDebitMemoAllocationUseCaseTest {

    @Mock private DebitMemoAllocationRepository repository;
    @Mock private DebitMemoAllocationSourcePort sourcePort;

    @Test
    void execute_should_recalculate_and_update_draft_lines() {
        DebitMemoAllocation draft = DebitMemoAllocation.createNew(
                "DMA-001", 100L, "DM-100", LocalDate.of(2026, 6, 5), null,
                List.of(new com.solusi.erp.accountspayable.debitmemoallocation.domain.model.DebitMemoAllocationLine(
                        null, 501L, "VB-501", bd("100.0000"), bd("100.0000"),
                        bd("20.0000"), bd("18.0000"), bd("2.0000"), bd("18.0000"), bd("2.0000"),
                        bd("1.000000"), bd("20.0000"), bd("0.0000"), bd("0.0000"))));
        when(repository.findById(10L)).thenReturn(Optional.of(draft));
        when(sourcePort.findDebitMemoSnapshot(100L)).thenReturn(Optional.of(debitMemo()));
        when(sourcePort.findVendorBillSnapshot(502L)).thenReturn(Optional.of(vendorBill(502L)));
        when(repository.save(any(DebitMemoAllocation.class))).thenAnswer(inv -> inv.getArgument(0));
        UpdateDebitMemoAllocationUseCase useCase = new UpdateDebitMemoAllocationUseCaseImpl(
                repository, sourcePort, new DebitMemoAllocationProrationService());

        var detail = useCase.execute(new UpdateDebitMemoAllocationCommand(
                10L, LocalDate.of(2026, 6, 6), "updated",
                List.of(new DebitMemoAllocationLineCommand(502L, bd("50.0000")))));

        assertThat(detail.allocationDate()).isEqualTo(LocalDate.of(2026, 6, 6));
        assertThat(detail.totalAppliedGrossOriginal()).isEqualByComparingTo("50.0000");
        assertThat(detail.lines().getFirst().vendorBillId()).isEqualTo(502L);
    }

    private static BigDecimal bd(String value) {
        return new BigDecimal(value);
    }
}
