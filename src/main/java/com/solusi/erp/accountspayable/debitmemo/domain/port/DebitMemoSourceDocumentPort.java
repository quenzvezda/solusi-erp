package com.solusi.erp.accountspayable.debitmemo.domain.port;

import java.util.Optional;

public interface DebitMemoSourceDocumentPort {

    Optional<Long> findGeneratedGoodsIssueId(Long purchaseReturnId);
}

