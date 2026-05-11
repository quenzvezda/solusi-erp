package com.solusi.erp.accountspayable.vendorbill.application.usecase.query;

import com.solusi.erp.accountspayable.vendorbill.domain.model.VendorBill;
import com.solusi.erp.accountspayable.vendorbill.domain.model.VendorBillGrRef;
import com.solusi.erp.accountspayable.vendorbill.domain.model.VendorBillLine;
import com.solusi.erp.accountspayable.vendorbill.domain.model.VendorBillStatus;
import com.solusi.erp.accountspayable.vendorbill.domain.repository.VendorBillRepository;
import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.core.exception.DomainException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GetVendorBillDetailUseCaseTest {

    @Test
    void execute_should_map_header_refs_and_lines() {
        InMemoryVendorBillRepository repository = new InMemoryVendorBillRepository();
        repository.existing = bill();

        VendorBillDetailView result = new GetVendorBillDetailUseCaseImpl(repository).execute(1L);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.code()).isEqualTo("VB-202605-00001");
        assertThat(result.grIds()).containsExactly(88L);
        assertThat(result.lines()).hasSize(1);
        assertThat(result.lines().getFirst().grLineId()).isEqualTo(1001L);
        assertThat(result.lines().getFirst().lineTotal()).isEqualByComparingTo("100.0000");
    }

    @Test
    void execute_should_fail_when_vendor_bill_not_found() {
        InMemoryVendorBillRepository repository = new InMemoryVendorBillRepository();

        assertThatThrownBy(() -> new GetVendorBillDetailUseCaseImpl(repository).execute(99L))
                .isInstanceOf(DomainException.class)
                .hasMessage("msg.err.vb.notfound");
    }

    private static VendorBill bill() {
        return new VendorBill(
                new AuditMetadata(1L, 1L, null, null, null, null),
                "VB-202605-00001",
                10L,
                "INV-001",
                LocalDate.of(2026, 5, 10),
                LocalDate.of(2026, 5, 20),
                1L,
                VendorBillStatus.CONFIRMED,
                new BigDecimal("100.0000"),
                BigDecimal.ZERO,
                new BigDecimal("100.0000"),
                "notes",
                List.of(new VendorBillGrRef(1L, 88L)),
                List.of(new VendorBillLine(
                        11L,
                        1001L,
                        2001L,
                        "Product A",
                        "Line A",
                        new BigDecimal("10.0000"),
                        1L,
                        "PCS",
                        new BigDecimal("10.0000"),
                        new BigDecimal("90.0000"),
                        new BigDecimal("10.0000"),
                        new BigDecimal("100.0000")
                ))
        );
    }

    private static final class InMemoryVendorBillRepository implements VendorBillRepository {
        private VendorBill existing;

        @Override
        public Page<VendorBill> findAll(String keyword, Long vendorId, VendorBillStatus status, Pageable pageable) {
            throw new UnsupportedOperationException();
        }

        @Override
        public VendorBill save(VendorBill bill) {
            throw new UnsupportedOperationException();
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
