package com.solusi.erp.inventory.goodsreceipt.web.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class GoodsReceiptSaveRequest extends BaseAuditResponse {
    @NotNull(message = "{label.gr.receiptDate} {validation.notnull.suffix}")
    private LocalDate receiptDate;
    private String notes;
    @Valid
    private List<GoodsReceiptSaveLineRequest> lines = new ArrayList<>();
}
