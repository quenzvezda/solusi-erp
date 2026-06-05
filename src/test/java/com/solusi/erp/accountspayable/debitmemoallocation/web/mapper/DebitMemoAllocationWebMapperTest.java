package com.solusi.erp.accountspayable.debitmemoallocation.web.mapper;

import com.solusi.erp.accountspayable.debitmemoallocation.application.usecase.command.CreateDebitMemoAllocationCommand;
import com.solusi.erp.accountspayable.debitmemoallocation.application.usecase.command.ReverseDebitMemoAllocationCommand;
import com.solusi.erp.accountspayable.debitmemoallocation.application.usecase.command.UpdateDebitMemoAllocationCommand;
import com.solusi.erp.accountspayable.debitmemoallocation.application.usecase.query.DebitMemoAllocationDetailView;
import com.solusi.erp.accountspayable.debitmemoallocation.application.usecase.query.DebitMemoAllocationLineView;
import com.solusi.erp.accountspayable.debitmemoallocation.application.usecase.query.DebitMemoAllocationSummaryView;
import com.solusi.erp.accountspayable.debitmemoallocation.domain.model.DebitMemoAllocationStatus;
import com.solusi.erp.accountspayable.debitmemoallocation.web.dto.DebitMemoAllocationReverseRequest;
import com.solusi.erp.accountspayable.debitmemoallocation.web.dto.DebitMemoAllocationSaveRequest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class DebitMemoAllocationWebMapperTest {

    private final DebitMemoAllocationWebMapper mapper = new DebitMemoAllocationWebMapper();

    @Test
    void should_map_summary_detail_and_commands() {
        DebitMemoAllocationSummaryView summary = new DebitMemoAllocationSummaryView(
                1L, "DMA-001", 10L, "DM-001", LocalDate.of(2026, 6, 5),
                DebitMemoAllocationStatus.DRAFT, bd("40.0000"), bd("50.0000"), bd("10.0000"), BigDecimal.ZERO);
        assertThat(mapper.toSummaryResponse(summary).getStatus()).isEqualTo("DRAFT");

        DebitMemoAllocationDetailView detail = detailView();
        assertThat(mapper.toDetailResponse(detail).getLines()).hasSize(1);
        DebitMemoAllocationSaveRequest request = mapper.toSaveRequest(detail);
        assertThat(request.getDebitMemoId()).isEqualTo(10L);
        assertThat(request.getLines().getFirst().getAppliedGrossOriginal()).isEqualByComparingTo("40.0000");

        CreateDebitMemoAllocationCommand create = mapper.toCreateCommand(request);
        UpdateDebitMemoAllocationCommand update = mapper.toUpdateCommand(1L, request);
        assertThat(create.debitMemoId()).isEqualTo(10L);
        assertThat(update.id()).isEqualTo(1L);

        DebitMemoAllocationReverseRequest reverseRequest = new DebitMemoAllocationReverseRequest();
        reverseRequest.setReversalDate(LocalDate.of(2026, 6, 6));
        reverseRequest.setReversalReason("wrong allocation");
        ReverseDebitMemoAllocationCommand reverse = mapper.toReverseCommand(1L, reverseRequest);
        assertThat(reverse.id()).isEqualTo(1L);
        assertThat(reverse.reversalReason()).isEqualTo("wrong allocation");
    }

    public static DebitMemoAllocationDetailView detailView() {
        return new DebitMemoAllocationDetailView(
                1L, "DMA-001", 10L, "DM-001", LocalDate.of(2026, 6, 5),
                DebitMemoAllocationStatus.DRAFT, bd("40.0000"), bd("36.0000"), bd("4.0000"),
                bd("36.0000"), bd("4.0000"), bd("50.0000"), bd("10.0000"), BigDecimal.ZERO,
                null, null, null, null, "notes",
                List.of(new DebitMemoAllocationLineView(null, 20L, "VB-001", bd("100.0000"),
                        bd("80.0000"), bd("40.0000"), bd("36.0000"), bd("4.0000"),
                        bd("36.0000"), bd("4.0000"), bd("1.250000"), bd("50.0000"),
                        bd("10.0000"), BigDecimal.ZERO))
        );
    }

    private static BigDecimal bd(String value) {
        return new BigDecimal(value);
    }
}
