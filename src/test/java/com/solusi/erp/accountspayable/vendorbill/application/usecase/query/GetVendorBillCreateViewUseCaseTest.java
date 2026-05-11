package com.solusi.erp.accountspayable.vendorbill.application.usecase.query;

import com.solusi.erp.accountspayable.vendorbill.domain.port.BillableGrLineView;
import com.solusi.erp.accountspayable.vendorbill.domain.port.BillableGrQueryPort;
import com.solusi.erp.accountspayable.vendorbill.domain.port.BillableGrView;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GetVendorBillCreateViewUseCaseTest {

    @Test
    void execute_should_load_billable_grs_for_vendor_and_currency() {
        FakeBillableGrQueryPort port = new FakeBillableGrQueryPort();
        port.billableGrs = List.of(new BillableGrView(88L, "GR-001", 77L, "PO-001", 10L, 1L));

        VendorBillCreateView result = new GetVendorBillCreateViewUseCaseImpl(port).execute(10L, 1L);

        assertThat(port.vendorId).isEqualTo(10L);
        assertThat(port.currencyId).isEqualTo(1L);
        assertThat(result.vendorId()).isEqualTo(10L);
        assertThat(result.currencyId()).isEqualTo(1L);
        assertThat(result.billableGrs()).hasSize(1);
        assertThat(result.billableGrs().getFirst().grCode()).isEqualTo("GR-001");
    }

    private static final class FakeBillableGrQueryPort implements BillableGrQueryPort {
        private Long vendorId;
        private Long currencyId;
        private List<BillableGrView> billableGrs = List.of();

        @Override
        public List<BillableGrView> findBillableGrs(Long vendorId, Long currencyId) {
            this.vendorId = vendorId;
            this.currencyId = currencyId;
            return billableGrs;
        }

        @Override
        public List<BillableGrLineView> findBillableGrLines(Long grId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Map<Long, BigDecimal> sumConfirmedBilledQtyByGrId(Long grId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public GrLineData getGrLineData(Long grLineId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public BigDecimal sumConfirmedLineTotals(Long grLineId, Long excludeBillId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public BigDecimal sumConfirmedBilledQty(Long grLineId, Long excludeBillId) {
            throw new UnsupportedOperationException();
        }
    }
}
