package com.solusi.erp.accountspayable.debitmemo.application.usecase.query;

import com.solusi.erp.accountspayable.debitmemo.domain.model.DebitMemoSettlementStatus;
import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;

import java.time.LocalDate;

public interface FindDebitMemosUseCase {

    Page<DebitMemoSummaryView> execute(String keyword,
                                       Long vendorId,
                                       DebitMemoSettlementStatus settlementStatus,
                                       LocalDate memoDateFrom,
                                       LocalDate memoDateTo,
                                       Pageable pageable);
}

