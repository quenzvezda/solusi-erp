package com.solusi.erp.purchasing.purchaseorder.application.usecase.command;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.purchasing.purchaserequisition.domain.model.PurchaseRequisition;
import com.solusi.erp.purchasing.purchaserequisition.domain.model.PurchaseRequisitionLine;
import com.solusi.erp.purchasing.purchaserequisition.domain.model.PurchaseRequisitionPriority;
import com.solusi.erp.purchasing.purchaserequisition.domain.model.PurchaseRequisitionStatus;
import com.solusi.erp.purchasing.purchaserequisition.domain.repository.PurchaseRequisitionRepository;
import com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrder;
import com.solusi.erp.purchasing.purchaseorder.domain.repository.PurchaseOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("StandardPurchaseOrderValidator Tests")
class StandardPurchaseOrderValidatorTest {

    private InMemoryPurchaseRequisitionRepository purchaseRequisitionRepository;
    private InMemoryPurchaseOrderRepository purchaseOrderRepository;
    private StandardPurchaseOrderValidator validator;

    @BeforeEach
    void setUp() {
        purchaseRequisitionRepository = new InMemoryPurchaseRequisitionRepository();
        purchaseOrderRepository = new InMemoryPurchaseOrderRepository();
        validator = new StandardPurchaseOrderValidator(purchaseRequisitionRepository, purchaseOrderRepository);
    }

    @Test
    void validate_throwsWhenPurchaseRequisitionNotFound() {
        assertThatThrownBy(() -> validator.validate(99L, 10L, 20L, 1L, null))
                .isInstanceOf(DomainException.class)
                .hasMessage("msg.error.po.standard.pr.not.found");
    }

    @Test
    void validate_throwsWhenPurchaseRequisitionIsNotApproved() {
        purchaseRequisitionRepository.existing = requisition(PurchaseRequisitionStatus.DRAFT);

        assertThatThrownBy(() -> validator.validate(1L, 10L, 20L, 1L, null))
                .isInstanceOf(DomainException.class)
                .hasMessage("msg.error.po.standard.pr.not.approved");
    }

    @Test
    void validate_throwsWhenHeaderDoesNotMatch() {
        purchaseRequisitionRepository.existing = requisition(PurchaseRequisitionStatus.APPROVED);

        assertThatThrownBy(() -> validator.validate(1L, 999L, 20L, 1L, null))
                .isInstanceOf(DomainException.class)
                .hasMessage("msg.error.po.standard.header.mismatch");
    }

    @Test
    void validate_returnsWhenLinesAreNullOrEmpty() {
        purchaseRequisitionRepository.existing = requisition(PurchaseRequisitionStatus.APPROVED);

        assertThatCode(() -> validator.validate(1L, 10L, 20L, 1L, null)).doesNotThrowAnyException();
        assertThatCode(() -> validator.validate(1L, 10L, 20L, 1L, List.of())).doesNotThrowAnyException();
        assertThat(purchaseOrderRepository.requestedPrLineIds).isNull();
    }

    @Test
    void validate_throwsWhenLineHasNoPrLineId() {
        purchaseRequisitionRepository.existing = requisition(PurchaseRequisitionStatus.APPROVED);

        assertThatThrownBy(() -> validator.validate(1L, 10L, 20L, 1L,
                List.of(input(null, 100L, 1L, "1.0000"))))
                .isInstanceOf(DomainException.class)
                .hasMessage("msg.error.po.standard.line.prLine.required");
    }

    @Test
    void validate_throwsWhenLineReferenceDoesNotExist() {
        purchaseRequisitionRepository.existing = requisition(PurchaseRequisitionStatus.APPROVED);

        assertThatThrownBy(() -> validator.validate(1L, 10L, 20L, 1L,
                List.of(input(999L, 100L, 1L, "1.0000"))))
                .isInstanceOf(DomainException.class)
                .hasMessage("msg.error.po.standard.line.invalid.reference");
    }

    @Test
    void validate_throwsWhenLineProductDoesNotMatch() {
        purchaseRequisitionRepository.existing = requisition(PurchaseRequisitionStatus.APPROVED);

        assertThatThrownBy(() -> validator.validate(1L, 10L, 20L, 1L,
                List.of(input(101L, 999L, 1L, "1.0000"))))
                .isInstanceOf(DomainException.class)
                .hasMessage("msg.error.po.standard.line.invalid.reference");
    }

    @Test
    void validate_throwsWhenLineUomDoesNotMatch() {
        purchaseRequisitionRepository.existing = requisition(PurchaseRequisitionStatus.APPROVED);

        assertThatThrownBy(() -> validator.validate(1L, 10L, 20L, 1L,
                List.of(input(101L, 100L, 999L, "1.0000"))))
                .isInstanceOf(DomainException.class)
                .hasMessage("msg.error.po.standard.line.invalid.reference");
    }

    @Test
    void validate_throwsWhenDuplicatePrLineRequested() {
        purchaseRequisitionRepository.existing = requisition(PurchaseRequisitionStatus.APPROVED);

        assertThatThrownBy(() -> validator.validate(1L, 10L, 20L, 1L,
                List.of(input(101L, 100L, 1L, "1.0000"), input(101L, 100L, 1L, "2.0000"))))
                .isInstanceOf(DomainException.class)
                .hasMessage("msg.error.po.standard.line.duplicate");
    }

    @Test
    void validate_throwsWhenRequestedQuantityExceedsRemaining() {
        purchaseRequisitionRepository.existing = requisition(PurchaseRequisitionStatus.APPROVED);
        purchaseOrderRepository.committedQuantityByPrLineId = Map.of(101L, new BigDecimal("9.0000"));

        assertThatThrownBy(() -> validator.validate(1L, 10L, 20L, 1L,
                List.of(input(101L, 100L, 1L, "2.0000"))))
                .isInstanceOf(DomainException.class)
                .hasMessage("msg.error.po.standard.line.quantity.exceeds.remaining");
    }

    @Test
    void validate_acceptsValidLinesWithinRemainingQuantity() {
        purchaseRequisitionRepository.existing = requisition(PurchaseRequisitionStatus.APPROVED);
        purchaseOrderRepository.committedQuantityByPrLineId = Map.of(101L, new BigDecimal("4.0000"));

        assertThatCode(() -> validator.validate(1L, 10L, 20L, 1L,
                List.of(input(101L, 100L, 1L, "6.0000"))))
                .doesNotThrowAnyException();
        assertThat(purchaseOrderRepository.requestedPrLineIds).containsExactly(101L);
    }

    private PurchaseRequisition requisition(PurchaseRequisitionStatus status) {
        return new PurchaseRequisition(
                new AuditMetadata(1L, 1L, null, null, null, null),
                "PR-001",
                LocalDate.of(2026, 5, 1),
                500L,
                20L,
                "Procurement",
                PurchaseRequisitionPriority.NORMAL,
                status,
                null,
                true,
                10L,
                1L,
                List.of(new PurchaseRequisitionLine(
                        new AuditMetadata(101L, 1L, null, null, null, null),
                        1L,
                        100L,
                        new BigDecimal("10.0000"),
                        1L,
                        LocalDate.of(2026, 5, 15),
                        new BigDecimal("100.0000"),
                        null,
                        null
                ))
        );
    }

    private PoLineInput input(Long prLineId, Long productId, Long uomId, String quantity) {
        return new PoLineInput(
                productId,
                new BigDecimal(quantity),
                uomId,
                new BigDecimal("100.0000"),
                BigDecimal.ZERO,
                prLineId,
                null
        );
    }

    private static final class InMemoryPurchaseRequisitionRepository implements PurchaseRequisitionRepository {
        private PurchaseRequisition existing;

        @Override
        public PurchaseRequisition save(PurchaseRequisition purchaseRequisition) {
            return purchaseRequisition;
        }

        @Override
        public Optional<PurchaseRequisition> findById(Long id) {
            return Optional.ofNullable(existing);
        }

        @Override
        public Page<PurchaseRequisition> findAll(String keyword, Pageable pageable) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void deleteById(Long id) {
        }
    }

    private static final class InMemoryPurchaseOrderRepository implements PurchaseOrderRepository {
        private Map<Long, BigDecimal> committedQuantityByPrLineId = Map.of();
        private Set<Long> requestedPrLineIds;

        @Override
        public PurchaseOrder save(PurchaseOrder purchaseOrder) {
            return purchaseOrder;
        }

        @Override
        public Optional<PurchaseOrder> findById(Long id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Page<PurchaseOrder> findAll(String keyword, Pageable pageable) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Map<Long, BigDecimal> sumCommittedQuantityByPrLineIds(Set<Long> prLineIds) {
            requestedPrLineIds = prLineIds;
            return committedQuantityByPrLineId;
        }

        @Override
        public void deleteById(Long id) {
        }
    }
}
