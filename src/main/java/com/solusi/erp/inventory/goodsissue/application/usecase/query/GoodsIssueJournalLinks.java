package com.solusi.erp.inventory.goodsissue.application.usecase.query;

public record GoodsIssueJournalLinks(Long originalJournalId, Long reversalJournalId) {
    public boolean hasOriginalJournal() {
        return originalJournalId != null;
    }

    public boolean hasReversalJournal() {
        return reversalJournalId != null;
    }
}
