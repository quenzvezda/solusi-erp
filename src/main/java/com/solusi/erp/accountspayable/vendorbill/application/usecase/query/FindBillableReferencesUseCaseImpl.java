package com.solusi.erp.accountspayable.vendorbill.application.usecase.query;

import com.solusi.erp.accountspayable.vendorbill.domain.port.BillableApReference;
import com.solusi.erp.accountspayable.vendorbill.domain.port.BillableApReferenceProvider;

import java.util.Comparator;
import java.util.List;

public class FindBillableReferencesUseCaseImpl implements FindBillableReferencesUseCase {

    private final List<BillableApReferenceProvider> providers;

    public FindBillableReferencesUseCaseImpl(List<BillableApReferenceProvider> providers) {
        this.providers = providers;
    }

    @Override
    public List<BillableApReference> execute(Long vendorId, Long currencyId) {
        return providers.stream()
                .flatMap(provider -> provider.findBillableReferences(vendorId, currencyId).stream())
                .sorted(Comparator.comparing(BillableApReference::sourceDate, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }
}
