package com.solusi.erp.accountspayable.vendorbill.application.usecase.command;

import com.solusi.erp.accounting.journal.application.usecase.command.JournalPostingCommand;
import com.solusi.erp.accounting.journal.application.usecase.command.PostJournalForEventUseCase;
import com.solusi.erp.accounting.journal.domain.model.JournalVariable;
import com.solusi.erp.accounting.period.application.usecase.query.EnsureOpenPeriodForDateUseCase;
import com.solusi.erp.accounting.schema.domain.model.SchemaEventType;
import com.solusi.erp.accountspayable.vendorbill.domain.model.VendorBill;
import com.solusi.erp.accountspayable.vendorbill.domain.model.VendorBillGrRef;
import com.solusi.erp.accountspayable.vendorbill.domain.model.VendorBillLine;
import com.solusi.erp.accountspayable.vendorbill.domain.model.VendorBillStatus;
import com.solusi.erp.accountspayable.vendorbill.domain.port.BillableGrQueryPort;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ConfirmVendorBillUseCase Tests")
class ConfirmVendorBillUseCaseTest {

    private InMemoryVendorBillRepository vendorBillRepository;
    private FakeBillableGrQueryPort billableGrQueryPort;
    private RecordingEnsureOpenPeriodForDateUseCase ensureOpenPeriodForDateUseCase;
    private RecordingPostJournalForEventUseCase postJournalForEventUseCase;
    private ConfirmVendorBillUseCase useCase;

    @BeforeEach
    void setUp() {
        vendorBillRepository = new InMemoryVendorBillRepository();
        billableGrQueryPort = new FakeBillableGrQueryPort();
        ensureOpenPeriodForDateUseCase = new RecordingEnsureOpenPeriodForDateUseCase();
        postJournalForEventUseCase = new RecordingPostJournalForEventUseCase();
        useCase = new ConfirmVendorBillUseCaseImpl(
                vendorBillRepository,
                billableGrQueryPort,
                ensureOpenPeriodForDateUseCase,
                postJournalForEventUseCase
        );
    }

    @Test
    void confirm_should_fail_when_qty_exceeds_outstanding() {
        vendorBillRepository.existing = draftBill(List.of(line(1001L, "6.0000")));
        billableGrQueryPort.grLineData.put(1001L, grLineData("10.0000", "100.0000"));
        billableGrQueryPort.confirmedBilledQty.put(1001L, new BigDecimal("5.0000"));

        assertThatThrownBy(() -> useCase.execute(1L))
                .isInstanceOf(DomainException.class)
                .hasMessage("msg.err.vb.qty.exceed.outstanding");

        assertThat(vendorBillRepository.saved).isNull();
        assertThat(postJournalForEventUseCase.command).isNull();
    }

    @Test
    void confirm_should_fail_when_duplicate_current_lines_exceed_outstanding() {
        vendorBillRepository.existing = draftBill(List.of(
                line(1001L, "6.0000"),
                line(1001L, "6.0000")
        ));
        billableGrQueryPort.grLineData.put(1001L, grLineData("10.0000", "100.0000"));

        assertThatThrownBy(() -> useCase.execute(1L))
                .isInstanceOf(DomainException.class)
                .hasMessage("msg.err.vb.qty.exceed.outstanding");

        assertThat(vendorBillRepository.saved).isNull();
        assertThat(postJournalForEventUseCase.command).isNull();
    }

    @Test
    void confirm_should_post_journal_using_vendor_bill_schema_vars() {
        vendorBillRepository.existing = draftBill(List.of(
                line(1001L, "2.0000"),
                line(1002L, "1.0000")
        ));
        billableGrQueryPort.grLineData.put(1001L, grLineData("10.0000", "100.0000", "11.0000"));
        billableGrQueryPort.grLineData.put(1002L, grLineData("5.0000", "55.0000", "6.0500"));

        useCase.execute(1L);

        VendorBill saved = vendorBillRepository.saved;
        assertThat(saved.getStatus()).isEqualTo(VendorBillStatus.CONFIRMED);
        assertThat(saved.getSubtotal()).isEqualByComparingTo("31.0000");
        assertThat(saved.getTaxAmount()).isEqualByComparingTo("3.4100");
        assertThat(saved.getTotalAmount()).isEqualByComparingTo("34.4100");

        JournalPostingCommand command = postJournalForEventUseCase.command;
        assertThat(command.eventType()).isEqualTo(SchemaEventType.VENDOR_BILL);
        assertThat(command.sourceType()).isEqualTo("VENDOR_BILL");
        assertThat(command.sourceId()).isEqualTo(1L);
        assertThat(command.sourceCode()).isEqualTo("VB-202605-00001");
        assertThat(command.postingDate()).isEqualTo(LocalDate.of(2026, 5, 10));
        assertThat(command.values().get(JournalVariable.VB_GRIR_CLEARING_AMT)).isEqualByComparingTo("31.0000");
        assertThat(command.values().get(JournalVariable.VB_TAX_AMT)).isEqualByComparingTo("3.4100");
        assertThat(command.values().get(JournalVariable.VB_AP_TOTAL)).isEqualByComparingTo("34.4100");
        assertThat(ensureOpenPeriodForDateUseCase.date).isEqualTo(LocalDate.of(2026, 5, 10));
    }

    @Test
    void confirm_should_use_remainder_on_last_bill_line() {
        vendorBillRepository.existing = draftBill(List.of(line(1001L, "3.3333")));
        billableGrQueryPort.grLineData.put(1001L, grLineData("10.0000", "100.0000"));
        billableGrQueryPort.confirmedBilledQty.put(1001L, new BigDecimal("6.6667"));
        billableGrQueryPort.confirmedLineTotals.put(1001L, new BigDecimal("66.6667"));

        useCase.execute(1L);

        VendorBill saved = vendorBillRepository.saved;
        assertThat(saved.getLines().getFirst().getLineTotal()).isEqualByComparingTo("33.3333");
        assertThat(saved.getTotalAmount()).isEqualByComparingTo("33.3333");
    }

    private static VendorBill draftBill(List<VendorBillLine> lines) {
        return new VendorBill(
                new AuditMetadata(1L, 1L, null, null, null, null),
                "VB-202605-00001",
                10L,
                "INV-001",
                LocalDate.of(2026, 5, 10),
                LocalDate.of(2026, 5, 20),
                1L,
                BigDecimal.ONE,
                VendorBillStatus.DRAFT,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                "notes",
                List.of(new VendorBillGrRef(1L, 88L)),
                lines
        );
    }

    private static VendorBillLine line(Long grLineId, String qtyBilled) {
        return new VendorBillLine(
                null,
                grLineId,
                2001L,
                "Product A",
                "Line A",
                new BigDecimal(qtyBilled),
                1L,
                "PCS",
                new BigDecimal("10.0000"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO
        );
    }

    private static BillableGrQueryPort.GrLineData grLineData(String quantityReceived, String inventoryAmount) {
        return grLineData(quantityReceived, inventoryAmount, "0.0000");
    }

    private static BillableGrQueryPort.GrLineData grLineData(String quantityReceived, String inventoryAmount, String taxAmount) {
        return new BillableGrQueryPort.GrLineData(
                new BigDecimal(quantityReceived),
                new BigDecimal(inventoryAmount),
                new BigDecimal(taxAmount),
                new BigDecimal(inventoryAmount).add(new BigDecimal(taxAmount))
        );
    }

    private static final class InMemoryVendorBillRepository implements VendorBillRepository {
        private VendorBill existing;
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
            return Optional.ofNullable(existing);
        }

        @Override
        public void deleteById(Long id) {
        }
    }

    private static final class FakeBillableGrQueryPort implements BillableGrQueryPort {
        private final Map<Long, GrLineData> grLineData = new HashMap<>();
        private final Map<Long, BigDecimal> confirmedLineTotals = new HashMap<>();
        private final Map<Long, BigDecimal> confirmedTaxAmounts = new HashMap<>();
        private final Map<Long, BigDecimal> confirmedBilledQty = new HashMap<>();

        @Override
        public List<com.solusi.erp.accountspayable.vendorbill.domain.port.BillableGrView> findBillableGrs(Long vendorId, Long currencyId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<com.solusi.erp.accountspayable.vendorbill.domain.port.BillableGrLineView> findBillableGrLines(Long grId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Map<Long, BigDecimal> sumConfirmedBilledQtyByGrId(Long grId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public GrLineData getGrLineData(Long grLineId) {
            return grLineData.get(grLineId);
        }

        @Override
        public BigDecimal sumConfirmedLineTotals(Long grLineId, Long excludeBillId) {
            return confirmedLineTotals.getOrDefault(grLineId, BigDecimal.ZERO);
        }

        @Override
        public BigDecimal sumConfirmedTaxAmounts(Long grLineId, Long excludeBillId) {
            return confirmedTaxAmounts.getOrDefault(grLineId, BigDecimal.ZERO);
        }

        @Override
        public BigDecimal sumConfirmedBilledQty(Long grLineId, Long excludeBillId) {
            return confirmedBilledQty.getOrDefault(grLineId, BigDecimal.ZERO);
        }

        @Override
        public BigDecimal getGrExchangeRate(Long grLineId) {
            return BigDecimal.ONE;
        }
    }

    private static final class RecordingEnsureOpenPeriodForDateUseCase implements EnsureOpenPeriodForDateUseCase {
        private LocalDate date;

        @Override
        public void execute(LocalDate date) {
            this.date = date;
        }
    }

    private static final class RecordingPostJournalForEventUseCase implements PostJournalForEventUseCase {
        private JournalPostingCommand command;

        @Override
        public void execute(JournalPostingCommand command) {
            this.command = command;
        }
    }
}
