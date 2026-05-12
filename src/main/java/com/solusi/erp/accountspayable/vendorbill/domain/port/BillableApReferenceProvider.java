package com.solusi.erp.accountspayable.vendorbill.domain.port;

import java.util.List;

public interface BillableApReferenceProvider {
    String getSourceType();

    List<BillableApReference> findBillableReferences(Long vendorId, Long currencyId);
}
