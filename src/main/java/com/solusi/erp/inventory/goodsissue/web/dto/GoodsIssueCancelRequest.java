package com.solusi.erp.inventory.goodsissue.web.dto;

import com.solusi.erp.inventory.goodsissue.application.usecase.query.GoodsIssueCancelLineView;
import com.solusi.erp.inventory.goodsissue.application.usecase.query.GoodsIssueCancelView;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

@Data
public class GoodsIssueCancelRequest {

    @NotNull(message = "{validation.gi.cancel.reversalDate.required}")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate reversalDate;

    @NotBlank(message = "{validation.gi.cancel.reason.required}")
    private String reason;

    @Valid
    @NotEmpty(message = "{validation.gi.cancel.lines.required}")
    private List<GoodsIssueCancelLineRequest> lines;

    public static GoodsIssueCancelRequest from(GoodsIssueCancelView view) {
        GoodsIssueCancelRequest request = new GoodsIssueCancelRequest();
        request.setReversalDate(LocalDate.now());
        request.setLines(view.lines().stream().map(GoodsIssueCancelRequest::fromLine).toList());
        return request;
    }

    private static GoodsIssueCancelLineRequest fromLine(GoodsIssueCancelLineView line) {
        return new GoodsIssueCancelLineRequest(
                null,
                line.originalMovementId(),
                line.historicalContainerId(),
                line.productName(),
                line.serialNumber(),
                line.quantityIssued()
        );
    }
}
