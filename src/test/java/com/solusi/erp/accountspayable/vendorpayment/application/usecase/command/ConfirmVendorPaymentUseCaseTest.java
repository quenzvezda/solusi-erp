package com.solusi.erp.accountspayable.vendorpayment.application.usecase.command;

import com.solusi.erp.accounting.journal.application.usecase.command.JournalPostingCommand;
import com.solusi.erp.accounting.journal.application.usecase.command.PostJournalForEventUseCase;
import com.solusi.erp.accounting.journal.domain.model.JournalVariable;
import com.solusi.erp.accountspayable.vendorpayment.domain.model.VendorPayment;
import com.solusi.erp.accountspayable.vendorpayment.domain.model.VendorPaymentLine;
import com.solusi.erp.accountspayable.vendorpayment.domain.model.VendorPaymentStatus;
import com.solusi.erp.accountspayable.vendorpayment.domain.port.VendorBillPaymentUpdatePort;
import com.solusi.erp.accountspayable.vendorpayment.domain.repository.VendorPaymentRepository;
import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ConfirmVendorPaymentUseCase Tests")
class ConfirmVendorPaymentUseCaseTest {

    @Mock private VendorPaymentRepository repository;
    @Mock private PostJournalForEventUseCase postJournalForEventUseCase;
    @Mock private VendorBillPaymentUpdatePort vendorBillPaymentUpdatePort;

    private ConfirmVendorPaymentUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new ConfirmVendorPaymentUseCaseImpl(repository, postJournalForEventUseCase, vendorBillPaymentUpdatePort);
    }

    @Test
    @DisplayName("execute confirms payment, posts journal, and updates VB status")
    void execute_confirmsAndPostsJournal() {
        VendorPayment payment = draftPaymentWithId(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(payment));
        when(repository.save(any(VendorPayment.class))).thenAnswer(inv -> inv.getArgument(0));

        useCase.execute(1L);

        verify(postJournalForEventUseCase).execute(any(JournalPostingCommand.class));
        verify(repository).save(payment);
        verify(vendorBillPaymentUpdatePort).updatePaymentStatus(List.of(10L));
        assertThat(payment.getStatus()).isEqualTo(VendorPaymentStatus.CONFIRMED);
    }

    @Test
    @DisplayName("execute posts journal with correct VP_AP_AMT and VP_BANK_OUT_AMT")
    void execute_postsJournalWithCorrectAmounts() {
        VendorPayment payment = draftPaymentWithId(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(payment));
        when(repository.save(any(VendorPayment.class))).thenAnswer(inv -> inv.getArgument(0));

        useCase.execute(1L);

        ArgumentCaptor<JournalPostingCommand> captor = ArgumentCaptor.forClass(JournalPostingCommand.class);
        verify(postJournalForEventUseCase).execute(captor.capture());
        JournalPostingCommand cmd = captor.getValue();

        assertThat(cmd.values().get(JournalVariable.VP_AP_AMT)).isEqualByComparingTo("500.0000");
        assertThat(cmd.values().get(JournalVariable.VP_BANK_OUT_AMT)).isEqualByComparingTo("500.0000");
        assertThat(cmd.values().get(JournalVariable.VP_FX_LOSS_AMT)).isEqualByComparingTo("0");
        assertThat(cmd.values().get(JournalVariable.VP_FX_GAIN_AMT)).isEqualByComparingTo("0");
    }

    @Test
    @DisplayName("execute with bankCoaId sets accountOverrides")
    void execute_withBankCoaId_setsAccountOverrides() {
        VendorPayment payment = draftPaymentWithId(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(payment));
        when(repository.save(any(VendorPayment.class))).thenAnswer(inv -> inv.getArgument(0));

        useCase.execute(1L, 999L);

        ArgumentCaptor<JournalPostingCommand> captor = ArgumentCaptor.forClass(JournalPostingCommand.class);
        verify(postJournalForEventUseCase).execute(captor.capture());
        JournalPostingCommand cmd = captor.getValue();

        assertThat(cmd.accountOverrides()).containsEntry(JournalVariable.VP_BANK_OUT_AMT, 999L);
    }

    @Test
    @DisplayName("execute fails when payment amount mismatch with lines")
    void execute_failsWhenAmountMismatch() {
        VendorPayment payment = mismatchPaymentWithId(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(payment));

        assertThatThrownBy(() -> useCase.execute(1L))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.err.vp.amount.mismatch");
    }

    @Test
    @DisplayName("execute fails when payment not found")
    void execute_failsWhenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(99L))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.err.vp.not.found");
    }

    private VendorPayment draftPaymentWithId(Long id) {
        AuditMetadata metadata = new AuditMetadata(id, 1L, null, null, null, null);
        return new VendorPayment(metadata, "VP-001", 1L, 1L, 1L,
                LocalDate.of(2026, 5, 15), BigDecimal.ONE, new BigDecimal("500.00"),
                VendorPaymentStatus.DRAFT, "REF", "notes",
                List.of(new VendorPaymentLine(null, 10L, "VB-001",
                        new BigDecimal("500.00"), new BigDecimal("500.00"))));
    }

    private VendorPayment mismatchPaymentWithId(Long id) {
        AuditMetadata metadata = new AuditMetadata(id, 1L, null, null, null, null);
        return new VendorPayment(metadata, "VP-001", 1L, 1L, 1L,
                LocalDate.of(2026, 5, 15), BigDecimal.ONE, new BigDecimal("500.00"),
                VendorPaymentStatus.DRAFT, "REF", "notes",
                List.of(new VendorPaymentLine(null, 10L, "VB-001",
                        new BigDecimal("500.00"), new BigDecimal("300.00"))));
    }
}
