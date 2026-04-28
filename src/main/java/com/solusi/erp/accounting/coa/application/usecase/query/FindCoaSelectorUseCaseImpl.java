package com.solusi.erp.accounting.coa.application.usecase.query;

import com.solusi.erp.accounting.coa.domain.model.ChartOfAccount;
import com.solusi.erp.accounting.coa.domain.repository.CoaRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FindCoaSelectorUseCaseImpl implements FindCoaSelectorUseCase {

    private final CoaRepository repository;

    public FindCoaSelectorUseCaseImpl(CoaRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<CoaSelectorRow> execute(String keyword, String accountType) {
        List<ChartOfAccount> allAccounts = repository.findAllActive();

        // Build lookup map for parent info
        Map<Long, ChartOfAccount> accountMap = allAccounts.stream()
                .collect(Collectors.toMap(ChartOfAccount::getId, a -> a));

        return allAccounts.stream()
                .filter(a -> matchesKeyword(a, keyword))
                .filter(a -> matchesAccountType(a, accountType))
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
    }

    private boolean matchesKeyword(ChartOfAccount account, String keyword) {
        if (keyword == null || keyword.isBlank()) return true;
        String lowerKeyword = keyword.toLowerCase();
        return (account.getCode() != null && account.getCode().toLowerCase().contains(lowerKeyword)) ||
               (account.getName() != null && account.getName().toLowerCase().contains(lowerKeyword));
    }

    private boolean matchesAccountType(ChartOfAccount account, String accountType) {
        if (accountType == null || accountType.isBlank()) return true;
        return account.getAccountType() != null && account.getAccountType().name().equals(accountType);
    }
}
