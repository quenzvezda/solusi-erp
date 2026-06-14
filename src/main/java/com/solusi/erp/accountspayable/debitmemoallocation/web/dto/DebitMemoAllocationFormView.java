package com.solusi.erp.accountspayable.debitmemoallocation.web.dto;

public record DebitMemoAllocationFormView(
        DebitMemoAllocationSaveRequest request,
        DebitMemoAllocationDetailResponse detail,
        boolean editMode
) {
}
