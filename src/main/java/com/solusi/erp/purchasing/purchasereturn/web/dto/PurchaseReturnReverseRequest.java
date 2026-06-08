package com.solusi.erp.purchasing.purchasereturn.web.dto;

import com.solusi.erp.purchasing.purchasereturn.application.usecase.query.GetPurchaseReturnReverseViewUseCase.PurchaseReturnReverseView;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.query.PurchaseReturnReverseLineView;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

@Data
public class PurchaseReturnReverseRequest {

    @NotNull(message = "{validation.purchase-return.reverse.reversal-date-required}")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate reversalDate;

    @NotBlank(message = "{validation.purchase-return.reverse.reason-required}")
    private String reversalReason;

    @Valid
    @NotEmpty(message = "{validation.purchase-return.reverse.lines-required}")
    private List<PurchaseReturnReverseLineRequest> lines;

    public static PurchaseReturnReverseRequest from(PurchaseReturnReverseView view) {
        PurchaseReturnReverseRequest request = new PurchaseReturnReverseRequest();
        request.setReversalDate(LocalDate.now());
        request.setLines(view.lines().stream().map(PurchaseReturnReverseRequest::fromLine).toList());
        return request;
    }

    private static PurchaseReturnReverseLineRequest fromLine(PurchaseReturnReverseLineView line) {
        return new PurchaseReturnReverseLineRequest(line.originalMovementId(), line.targetContainerId());
    }
}
