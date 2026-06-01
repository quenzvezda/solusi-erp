package com.solusi.erp.inventory.goodsissue.infrastructure.adapter;

import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssueReferenceType;
import com.solusi.erp.inventory.goodsissue.domain.port.GoodsIssueReferenceLookupProvider;

import java.util.List;
import java.util.Map;

public class GoodsIssueReferenceLookupProviderImpl implements GoodsIssueReferenceLookupProvider {

    @Override
    public String resolveReferenceCode(GoodsIssueReferenceType referenceType, Long referenceId) {
        return null;
    }

    @Override
    public Map<Long, ReferenceLineSnapshot> resolveReferenceLineSnapshots(GoodsIssueReferenceType referenceType,
                                                                          Long referenceId) {
        return Map.of();
    }

    @Override
    public List<SourceLineSelectorRow> resolveSourceLineSelectorRows(GoodsIssueReferenceType referenceType,
                                                                     Long referenceId) {
        return List.of();
    }
}
