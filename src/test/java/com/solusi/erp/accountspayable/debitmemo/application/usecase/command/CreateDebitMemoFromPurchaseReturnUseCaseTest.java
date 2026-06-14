package com.solusi.erp.accountspayable.debitmemo.application.usecase.command;

import com.solusi.erp.accountspayable.debitmemo.domain.model.DebitMemo;
import com.solusi.erp.accountspayable.debitmemo.domain.model.DebitMemoLine;
import com.solusi.erp.accountspayable.debitmemo.domain.model.DebitMemoSettlementStatus;
import com.solusi.erp.accountspayable.debitmemo.domain.repository.DebitMemoRepository;
import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.core.infrastructure.sequence.SequenceGeneratorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateDebitMemoFromPurchaseReturnUseCaseTest {

    @Mock
    private DebitMemoRepository repository;

    @Mock
    private SequenceGeneratorService sequenceGeneratorService;

    @Test
    void execute_should_create_open_debit_memo_from_purchase_return_snapshot() {
        CreateDebitMemoFromPurchaseReturnUseCaseImpl useCase = useCase();
        DebitMemoPurchaseReturnSource source = source();
        ArgumentCaptor<DebitMemo> captor = ArgumentCaptor.forClass(DebitMemo.class);

        when(repository.findByPurchaseReturnId(100L)).thenReturn(Optional.empty());
        when(sequenceGeneratorService.generate("DEBIT_MEMO")).thenReturn("DM-202606-00001");
        when(repository.save(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        DebitMemo result = useCase.execute(source);

        assertThat(result.getCode()).isEqualTo("DM-202606-00001");
        assertThat(result.getPurchaseReturnId()).isEqualTo(100L);
        assertThat(result.getPurchaseReturnCode()).isEqualTo("PRT-202606-00001");
        assertThat(result.getSettlementStatus()).isEqualTo(DebitMemoSettlementStatus.OPEN);
        assertThat(result.getDppAmountOriginal()).isEqualByComparingTo("100.0000");
        assertThat(result.getTaxAmountOriginal()).isEqualByComparingTo("11.0000");
        assertThat(result.getGrossAmountOriginal()).isEqualByComparingTo("111.0000");
        assertThat(captor.getValue().getLines()).hasSize(1);
        assertThat(captor.getValue().getLines().getFirst().getDppAmountBase()).isEqualByComparingTo("100.0000");
    }

    @Test
    void execute_existing_debit_memo_should_return_existing_without_generating_sequence() {
        CreateDebitMemoFromPurchaseReturnUseCaseImpl useCase = useCase();
        DebitMemo existing = existingDebitMemo();

        when(repository.findByPurchaseReturnId(100L)).thenReturn(Optional.of(existing));

        DebitMemo result = useCase.execute(source());

        assertThat(result).isSameAs(existing);
        verify(sequenceGeneratorService, never()).generate("DEBIT_MEMO");
        verify(repository, never()).save(existing);
    }

    @Test
    void execute_zero_gross_snapshot_should_fail_before_save() {
        CreateDebitMemoFromPurchaseReturnUseCaseImpl useCase = useCase();
        DebitMemoPurchaseReturnSource source = new DebitMemoPurchaseReturnSource(
                100L,
                "PRT-202606-00001",
                22L,
                1L,
                LocalDate.of(2026, 6, 2),
                List.of(new DebitMemoPurchaseReturnLineSource(
                        1001L,
                        501L,
                        new BigDecimal("2.0000"),
                        1L,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO
                ))
        );

        when(repository.findByPurchaseReturnId(100L)).thenReturn(Optional.empty());
        when(sequenceGeneratorService.generate("DEBIT_MEMO")).thenReturn("DM-202606-00001");

        assertThatThrownBy(() -> useCase.execute(source))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.debit-memo.gross-positive");
        verify(repository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    private CreateDebitMemoFromPurchaseReturnUseCaseImpl useCase() {
        return new CreateDebitMemoFromPurchaseReturnUseCaseImpl(repository, sequenceGeneratorService);
    }

    private DebitMemoPurchaseReturnSource source() {
        return new DebitMemoPurchaseReturnSource(
                100L,
                "PRT-202606-00001",
                22L,
                1L,
                LocalDate.of(2026, 6, 2),
                List.of(new DebitMemoPurchaseReturnLineSource(
                        1001L,
                        501L,
                        new BigDecimal("2.0000"),
                        1L,
                        new BigDecimal("100.0000"),
                        new BigDecimal("11.0000")
                ))
        );
    }

    private DebitMemo existingDebitMemo() {
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
                null,
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

