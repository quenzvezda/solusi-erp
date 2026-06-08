package com.solusi.erp.purchasing.purchasereturn.domain.port;

import java.time.LocalDate;

public interface PurchaseReturnJournalReversalPort {

    Long reverseOriginalPurchaseReturnJournal(Long purchaseReturnId, LocalDate reversalDate, String reversalReason);
}
