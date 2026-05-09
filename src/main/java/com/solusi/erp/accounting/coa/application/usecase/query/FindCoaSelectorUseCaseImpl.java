package com.solusi.erp.accounting.coa.application.usecase.query;

import com.solusi.erp.accounting.coa.domain.model.ChartOfAccount;
import com.solusi.erp.accounting.coa.domain.repository.CoaRepository;
import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FindCoaSelectorUseCaseImpl implements FindCoaSelectorUseCase {

    private final CoaRepository repository;

    public FindCoaSelectorUseCaseImpl(CoaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Page<CoaSelectorRow> execute(String keyword, String accountType, Pageable pageable) {
        Page<ChartOfAccount> selectorPage = repository.findAllActiveForSelector(
                normalizeKeyword(keyword),
                normalizeAccountType(accountType),
                pageable
        );
        List<ChartOfAccount> allAccounts = repository.findAllActive();

        Map<Long, ChartOfAccount> accountMap = allAccounts.stream()
                .collect(Collectors.toMap(ChartOfAccount::getId, a -> a));

        List<CoaSelectorRow> rows = selectorPage.content().stream()
                .map(a -> {
                    ChartOfAccount parent = a.getParentId() != null ? accountMap.get(a.getParentId()) : null;
                    return new CoaSelectorRow(
                            a.getId(),
                            a.getCode(),
                            a.getName(),
                            a.getAccountType() != null ? a.getAccountType().name() : null,
                            a.getLevel(),
                            a.getIsHeader(),
                            a.getParentId(),
                            parent != null ? parent.getCode() : null,
                            parent != null ? parent.getName() : null
                    );
                })
                .collect(Collectors.toList());

        return new Page<>(rows, selectorPage.page(), selectorPage.size(), selectorPage.totalElements());
    }

    private String normalizeKeyword(String keyword) {
        return keyword == null || keyword.isBlank() ? null : keyword.trim();
    }

    private String normalizeAccountType(String accountType) {
        return accountType == null || accountType.isBlank() ? null : accountType.trim().toUpperCase();
    }
}
