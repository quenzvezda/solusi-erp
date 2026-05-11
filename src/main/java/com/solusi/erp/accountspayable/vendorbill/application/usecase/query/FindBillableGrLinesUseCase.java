package com.solusi.erp.accountspayable.vendorbill.application.usecase.query;

import com.solusi.erp.accountspayable.vendorbill.domain.port.BillableGrLineView;

import java.util.List;

public interface FindBillableGrLinesUseCase {
    List<BillableGrLineView> execute(Long grId);
}
