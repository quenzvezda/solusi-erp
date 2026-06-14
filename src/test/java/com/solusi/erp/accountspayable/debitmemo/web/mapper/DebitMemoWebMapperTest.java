package com.solusi.erp.accountspayable.debitmemo.web.mapper;

import com.solusi.erp.accountspayable.debitmemo.application.usecase.query.DebitMemoDetailView;
import com.solusi.erp.accountspayable.debitmemo.application.usecase.query.DebitMemoLineView;
import com.solusi.erp.accountspayable.debitmemo.application.usecase.query.DebitMemoSummaryView;
import com.solusi.erp.accountspayable.debitmemo.domain.model.DebitMemoSettlementStatus;
import com.solusi.erp.accountspayable.debitmemo.web.dto.DebitMemoDetailResponse;
import com.solusi.erp.accountspayable.debitmemo.web.dto.DebitMemoSummaryResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DebitMemoWebMapperTest {

    private final DebitMemoWebMapper mapper = new DebitMemoWebMapper();

    @Test
    void toSummaryResponse_should_map_status_and_amounts() {
        DebitMemoSummaryView view = new DebitMemoSummaryView(
                10L,
                "DM-202606-00001",
                LocalDate.of(2026, 6, 2),
                22L,
                1L,
                100L,
                "PRT-202606-00001",
                new BigDecimal("111.0000"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                new BigDecimal("111.0000"),
                DebitMemoSettlementStatus.OPEN
        );

        DebitMemoSummaryResponse response = mapper.toSummaryResponse(view);

        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getCode()).isEqualTo("DM-202606-00001");
        assertThat(response.getSettlementStatus()).isEqualTo("OPEN");
        assertThat(response.getRemainingAmount()).isEqualByComparingTo("111.0000");
    }

    @Test
    void toDetailResponse_should_map_source_links_metadata_and_lines() {
        DebitMemoDetailView view = new DebitMemoDetailView(
                10L,
                "DM-202606-00001",
                100L,
                "PRT-202606-00001",
                900L,
                22L,
                1L,
                LocalDate.of(2026, 6, 2),
                new BigDecimal("111.0000"),
                new BigDecimal("100.0000"),
                new BigDecimal("11.0000"),
                new BigDecimal("111.0000"),
                new BigDecimal("100.0000"),
                new BigDecimal("11.0000"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                new BigDecimal("111.0000"),
                DebitMemoSettlementStatus.OPEN,
                "SUP-DM-001",
                LocalDate.of(2026, 6, 3),
                "TAX-001",
                LocalDate.of(2026, 6, 4),
                "notes",
                List.of(new DebitMemoLineView(
                        1L,
                        1001L,
                        501L,
                        new BigDecimal("2.0000"),
                        1L,
                        new BigDecimal("100.0000"),
                        new BigDecimal("11.0000"),
                        new BigDecimal("100.0000"),
                        new BigDecimal("11.0000")
                ))
        );

        DebitMemoDetailResponse response = mapper.toDetailResponse(view);

        assertThat(response.getGeneratedGoodsIssueId()).isEqualTo(900L);
        assertThat(response.getSupplierMemoNumber()).isEqualTo("SUP-DM-001");
        assertThat(response.getLines()).hasSize(1);
        assertThat(response.getLines().getFirst().getPurchaseReturnLineId()).isEqualTo(1001L);
    }
}

