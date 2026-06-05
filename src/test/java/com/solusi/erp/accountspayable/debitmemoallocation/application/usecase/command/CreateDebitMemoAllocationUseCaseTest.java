package com.solusi.erp.accountspayable.debitmemoallocation.application.usecase.command;

import com.solusi.erp.accountspayable.debitmemoallocation.domain.model.DebitMemoAllocation;
import com.solusi.erp.accountspayable.debitmemoallocation.domain.model.DebitMemoAllocationStatus;
import com.solusi.erp.accountspayable.debitmemoallocation.domain.port.DebitMemoAllocationSourcePort;
import com.solusi.erp.accountspayable.debitmemoallocation.domain.repository.DebitMemoAllocationRepository;
import com.solusi.erp.accountspayable.debitmemoallocation.domain.service.DebitMemoAllocationProrationService;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.core.infrastructure.sequence.SequenceGeneratorService;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateDebitMemoAllocationUseCaseTest {

    @Mock private DebitMemoAllocationRepository repository;
    @Mock private DebitMemoAllocationSourcePort sourcePort;
    @Mock private SequenceGeneratorService sequenceGeneratorService;

    @Test
    void execute_should_create_draft_from_debit_memo_and_vendor_bill_snapshots() {
        CreateDebitMemoAllocationUseCase useCase = new CreateDebitMemoAllocationUseCaseImpl(
                repository, sourcePort, sequenceGeneratorService, new DebitMemoAllocationProrationService());
        when(sequenceGeneratorService.generate("DEBIT_MEMO_ALLOCATION")).thenReturn("DMA-202606-00001");
        when(sourcePort.findDebitMemoSnapshot(100L)).thenReturn(Optional.of(debitMemo()));
        when(sourcePort.findVendorBillSnapshot(501L)).thenReturn(Optional.of(vendorBill(501L)));
        when(repository.save(any(DebitMemoAllocation.class))).thenAnswer(inv -> inv.getArgument(0));

        useCase.execute(new CreateDebitMemoAllocationCommand(
                100L,
                LocalDate.of(2026, 6, 5),
                "notes",
                List.of(new DebitMemoAllocationLineCommand(501L, new BigDecimal("40.0000")))
        ));

        ArgumentCaptor<DebitMemoAllocation> captor = ArgumentCaptor.forClass(DebitMemoAllocation.class);
        verify(repository).save(captor.capture());
        DebitMemoAllocation saved = captor.getValue();
        assertThat(saved.getCode()).isEqualTo("DMA-202606-00001");
        assertThat(saved.getStatus()).isEqualTo(DebitMemoAllocationStatus.DRAFT);
        assertThat(saved.getTotalAppliedGrossOriginal()).isEqualByComparingTo("40.0000");
        assertThat(saved.getLines().getFirst().getVendorBillCode()).isEqualTo("VB-501");
    }

    @Test
    void execute_should_reject_vendor_mismatch() {
        CreateDebitMemoAllocationUseCase useCase = new CreateDebitMemoAllocationUseCaseImpl(
                repository, sourcePort, sequenceGeneratorService, new DebitMemoAllocationProrationService());
        when(sourcePort.findDebitMemoSnapshot(100L)).thenReturn(Optional.of(debitMemo()));
        when(sourcePort.findVendorBillSnapshot(501L)).thenReturn(Optional.of(new DebitMemoAllocationSourcePort.VendorBillSnapshot(
                501L, "VB-501", 999L, 1L, bd("100.0000"), bd("1.000000"), bd("100.0000"))));

        assertThatThrownBy(() -> useCase.execute(new CreateDebitMemoAllocationCommand(
                100L, LocalDate.of(2026, 6, 5), null,
                List.of(new DebitMemoAllocationLineCommand(501L, bd("40.0000"))))))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.debit-memo-allocation.vendor-mismatch");
    }

    static DebitMemoAllocationSourcePort.DebitMemoSnapshot debitMemo() {
        return new DebitMemoAllocationSourcePort.DebitMemoSnapshot(
                100L, "DM-100", 10L, 1L,
                bd("100.0000"), bd("90.0000"), bd("10.0000"),
                bd("90.0000"), bd("10.0000"), bd("100.0000"),
                bd("0.0000"), bd("0.0000"), bd("0.0000"), bd("0.0000"), bd("0.0000"));
    }

    static DebitMemoAllocationSourcePort.VendorBillSnapshot vendorBill(Long id) {
        return new DebitMemoAllocationSourcePort.VendorBillSnapshot(
                id, "VB-" + id, 10L, 1L, bd("100.0000"), bd("1.000000"), bd("100.0000"));
    }

    static BigDecimal bd(String value) {
        return new BigDecimal(value);
    }
}
