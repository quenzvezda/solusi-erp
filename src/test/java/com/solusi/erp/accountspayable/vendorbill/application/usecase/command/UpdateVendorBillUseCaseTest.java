package com.solusi.erp.accountspayable.vendorbill.application.usecase.command;

import com.solusi.erp.accountspayable.vendorbill.domain.model.VendorBill;
import com.solusi.erp.accountspayable.vendorbill.domain.model.VendorBillDocumentStatus;
import com.solusi.erp.accountspayable.vendorbill.domain.repository.VendorBillRepository;
import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.core.exception.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("UpdateVendorBillUseCase Tests")
class UpdateVendorBillUseCaseTest {

    private InMemoryVendorBillRepository vendorBillRepository;
    private UpdateVendorBillUseCase useCase;

    @BeforeEach
    void setUp() {
        vendorBillRepository = new InMemoryVendorBillRepository();
        useCase = new UpdateVendorBillUseCaseImpl(vendorBillRepository);
    }

    @Test
    void update_should_save_updated_draft_bill_with_totals_and_gr_refs() {
        vendorBillRepository.existing = draftBill();

        VendorBill updated = useCase.execute(
                1L,
                20L,
                "INV-002",
                LocalDate.of(2026, 5, 11),
                LocalDate.of(2026, 5, 21),
                2L,
                new BigDecimal("16000"),
                "updated",
                List.of(7L, 8L),
                List.of(
                        line(101L, new BigDecimal("100.0000"), new BigDecimal("11.0000")),
                        line(102L, new BigDecimal("200.0000"), new BigDecimal("22.0000"))
                )
        );

        assertThat(updated.getMetadata()).isEqualTo(vendorBillRepository.existing.getMetadata());
        assertThat(updated.getCode()).isEqualTo("VB-202605-00001");
        assertThat(updated.getVendorId()).isEqualTo(20L);
        assertThat(updated.getSubtotal()).isEqualByComparingTo("300.0000");
        assertThat(updated.getTaxAmount()).isEqualByComparingTo("33.0000");
        assertThat(updated.getTotalAmount()).isEqualByComparingTo("333.0000");
        assertThat(updated.getGrRefs()).hasSize(2);
        assertThat(updated.getLines()).hasSize(2);
        assertThat(vendorBillRepository.saved).isSameAs(updated);
    }

    @Test
    void update_should_default_gr_refs_to_empty_when_gr_ids_null() {
        vendorBillRepository.existing = draftBill();

        VendorBill updated = useCase.execute(
                1L,
                20L,
                "INV-002",
                LocalDate.of(2026, 5, 11),
                null,
                2L,
                BigDecimal.ONE,
                "updated",
                null,
                List.of(line(101L, null, BigDecimal.ZERO))
        );

        assertThat(updated.getGrRefs()).isEmpty();
        assertThat(updated.getSubtotal()).isZero();
        assertThat(updated.getTotalAmount()).isZero();
    }

    @Test
    void update_should_reject_non_draft_bill() {
        vendorBillRepository.existing = new VendorBill(
                new AuditMetadata(1L, 1L, null, null, null, null),
                "VB-202605-00001",
                10L,
                "INV-001",
                LocalDate.of(2026, 5, 10),
                LocalDate.of(2026, 5, 20),
                1L,
                BigDecimal.ONE,
                VendorBillDocumentStatus.CONFIRMED,
                null,
                new BigDecimal("100.0000"),
                new BigDecimal("11.0000"),
                new BigDecimal("111.0000"),
                "notes",
                List.of(),
                List.of()
        );

        assertThatThrownBy(() -> useCase.execute(
                1L,
                10L,
                "INV-002",
                LocalDate.of(2026, 5, 10),
                LocalDate.of(2026, 5, 20),
                1L,
                BigDecimal.ONE,
                "updated",
                List.of(),
                List.of()
        )).isInstanceOf(DomainException.class)
                .hasMessage("msg.error.vb.only.draft.editable");
    }

    @Test
    void update_should_reject_missing_bill() {
        assertThatThrownBy(() -> useCase.execute(
                99L,
                10L,
                "INV-002",
                LocalDate.of(2026, 5, 10),
                LocalDate.of(2026, 5, 20),
                1L,
                BigDecimal.ONE,
                "updated",
                List.of(),
                List.of(line(101L, BigDecimal.TEN, BigDecimal.ONE))
        )).isInstanceOf(DomainException.class)
                .hasMessage("msg.error.vb.not.found");
    }

    @Test
    void update_should_reject_due_date_before_bill_date() {
        vendorBillRepository.existing = draftBill();

        assertThatThrownBy(() -> useCase.execute(
                1L,
                10L,
                "INV-002",
                LocalDate.of(2026, 5, 20),
                LocalDate.of(2026, 5, 19),
                1L,
                BigDecimal.ONE,
                "updated",
                List.of(),
                List.of(line(101L, BigDecimal.TEN, BigDecimal.ONE))
        )).isInstanceOf(DomainException.class)
                .hasMessage("msg.error.vb.due.before.bill");
    }

    @Test
    void update_should_reject_null_lines() {
        vendorBillRepository.existing = draftBill();

        assertThatThrownBy(() -> useCase.execute(
                1L,
                10L,
                "INV-002",
                LocalDate.of(2026, 5, 10),
                LocalDate.of(2026, 5, 20),
                1L,
                BigDecimal.ONE,
                "updated",
                List.of(),
                null
        )).isInstanceOf(DomainException.class)
                .hasMessage("msg.err.vb.lines.required");
    }

    @Test
    void update_should_reject_empty_lines() {
        vendorBillRepository.existing = draftBill();

        assertThatThrownBy(() -> useCase.execute(
                1L,
                10L,
                "INV-002",
                LocalDate.of(2026, 5, 10),
                LocalDate.of(2026, 5, 20),
                1L,
                BigDecimal.ONE,
                "updated",
                List.of(),
                List.of()
        )).isInstanceOf(DomainException.class)
                .hasMessage("msg.err.vb.lines.required");
    }

    private VendorBill draftBill() {
        return new VendorBill(
                new AuditMetadata(1L, 1L, null, null, null, null),
                "VB-202605-00001",
                10L,
                "INV-001",
                LocalDate.of(2026, 5, 10),
                LocalDate.of(2026, 5, 20),
                1L,
                BigDecimal.ONE,
                VendorBillDocumentStatus.DRAFT,
                null,
                new BigDecimal("100.0000"),
                new BigDecimal("11.0000"),
                new BigDecimal("111.0000"),
                "notes",
                List.of(),
                List.of()
        );
    }

    private VendorBillLineCommand line(Long grLineId, BigDecimal inventoryAmount, BigDecimal taxAmount) {
        return new VendorBillLineCommand(
                null,
                grLineId,
                201L,
                "Product",
                "Line",
                BigDecimal.ONE,
                1L,
                "PCS",
                new BigDecimal("100.0000"),
                inventoryAmount,
                taxAmount
        );
    }

    private static final class InMemoryVendorBillRepository implements VendorBillRepository {
        private VendorBill existing;
        private VendorBill saved;

        @Override
        public Page<VendorBill> findAll(String keyword, Long vendorId, VendorBillDocumentStatus status, com.solusi.erp.accountspayable.vendorbill.domain.model.VendorBillSettlementStatus settlementStatus, Pageable pageable) {
            throw new UnsupportedOperationException();
        }

        @Override
        public VendorBill save(VendorBill bill) {
            this.saved = bill;
            return bill;
        }

        @Override
        public Optional<VendorBill> findById(Long id) {
            return Optional.ofNullable(existing);
        }

        @Override
        public void deleteById(Long id) {
        }
    }
}
