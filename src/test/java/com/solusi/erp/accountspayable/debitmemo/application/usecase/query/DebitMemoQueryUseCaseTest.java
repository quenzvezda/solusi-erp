package com.solusi.erp.accountspayable.debitmemo.application.usecase.query;

import com.solusi.erp.accountspayable.debitmemo.domain.model.DebitMemo;
import com.solusi.erp.accountspayable.debitmemo.domain.model.DebitMemoLine;
import com.solusi.erp.accountspayable.debitmemo.domain.model.DebitMemoSettlementStatus;
import com.solusi.erp.accountspayable.debitmemo.domain.repository.DebitMemoRepository;
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
class DebitMemoQueryUseCaseTest {

    @Mock
    private DebitMemoRepository repository;

    @Test
    void findDebitMemos_should_return_phase_d_zero_settlement_recap() {
        FindDebitMemosUseCase useCase = new FindDebitMemosUseCaseImpl(repository);
        DebitMemo debitMemo = sampleDebitMemo();
        Pageable pageable = Pageable.of(0, 20);
        LocalDate from = LocalDate.of(2026, 6, 1);
        LocalDate to = LocalDate.of(2026, 6, 30);

        when(repository.findAll("PRT", 22L, DebitMemoSettlementStatus.OPEN, from, to, pageable))
                .thenReturn(new Page<>(List.of(debitMemo), 0, 20, 1));

        Page<DebitMemoSummaryView> result = useCase.execute("PRT", 22L, DebitMemoSettlementStatus.OPEN, from, to, pageable);

        assertThat(result.content()).hasSize(1);
        DebitMemoSummaryView summary = result.content().getFirst();
        assertThat(summary.grossAmountOriginal()).isEqualByComparingTo("111.0000");
        assertThat(summary.settledAmount()).isEqualByComparingTo("0.0000");
        assertThat(summary.refundedAmount()).isEqualByComparingTo("0.0000");
        assertThat(summary.remainingAmount()).isEqualByComparingTo("111.0000");
    }

    @Test
    void getDebitMemoDetail_should_return_lines_and_phase_d_zero_settlement_recap() {
        GetDebitMemoDetailUseCase useCase = new GetDebitMemoDetailUseCaseImpl(repository);
        DebitMemo debitMemo = sampleDebitMemo();

        when(repository.findById(10L)).thenReturn(Optional.of(debitMemo));

        DebitMemoDetailView detail = useCase.execute(10L);

        assertThat(detail.code()).isEqualTo("DM-202606-00001");
        assertThat(detail.remainingAmount()).isEqualByComparingTo("111.0000");
        assertThat(detail.lines()).hasSize(1);
        assertThat(detail.lines().getFirst().purchaseReturnLineId()).isEqualTo(1001L);
    }

    @Test
    void getDebitMemoDetail_should_fail_when_not_found() {
        GetDebitMemoDetailUseCase useCase = new GetDebitMemoDetailUseCaseImpl(repository);
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(99L))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.debit-memo.not-found");
    }

    @Test
    void findByPurchaseReturn_should_return_source_link() {
        FindDebitMemoByPurchaseReturnUseCase useCase = new FindDebitMemoByPurchaseReturnUseCaseImpl(repository);
        DebitMemo debitMemo = sampleDebitMemo();

        when(repository.findByPurchaseReturnId(100L)).thenReturn(Optional.of(debitMemo));

        Optional<DebitMemoSourceLinkView> result = useCase.execute(100L);

        assertThat(result).isPresent();
        assertThat(result.orElseThrow().code()).isEqualTo("DM-202606-00001");
        assertThat(result.orElseThrow().settlementStatus()).isEqualTo(DebitMemoSettlementStatus.OPEN);
    }

    private DebitMemo sampleDebitMemo() {
        return DebitMemo.reconstitute(
                new AuditMetadata(10L, 1L, LocalDateTime.now(), 1L, LocalDateTime.now(), 1L),
                "DM-202606-00001",
                100L,
                "PRT-202606-00001",
                22L,
                1L,
                LocalDate.of(2026, 6, 2),
                DebitMemoSettlementStatus.OPEN,
                null,
                null,
                null,
                null,
                "note",
                List.of(new DebitMemoLine(
                        1L,
                        1001L,
                        501L,
                        new BigDecimal("2.0000"),
                        1L,
                        new BigDecimal("100.0000"),
                        new BigDecimal("11.0000"),
                        new BigDecimal("100.0000"),
                        new BigDecimal("11.0000")
                ))
        );
    }
}

