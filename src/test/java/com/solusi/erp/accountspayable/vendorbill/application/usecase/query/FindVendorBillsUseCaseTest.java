package com.solusi.erp.accountspayable.vendorbill.application.usecase.query;

import com.solusi.erp.accountspayable.vendorbill.domain.model.VendorBill;
import com.solusi.erp.accountspayable.vendorbill.domain.model.VendorBillStatus;
import com.solusi.erp.accountspayable.vendorbill.domain.port.VendorBillPaymentSummaryPort;
import com.solusi.erp.accountspayable.vendorbill.domain.repository.VendorBillRepository;
import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class FindVendorBillsUseCaseTest {

    @Test
    void execute_should_map_vendor_bills_to_summary_page() {
        InMemoryVendorBillRepository repository = new InMemoryVendorBillRepository();
        Pageable pageable = Pageable.of(1, 10, "billDate", "desc");
        repository.page = new Page<>(List.of(bill()), 1, 10, 1);
        InMemoryPaymentSummaryPort paymentSummaryPort = new InMemoryPaymentSummaryPort(Map.of(
                1L, new VendorBillPaymentSummaryPort.PaymentSummary(1L, new BigDecimal("50.0000"), new BigDecimal("50.0000"))
        ));

        Page<VendorBillSummaryView> result = new FindVendorBillsUseCaseImpl(repository, paymentSummaryPort)
                .execute("INV", 10L, VendorBillStatus.CONFIRMED, pageable);

        assertThat(repository.keyword).isEqualTo("INV");
        assertThat(repository.vendorId).isEqualTo(10L);
        assertThat(repository.status).isEqualTo(VendorBillStatus.CONFIRMED);
        assertThat(repository.pageable).isEqualTo(pageable);
        assertThat(result.content()).hasSize(1);
        assertThat(result.content().getFirst().code()).isEqualTo("VB-202605-00001");
        assertThat(result.content().getFirst().totalAmount()).isEqualByComparingTo("100.0000");
        assertThat(result.content().getFirst().paidAmount()).isEqualByComparingTo("50.0000");
        assertThat(result.content().getFirst().outstandingAmount()).isEqualByComparingTo("50.0000");
        assertThat(paymentSummaryPort.requestedIds).containsExactly(1L);
        assertThat(result.totalElements()).isEqualTo(1);
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
                BigDecimal.ONE,
                VendorBillStatus.CONFIRMED,
                new BigDecimal("100.0000"),
                BigDecimal.ZERO,
                new BigDecimal("100.0000"),
                "notes",
                List.of(),
                List.of()
        );
    }

    private record InMemoryPaymentSummaryPort(Map<Long, VendorBillPaymentSummaryPort.PaymentSummary> summaries) implements VendorBillPaymentSummaryPort {
        private static List<Long> requestedIds;

        @Override
        public PaymentSummary getPaymentSummary(Long vendorBillId) {
            return summaries.get(vendorBillId);
        }

        @Override
        public Map<Long, PaymentSummary> getPaymentSummaries(List<Long> vendorBillIds) {
            requestedIds = vendorBillIds;
            return summaries;
        }
    }

    private static final class InMemoryVendorBillRepository implements VendorBillRepository {
        private Page<VendorBill> page;
        private String keyword;
        private Long vendorId;
        private VendorBillStatus status;
        private Pageable pageable;

        @Override
        public Page<VendorBill> findAll(String keyword, Long vendorId, VendorBillStatus status, Pageable pageable) {
            this.keyword = keyword;
            this.vendorId = vendorId;
            this.status = status;
            this.pageable = pageable;
            return page;
        }

        @Override
        public VendorBill save(VendorBill bill) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<VendorBill> findById(Long id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void deleteById(Long id) {
        }
    }
}
