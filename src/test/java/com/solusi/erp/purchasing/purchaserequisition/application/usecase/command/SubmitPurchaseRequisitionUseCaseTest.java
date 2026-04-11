package com.solusi.erp.purchasing.purchaserequisition.application.usecase.command;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.purchasing.purchaserequisition.domain.model.PurchaseRequisition;
import com.solusi.erp.purchasing.purchaserequisition.domain.model.PurchaseRequisitionLine;
import com.solusi.erp.purchasing.purchaserequisition.domain.model.PurchaseRequisitionPriority;
import com.solusi.erp.purchasing.purchaserequisition.domain.model.PurchaseRequisitionStatus;
import com.solusi.erp.purchasing.purchaserequisition.domain.port.PurchaseRequisitionEventPublisher;
import com.solusi.erp.purchasing.purchaserequisition.domain.repository.PurchaseRequisitionRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SubmitPurchaseRequisitionUseCase Tests")
class SubmitPurchaseRequisitionUseCaseTest {

    @Mock
    private PurchaseRequisitionRepository repository;

    @Mock
    private PurchaseRequisitionEventPublisher eventPublisher;

    private SubmitPurchaseRequisitionUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new SubmitPurchaseRequisitionUseCaseImpl(repository, eventPublisher);
    }

    @Test
    @DisplayName("execute submits draft purchase requisition with lines and publishes event")
    void execute_submitsDraftWithLines() {
        PurchaseRequisitionLine line = new PurchaseRequisitionLine(
                AuditMetadata.empty(), null, 10L, new BigDecimal("5.0000"),
                1L, LocalDate.of(2026, 7, 15), new BigDecimal("100.0000"),
                20L, null, "line note"
        );
        PurchaseRequisition existing = new PurchaseRequisition(
                new AuditMetadata(1L, 1L, null, null, null, null),
                "PR-2607-00001", LocalDate.of(2026, 7, 1), 10L, 2L,
                "IT", PurchaseRequisitionPriority.NORMAL,
                PurchaseRequisitionStatus.DRAFT, "note", true, List.of(line)
        );

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(PurchaseRequisition.class))).thenAnswer(inv -> inv.getArgument(0));

        PurchaseRequisition result = useCase.execute(1L, 50L);

        assertThat(result.getStatus()).isEqualTo(PurchaseRequisitionStatus.SUBMITTED);
        verify(repository).save(any(PurchaseRequisition.class));
        verify(eventPublisher).publishApprovalRequested(eq(1L), eq("PR-2607-00001"), eq(10L), eq(50L));
    }

    @Test
    @DisplayName("execute throws DomainException when purchase requisition not found")
    void execute_throwsWhenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(99L, 50L))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.pr.notfound");
    }

    @Test
    @DisplayName("execute throws DomainException when purchase requisition has no lines")
    void execute_throwsWhenNoLines() {
        PurchaseRequisition existing = new PurchaseRequisition(
                new AuditMetadata(2L, 1L, null, null, null, null),
                "PR-2607-00002", LocalDate.of(2026, 7, 1), 10L, 2L,
                "IT", PurchaseRequisitionPriority.NORMAL,
                PurchaseRequisitionStatus.DRAFT, "note", true, List.of()
        );

        when(repository.findById(2L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> useCase.execute(2L, 50L))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.pr.submit.no.lines");
    }
}
