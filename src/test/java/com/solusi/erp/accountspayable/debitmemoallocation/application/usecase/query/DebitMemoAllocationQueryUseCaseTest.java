package com.solusi.erp.accountspayable.debitmemoallocation.application.usecase.query;

import com.solusi.erp.accountspayable.debitmemoallocation.domain.model.DebitMemoAllocation;
import com.solusi.erp.accountspayable.debitmemoallocation.domain.model.DebitMemoAllocationLine;
import com.solusi.erp.accountspayable.debitmemoallocation.domain.model.DebitMemoAllocationStatus;
import com.solusi.erp.accountspayable.debitmemoallocation.domain.repository.DebitMemoAllocationHistory;
import com.solusi.erp.accountspayable.debitmemoallocation.domain.repository.DebitMemoAllocationRepository;
import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.core.exception.DomainException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DebitMemoAllocationQueryUseCaseTest {

    @Mock
    private DebitMemoAllocationRepository repository;

    @Test
    void findAllocations_should_map_domain_page_to_summary_page() {
        FindDebitMemoAllocationsUseCase useCase = new FindDebitMemoAllocationsUseCaseImpl(repository);
        DebitMemoAllocation allocation = sampleAllocation();
        Pageable pageable = Pageable.of(0, 20);
        LocalDate from = LocalDate.of(2026, 6, 1);
        LocalDate to = LocalDate.of(2026, 6, 30);
        when(repository.findAll("DMA", 100L, DebitMemoAllocationStatus.DRAFT, from, to, pageable))
                .thenReturn(new Page<>(List.of(allocation), 0, 20, 1));

        Page<DebitMemoAllocationSummaryView> result = useCase.execute(
                "DMA", 100L, DebitMemoAllocationStatus.DRAFT, from, to, pageable);

        assertThat(result.content()).hasSize(1);
        DebitMemoAllocationSummaryView summary = result.content().getFirst();
        assertThat(summary.code()).isEqualTo("DMA-202606-00001");
        assertThat(summary.totalAppliedGrossOriginal()).isEqualByComparingTo("100.0000");
    }

    @Test
    void getDetail_should_map_lines_and_totals() {
        GetDebitMemoAllocationDetailUseCase useCase = new GetDebitMemoAllocationDetailUseCaseImpl(repository);
        DebitMemoAllocation allocation = sampleAllocation();
        when(repository.findById(10L)).thenReturn(Optional.of(allocation));

        DebitMemoAllocationDetailView detail = useCase.execute(10L);

        assertThat(detail.code()).isEqualTo("DMA-202606-00001");
        assertThat(detail.lines()).hasSize(2);
        assertThat(detail.totalAppliedGrossOriginal()).isEqualByComparingTo("100.0000");
        assertThat(detail.lines().getFirst().vendorBillCode()).isEqualTo("VB-202606-501");
    }

    @Test
    void getDetail_should_fail_when_not_found() {
        GetDebitMemoAllocationDetailUseCase useCase = new GetDebitMemoAllocationDetailUseCaseImpl(repository);
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(99L))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.debit-memo-allocation.not-found");
    }

    @Test
    void historyUseCase_should_map_debit_memo_and_vendor_bill_history() {
        FindDebitMemoAllocationHistoryUseCase useCase = new FindDebitMemoAllocationHistoryUseCaseImpl(repository);
        DebitMemoAllocationHistory history = new DebitMemoAllocationHistory(
                10L,
                "DMA-202606-00001",
                100L,
                "DM-202606-00001",
                501L,
                "VB-202606-501",
                LocalDate.of(2026, 6, 5),
                DebitMemoAllocationStatus.CONFIRMED,
                new BigDecimal("60.0000"),
                new BigDecimal("60.0000"),
                9001L,
                null,
                null
        );
        when(repository.findHistoryByDebitMemoId(100L)).thenReturn(List.of(history));
        when(repository.findHistoryByVendorBillId(501L)).thenReturn(List.of(history));

        assertThat(useCase.byDebitMemoId(100L)).hasSize(1);
        assertThat(useCase.byVendorBillId(501L).getFirst().applyJournalEntryId()).isEqualTo(9001L);
    }

    private DebitMemoAllocation sampleAllocation() {
        return DebitMemoAllocation.reconstitute(
                new AuditMetadata(10L, 1L, LocalDateTime.now(), 1L, LocalDateTime.now(), 1L),
                "DMA-202606-00001",
                100L,
                "DM-202606-00001",
                LocalDate.of(2026, 6, 5),
                DebitMemoAllocationStatus.DRAFT,
                null,
                null,
                null,
                null,
                "notes",
                List.of(line(501L, "60.0000"), line(502L, "40.0000"))
        );
    }

    private DebitMemoAllocationLine line(Long vendorBillId, String amount) {
        return new DebitMemoAllocationLine(
                null,
                vendorBillId,
                "VB-202606-" + vendorBillId,
                new BigDecimal("100.0000"),
                new BigDecimal("100.0000"),
                new BigDecimal(amount),
                new BigDecimal(amount),
                BigDecimal.ZERO,
                new BigDecimal(amount),
                BigDecimal.ZERO,
                BigDecimal.ONE,
                new BigDecimal(amount),
                BigDecimal.ZERO,
                BigDecimal.ZERO
        );
    }
}
