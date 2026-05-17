package com.solusi.erp.accounting.journal.web.controller;

import com.solusi.erp.accounting.journal.application.usecase.query.FindJournalEntriesUseCase;
import com.solusi.erp.accounting.journal.application.usecase.query.GetJournalEntryDetailUseCase;
import com.solusi.erp.accounting.journal.domain.model.JournalEntry;
import com.solusi.erp.accounting.journal.domain.model.JournalEntryFilter;
import com.solusi.erp.accounting.schema.domain.model.SchemaEventType;
import com.solusi.erp.accounting.journal.web.dto.JournalEntryDetailResponse;
import com.solusi.erp.accounting.journal.web.dto.JournalEntryListResponse;
import com.solusi.erp.accounting.journal.web.mapper.JournalEntryWebMapper;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.core.infrastructure.util.PageableMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/accounting/journal-entries")
@RequiredArgsConstructor
public class JournalEntryController {

    private final FindJournalEntriesUseCase findJournalEntriesUseCase;
    private final GetJournalEntryDetailUseCase getJournalEntryDetailUseCase;
    private final JournalEntryWebMapper webMapper;

    @GetMapping
    @PreAuthorize("hasAuthority('JOURNAL-ENTRY_READ')")
    public String list(
            @RequestParam(required = false) SchemaEventType sourceType,
            @RequestParam(required = false) String sourceCode,
            @RequestParam(required = false) String journalCode,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate postingDateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate postingDateTo,
            org.springframework.data.domain.Pageable springPageable,
            Model model) {
        
        JournalEntryFilter filter = new JournalEntryFilter(sourceType, sourceCode, journalCode, postingDateFrom, postingDateTo);
        Pageable domainPageable = PageableMapper.toDomain(springPageable);
        
        com.solusi.erp.core.domain.model.Page<JournalEntry> domainPage = findJournalEntriesUseCase.execute(filter, domainPageable);
        List<JournalEntryListResponse> content = domainPage.content().stream().map(webMapper::toListResponse).toList();
        Page<JournalEntryListResponse> springPage = new PageImpl<>(content, springPageable, domainPage.totalElements());

        model.addAttribute("page", springPage);
        model.addAttribute("filter", filter);
        model.addAttribute("eventTypes", SchemaEventType.values());
        
        return "accounting/journal/journal-entry-list";
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('JOURNAL-ENTRY_READ')")
    public String detail(@PathVariable Long id, Model model) {
        JournalEntry entry = getJournalEntryDetailUseCase.execute(id)
                .orElseThrow(() -> new RuntimeException("Journal Entry not found"));
        JournalEntryDetailResponse response = webMapper.toDetailResponse(entry);
        model.addAttribute("journal", response);
        return "accounting/journal/journal-entry-detail";
    }
}