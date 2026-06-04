package com.solusi.erp.accountspayable.debitmemo.application.usecase.command;

import com.solusi.erp.accountspayable.debitmemo.domain.model.DebitMemo;
import com.solusi.erp.accountspayable.debitmemo.domain.model.DebitMemoLine;
import com.solusi.erp.accountspayable.debitmemo.domain.model.DebitMemoSettlementStatus;
import com.solusi.erp.accountspayable.debitmemo.domain.port.DebitMemoAllocationConsumptionPort;
import com.solusi.erp.accountspayable.debitmemo.domain.repository.DebitMemoRepository;
import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;
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
class DebitMemoCommandUseCaseTest {

    @Mock
    private DebitMemoRepository repository;

    @Mock
    private DebitMemoAllocationConsumptionPort allocationConsumptionPort;

    @Test
    void updateMetadata_should_trim_validate_uniqueness_and_save() {
        UpdateDebitMemoMetadataUseCaseImpl useCase = new UpdateDebitMemoMetadataUseCaseImpl(repository);
        DebitMemo debitMemo = debitMemo(DebitMemoSettlementStatus.OPEN);
        ArgumentCaptor<DebitMemo> captor = ArgumentCaptor.forClass(DebitMemo.class);

        when(repository.findById(10L)).thenReturn(Optional.of(debitMemo));
        when(repository.existsSupplierMemoNumber(22L, "SUP-DM-001", 10L)).thenReturn(false);
        when(repository.existsTaxDocumentNumber("TAX-001", 10L)).thenReturn(false);
        when(repository.save(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        DebitMemo result = useCase.execute(
                10L,
                "  SUP-DM-001  ",
                LocalDate.of(2026, 6, 3),
                " TAX-001 ",
                LocalDate.of(2026, 6, 4),
                "notes"
        );

        assertThat(result.getSupplierMemoNumber()).isEqualTo("SUP-DM-001");
        assertThat(result.getTaxDocumentNumber()).isEqualTo("TAX-001");
        assertThat(captor.getValue().getSupplierMemoDate()).isEqualTo(LocalDate.of(2026, 6, 3));
    }

    @Test
    void updateMetadata_duplicateSupplierMemo_should_fail_with_friendly_key() {
        UpdateDebitMemoMetadataUseCaseImpl useCase = new UpdateDebitMemoMetadataUseCaseImpl(repository);
        DebitMemo debitMemo = debitMemo(DebitMemoSettlementStatus.OPEN);

        when(repository.findById(10L)).thenReturn(Optional.of(debitMemo));
        when(repository.existsSupplierMemoNumber(22L, "SUP-DM-001", 10L)).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(10L, "SUP-DM-001", null, null, null, null))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.debit-memo.supplier-memo-number-duplicate");
        verify(repository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void updateMetadata_duplicateTaxDocument_should_fail_with_friendly_key() {
        UpdateDebitMemoMetadataUseCaseImpl useCase = new UpdateDebitMemoMetadataUseCaseImpl(repository);
        DebitMemo debitMemo = debitMemo(DebitMemoSettlementStatus.OPEN);

        when(repository.findById(10L)).thenReturn(Optional.of(debitMemo));
        when(repository.existsTaxDocumentNumber("TAX-001", 10L)).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(10L, null, null, "TAX-001", null, null))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.debit-memo.tax-document-number-duplicate");
        verify(repository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void updateMetadata_cancelledDebitMemo_should_fail_before_save() {
        UpdateDebitMemoMetadataUseCaseImpl useCase = new UpdateDebitMemoMetadataUseCaseImpl(repository);
        DebitMemo debitMemo = debitMemo(DebitMemoSettlementStatus.CANCELLED);

        when(repository.findById(10L)).thenReturn(Optional.of(debitMemo));

        assertThatThrownBy(() -> useCase.execute(10L, "SUP-DM-001", null, null, null, null))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.debit-memo.metadata.cancelled");
        verify(repository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void cancel_should_mark_open_debit_memo_cancelled_and_save() {
        CancelDebitMemoUseCaseImpl useCase = new CancelDebitMemoUseCaseImpl(repository, allocationConsumptionPort);
        DebitMemo debitMemo = debitMemo(DebitMemoSettlementStatus.OPEN);

        when(repository.findById(10L)).thenReturn(Optional.of(debitMemo));
        when(allocationConsumptionPort.hasConfirmedConsumption(10L)).thenReturn(false);
        when(repository.save(debitMemo)).thenReturn(debitMemo);

        DebitMemo result = useCase.execute(10L);

        assertThat(result.getSettlementStatus()).isEqualTo(DebitMemoSettlementStatus.CANCELLED);
        verify(repository).save(debitMemo);
    }

    @Test
    void cancel_withConfirmedConsumption_should_fail_before_domain_cancel() {
        CancelDebitMemoUseCaseImpl useCase = new CancelDebitMemoUseCaseImpl(repository, allocationConsumptionPort);
        DebitMemo debitMemo = debitMemo(DebitMemoSettlementStatus.OPEN);

        when(repository.findById(10L)).thenReturn(Optional.of(debitMemo));
        when(allocationConsumptionPort.hasConfirmedConsumption(10L)).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(10L))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.debit-memo.cancel.has-consumption");
        assertThat(debitMemo.getSettlementStatus()).isEqualTo(DebitMemoSettlementStatus.OPEN);
        verify(repository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void cancel_nonOpenDebitMemo_should_fail_before_save() {
        CancelDebitMemoUseCaseImpl useCase = new CancelDebitMemoUseCaseImpl(repository, allocationConsumptionPort);
        DebitMemo debitMemo = debitMemo(DebitMemoSettlementStatus.PARTIALLY_SETTLED);

        when(repository.findById(10L)).thenReturn(Optional.of(debitMemo));
        when(allocationConsumptionPort.hasConfirmedConsumption(10L)).thenReturn(false);

        assertThatThrownBy(() -> useCase.execute(10L))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.debit-memo.cancel.only-open");
        verify(repository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    private DebitMemo debitMemo(DebitMemoSettlementStatus status) {
        return DebitMemo.reconstitute(
                new AuditMetadata(10L, 1L, LocalDateTime.now(), 1L, LocalDateTime.now(), 1L),
                "DM-202606-00001",
                100L,
                "PRT-202606-00001",
                22L,
                1L,
                LocalDate.of(2026, 6, 2),
                status,
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

