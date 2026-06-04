package com.solusi.erp.inventory.goodsissue.application.usecase.query;

import com.solusi.erp.accounting.journal.domain.model.JournalEntry;
import com.solusi.erp.accounting.journal.domain.repository.JournalEntryRepository;

public class GetGoodsIssueJournalLinksUseCaseImpl implements GetGoodsIssueJournalLinksUseCase {

    private static final String GOODS_ISSUE_SOURCE_TYPE = "GOODS_ISSUE";

    private final JournalEntryRepository journalEntryRepository;

    public GetGoodsIssueJournalLinksUseCaseImpl(JournalEntryRepository journalEntryRepository) {
        this.journalEntryRepository = journalEntryRepository;
    }

    @Override
    public GoodsIssueJournalLinks execute(Long goodsIssueId) {
        if (goodsIssueId == null) {
            return new GoodsIssueJournalLinks(null, null);
        }
        return journalEntryRepository.findBySource(GOODS_ISSUE_SOURCE_TYPE, goodsIssueId)
                .map(this::toLinks)
                .orElseGet(() -> new GoodsIssueJournalLinks(null, null));
    }

    private GoodsIssueJournalLinks toLinks(JournalEntry originalJournal) {
        Long reversalJournalId = journalEntryRepository.findReversalOf(originalJournal.getId())
                .map(JournalEntry::getId)
                .orElse(null);
        return new GoodsIssueJournalLinks(originalJournal.getId(), reversalJournalId);
    }
}
