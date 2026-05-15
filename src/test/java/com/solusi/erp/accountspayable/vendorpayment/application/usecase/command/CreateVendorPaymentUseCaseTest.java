package com.solusi.erp.accountspayable.vendorpayment.application.usecase.command;

import com.solusi.erp.accountspayable.vendorpayment.domain.model.VendorPayment;
import com.solusi.erp.accountspayable.vendorpayment.domain.model.VendorPaymentLine;
import com.solusi.erp.accountspayable.vendorpayment.domain.repository.VendorPaymentRepository;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.core.infrastructure.sequence.SequenceGeneratorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateVendorPaymentUseCase Tests")
class CreateVendorPaymentUseCaseTest {

    @Mock private VendorPaymentRepository repository;
    @Mock private SequenceGeneratorService sequenceGeneratorService;

    private CreateVendorPaymentUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateVendorPaymentUseCaseImpl(repository, sequenceGeneratorService);
    }

    @Test
    @DisplayName("execute creates payment with generated code and saves")
    void execute_createsPaymentAndSaves() {
        when(sequenceGeneratorService.generate("VENDOR-PAYMENT")).thenReturn("VP-202605-00001");
        when(repository.save(any(VendorPayment.class))).thenAnswer(inv -> inv.getArgument(0));

        VendorPaymentLine line = new VendorPaymentLine(null, 1L, "VB-001",
                new BigDecimal("500.00"), new BigDecimal("500.00"));

        VendorPayment result = useCase.execute(1L, 1L, 1L, LocalDate.of(2026, 5, 15),
                BigDecimal.ONE, new BigDecimal("500.00"), "REF", "notes", List.of(line));

        assertThat(result.getCode()).isEqualTo("VP-202605-00001");
        assertThat(result.getPaymentAmount()).isEqualByComparingTo("500.00");
        assertThat(result.getLines()).hasSize(1);
    }

    @Test
    @DisplayName("execute fails when no lines provided")
    void execute_failsWhenNoLines() {
        when(sequenceGeneratorService.generate("VENDOR-PAYMENT")).thenReturn("VP-202605-00001");

        assertThatThrownBy(() -> useCase.execute(1L, 1L, 1L, LocalDate.now(),
                BigDecimal.ONE, new BigDecimal("500.00"), null, null, List.of()))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.err.vp.lines.required");
    }

    @Test
    @DisplayName("execute fails when amount is zero")
    void execute_failsWhenAmountZero() {
        when(sequenceGeneratorService.generate("VENDOR-PAYMENT")).thenReturn("VP-202605-00001");

        VendorPaymentLine line = new VendorPaymentLine(null, 1L, "VB-001",
                new BigDecimal("500.00"), new BigDecimal("0"));

        assertThatThrownBy(() -> useCase.execute(1L, 1L, 1L, LocalDate.now(),
                BigDecimal.ONE, BigDecimal.ZERO, null, null, List.of(line)))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.err.vp.amount.positive");
    }
}
