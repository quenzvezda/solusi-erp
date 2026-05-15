package com.solusi.erp.accountspayable.vendorpayment.application.usecase.command;

import com.solusi.erp.accountspayable.vendorpayment.domain.model.VendorPayment;
import com.solusi.erp.accountspayable.vendorpayment.domain.model.VendorPaymentLine;
import com.solusi.erp.accountspayable.vendorpayment.domain.model.VendorPaymentStatus;
import com.solusi.erp.accountspayable.vendorpayment.domain.repository.VendorPaymentRepository;
import com.solusi.erp.core.exception.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CancelVendorPaymentUseCase Tests")
class CancelVendorPaymentUseCaseTest {

    @Mock private VendorPaymentRepository repository;

    private CancelVendorPaymentUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new CancelVendorPaymentUseCaseImpl(repository);
    }

    @Test
    @DisplayName("execute cancels a DRAFT payment")
    void execute_cancelsDraftPayment() {
        VendorPayment payment = draftPayment();
        when(repository.findById(1L)).thenReturn(Optional.of(payment));
        when(repository.save(payment)).thenReturn(payment);

        useCase.execute(1L);

        verify(repository).save(payment);
    }

    @Test
    @DisplayName("execute fails when payment is CONFIRMED")
    void execute_failsWhenConfirmed() {
        VendorPayment payment = confirmedPayment();
        when(repository.findById(1L)).thenReturn(Optional.of(payment));

        assertThatThrownBy(() -> useCase.execute(1L))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.err.vp.cancel.only.draft");
    }

    @Test
    @DisplayName("execute fails when payment not found")
    void execute_failsWhenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(99L))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.err.vp.not.found");
    }

    private VendorPayment draftPayment() {
        return VendorPayment.createNew("VP-001", 1L, 1L, 1L,
                LocalDate.now(), BigDecimal.ONE, new BigDecimal("500.00"),
                null, null, List.of(new VendorPaymentLine(null, 1L, "VB-001",
                        new BigDecimal("500.00"), new BigDecimal("500.00"))));
    }

    private VendorPayment confirmedPayment() {
        VendorPayment payment = draftPayment();
        payment.confirm();
        return payment;
    }
}
