package com.solusi.erp.accountspayable.vendorbill.application.usecase.query;

import com.solusi.erp.accountspayable.vendorbill.domain.port.BillableGrLineView;
import com.solusi.erp.accountspayable.vendorbill.domain.port.BillableGrQueryPort;

import java.util.List;

public class FindBillableGrLinesUseCaseImpl implements FindBillableGrLinesUseCase {

    private final BillableGrQueryPort billableGrQueryPort;

    public FindBillableGrLinesUseCaseImpl(BillableGrQueryPort billableGrQueryPort) {
        this.billableGrQueryPort = billableGrQueryPort;
    }

    @Override
    public List<BillableGrLineView> execute(Long grId) {
        return billableGrQueryPort.findBillableGrLines(grId);
    }
}
