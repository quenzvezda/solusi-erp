package com.solusi.erp.inventory.goodsissue.application.usecase.query;

import com.solusi.erp.accounting.journal.domain.model.JournalEntry;
import com.solusi.erp.accounting.journal.domain.repository.JournalEntryRepository;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssue;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssueReferenceType;
import com.solusi.erp.inventory.goodsissue.domain.repository.GoodsIssueRepository;

import java.util.Optional;

public class GetGoodsIssueJournalLinksUseCaseImpl implements GetGoodsIssueJournalLinksUseCase {

    private static final String GOODS_ISSUE_SOURCE_TYPE = "GOODS_ISSUE";
    private static final String PURCHASE_RETURN_SOURCE_TYPE = "PURCHASE_RETURN";

    private final GoodsIssueRepository goodsIssueRepository;
    private final JournalEntryRepository journalEntryRepository;

    public GetGoodsIssueJournalLinksUseCaseImpl(GoodsIssueRepository goodsIssueRepository,
                                                JournalEntryRepository journalEntryRepository) {
        this.goodsIssueRepository = goodsIssueRepository;
        this.journalEntryRepository = journalEntryRepository;
    }

    @Override
    public GoodsIssueJournalLinks execute(Long goodsIssueId) {
        if (goodsIssueId == null) {
            return new GoodsIssueJournalLinks(null, null);
        }
        return goodsIssueRepository.findById(goodsIssueId)
                .flatMap(this::findOriginalJournal)
                .map(this::toLinks)
                .orElseGet(() -> new GoodsIssueJournalLinks(null, null));
    }

    private Optional<JournalEntry> findOriginalJournal(GoodsIssue issue) {
        if (issue.getReferenceType() == GoodsIssueReferenceType.PURCHASE_RETURN) {
            return journalEntryRepository.findBySource(PURCHASE_RETURN_SOURCE_TYPE, issue.getReferenceId());
        }
        return journalEntryRepository.findBySource(GOODS_ISSUE_SOURCE_TYPE, issue.getId());
    }

    private GoodsIssueJournalLinks toLinks(JournalEntry originalJournal) {
        Long reversalJournalId = journalEntryRepository.findReversalOf(originalJournal.getId())
                .map(JournalEntry::getId)
                .orElse(null);
        return new GoodsIssueJournalLinks(originalJournal.getId(), reversalJournalId);
    }
}
