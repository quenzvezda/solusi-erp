package com.solusi.erp.accountspayable.vendorbill.application.usecase.command;

import com.solusi.erp.accountspayable.vendorbill.domain.model.VendorBill;
import com.solusi.erp.accountspayable.vendorbill.domain.model.VendorBillStatus;
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
                VendorBillStatus.CONFIRMED,
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

    private static final class InMemoryVendorBillRepository implements VendorBillRepository {
        private VendorBill existing;

        @Override
        public Page<VendorBill> findAll(String keyword, Long vendorId, VendorBillStatus status, Pageable pageable) {
            throw new UnsupportedOperationException();
        }

        @Override
        public VendorBill save(VendorBill bill) {
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
