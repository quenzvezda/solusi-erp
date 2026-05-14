package com.solusi.erp.accountspayable.vendorbill.application.usecase.command;

import com.solusi.erp.accountspayable.vendorbill.domain.model.VendorBill;
import com.solusi.erp.accountspayable.vendorbill.domain.model.VendorBillStatus;
import com.solusi.erp.accountspayable.vendorbill.domain.repository.VendorBillRepository;
import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.core.infrastructure.sequence.SequenceGeneratorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("CreateVendorBillUseCase Tests")
class CreateVendorBillUseCaseTest {

    private InMemoryVendorBillRepository vendorBillRepository;
    private SequenceGeneratorService sequenceGeneratorService;
    private CreateVendorBillUseCase useCase;

    @BeforeEach
    void setUp() {
        vendorBillRepository = new InMemoryVendorBillRepository();
        sequenceGeneratorService = new FixedSequenceGeneratorService("VB-202605-00001");
        useCase = new CreateVendorBillUseCaseImpl(vendorBillRepository, sequenceGeneratorService);
    }

    @Test
    void create_should_reject_due_date_before_bill_date() {
        assertThatThrownBy(() -> useCase.execute(
                10L,
                "INV-001",
                LocalDate.of(2026, 5, 10),
                LocalDate.of(2026, 5, 9),
                1L,
                BigDecimal.ONE,
                "notes",
                List.of(99L),
                List.of()
        )).isInstanceOf(DomainException.class)
                .hasMessage("msg.error.vb.due.before.bill");
    }

    @Test
    void create_should_set_status_draft_and_amounts() {
        VendorBill result = useCase.execute(
                10L,
                "INV-001",
                LocalDate.of(2026, 5, 10),
                LocalDate.of(2026, 5, 20),
                1L,
                BigDecimal.ONE,
                "notes",
                List.of(88L),
                List.of(new VendorBillLineCommand(
                        null,
                        1001L,
                        2001L,
                        "Product A",
                        "Line A",
                        new BigDecimal("2.0000"),
                        1L,
                        "PCS",
                        new BigDecimal("10.0000"),
                        new BigDecimal("20.0000"),
                        new BigDecimal("2.2000")
                ))
        );

        assertThat(result.getCode()).isEqualTo("VB-202605-00001");
        assertThat(result.getStatus()).isEqualTo(VendorBillStatus.DRAFT);
        assertThat(result.getSubtotal()).isEqualByComparingTo("20.0000");
        assertThat(result.getTaxAmount()).isEqualByComparingTo("2.2000");
        assertThat(result.getTotalAmount()).isEqualByComparingTo("22.2000");
        assertThat(result.getLines()).hasSize(1);
        assertThat(result.getLines().getFirst().getLineTotal()).isEqualByComparingTo("20.0000");
    }

    private static final class FixedSequenceGeneratorService extends SequenceGeneratorService {
        private final String code;

        private FixedSequenceGeneratorService(String code) {
            super(null);
            this.code = code;
        }

        @Override
        public String generate(String moduleCode) {
            return code;
        }
    }

    private static final class InMemoryVendorBillRepository implements VendorBillRepository {
        private VendorBill saved;

        @Override
        public Page<VendorBill> findAll(String keyword, Long vendorId, VendorBillStatus status, Pageable pageable) {
            throw new UnsupportedOperationException();
        }

        @Override
        public VendorBill save(VendorBill bill) {
            this.saved = bill;
            return bill;
        }

        @Override
        public Optional<VendorBill> findById(Long id) {
            return Optional.ofNullable(saved);
        }

        @Override
        public void deleteById(Long id) {
        }
    }
}
