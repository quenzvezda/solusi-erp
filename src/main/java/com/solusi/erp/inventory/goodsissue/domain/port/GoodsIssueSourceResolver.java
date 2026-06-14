package com.solusi.erp.inventory.goodsissue.domain.port;

import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssue;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssueReferenceType;

public interface GoodsIssueSourceResolver {

    GoodsIssueReferenceType getReferenceType();

    GoodsIssue resolve(Long referenceId);
}
