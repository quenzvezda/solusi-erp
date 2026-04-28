package com.solusi.erp.accounting.coa.application.usecase.query;

import java.util.List;

public interface FindCoaSelectorUseCase {
    List<CoaSelectorRow> execute(String keyword, String accountType);
}
