package com.solusi.erp.accounting.journal.web.controller;

import com.solusi.erp.accounting.journal.application.usecase.command.*;
import com.solusi.erp.accounting.journal.application.usecase.query.FindJournalEntriesUseCase;
import com.solusi.erp.accounting.journal.application.usecase.query.GetJournalEntryDetailUseCase;
import com.solusi.erp.accounting.journal.application.usecase.query.JournalEntryDetailView;
import com.solusi.erp.accounting.journal.domain.model.JournalEntry;
import com.solusi.erp.accounting.journal.domain.model.JournalEntryFilter;
import com.solusi.erp.accounting.schema.domain.model.SchemaEventType;
import com.solusi.erp.accounting.journal.web.dto.JournalEntrySaveRequest;
import com.solusi.erp.accounting.journal.web.dto.JournalEntryDetailResponse;
import com.solusi.erp.accounting.journal.web.dto.JournalEntryListResponse;
import com.solusi.erp.accounting.journal.web.dto.JournalLineSaveRequest;
import com.solusi.erp.accounting.journal.web.dto.ReverseJournalRequest;
import com.solusi.erp.accounting.journal.web.mapper.JournalEntryWebMapper;
import com.solusi.erp.core.annotation.DefaultRedirectUrl;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.core.dto.ApiResponse;
import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.core.infrastructure.util.PageableMapper;
import com.solusi.erp.master.currency.application.usecase.query.GetDefaultCurrencyUseCase;
import com.solusi.erp.master.currency.domain.port.CurrencyLookupProvider;
import com.solusi.erp.util.HtmxResponseUtility;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/accounting/journal-entries")
@RequiredArgsConstructor
@DefaultRedirectUrl
public class JournalEntryController {

    private final CreateManualJournalUseCase createManualJournalUseCase;
    private final UpdateManualJournalUseCase updateManualJournalUseCase;
    private final DeleteManualJournalUseCase deleteManualJournalUseCase;
    private final PostManualJournalUseCase postManualJournalUseCase;
    private final ReverseManualJournalUseCase reverseManualJournalUseCase;
    private final FindJournalEntriesUseCase findJournalEntriesUseCase;
    private final GetJournalEntryDetailUseCase getJournalEntryDetailUseCase;
    private final GetDefaultCurrencyUseCase getDefaultCurrencyUseCase;
    private final CurrencyLookupProvider currencyLookupProvider;
    private final JournalEntryWebMapper webMapper;
    private final MessageSource messageSource;

    @GetMapping
    @PreAuthorize("hasAuthority('JOURNAL-ENTRY_READ')")
    public String list(
            @RequestParam(required = false) String sourceType,
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
        model.addAttribute("eventTypes", eventTypeFilters());
        
        return "accounting/journal/journal-entry-list";
    }

    @GetMapping("/create")
    @PreAuthorize("hasAuthority('JOURNAL-ENTRY_CREATE')")
    public String createForm(Model model) {
        JournalEntrySaveRequest request = new JournalEntrySaveRequest();
        request.setPostingDate(LocalDate.now());
        request.setExchangeRate(BigDecimal.ONE);
        request.getLines().add(new JournalLineSaveRequest());
        request.getLines().add(new JournalLineSaveRequest());
        Map<String, Object> journalUI = new HashMap<>();
        getDefaultCurrencyUseCase.execute().ifPresent(currency -> {
            request.setCurrencyId(currency.getId());
            LookupDto lookup = currencyLookupProvider.resolve(currency.getId());
            if (lookup != null) {
                journalUI.put("currencyText", lookup.name());
                journalUI.put("currencySubtext", lookup.subText());
                journalUI.put("currencyIsDefault", Boolean.TRUE.equals(currency.getIsDefault()));
            }
        });
        model.addAttribute("journalRequest", request);
        model.addAttribute("journalUI", journalUI);
        return "accounting/journal/journal-entry-form";
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('JOURNAL-ENTRY_UPDATE')")
    public String editForm(@PathVariable Long id, Model model) {
        JournalEntryDetailView view = getJournalEntryDetailUseCase.execute(id)
                .orElseThrow(() -> new RuntimeException("Journal Entry not found"));
        model.addAttribute("journalRequest", webMapper.toSaveRequest(view.entry()));
        model.addAttribute("journalUI", buildJournalUI(view.entry()));
        return "accounting/journal/journal-entry-form";
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('JOURNAL-ENTRY_READ')")
    public String detail(@PathVariable Long id, Model model) {
        JournalEntryDetailView view = getJournalEntryDetailUseCase.execute(id)
                .orElseThrow(() -> new RuntimeException("Journal Entry not found"));
        JournalEntryDetailResponse response = webMapper.toDetailResponse(view);
        model.addAttribute("journal", response);
        return "accounting/journal/journal-entry-detail";
    }

    @PostMapping
    @PreAuthorize("hasAuthority('JOURNAL-ENTRY_CREATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<JournalEntryDetailResponse>> create(@Valid @RequestBody JournalEntrySaveRequest request) {
        JournalEntry entry = createManualJournalUseCase.execute(webMapper.toManualJournalCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(msg("msg.success.create"), webMapper.toDetailResponse(new JournalEntryDetailView(entry, null))));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('JOURNAL-ENTRY_UPDATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<JournalEntryDetailResponse>> update(@PathVariable Long id,
                                                                          @Valid @RequestBody JournalEntrySaveRequest request) {
        JournalEntry entry = updateManualJournalUseCase.execute(id, webMapper.toManualJournalCommand(request));
        return ResponseEntity.ok(ApiResponse.success(msg("msg.success.update"), webMapper.toDetailResponse(new JournalEntryDetailView(entry, null))));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('JOURNAL-ENTRY_DELETE')")
    @ResponseBody
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deleteManualJournalUseCase.execute(id);
        return HtmxResponseUtility.okWithRefreshTableAndSuccess(msg("msg.success.delete"));
    }

    @PostMapping("/{id}/post")
    @PreAuthorize("hasAuthority('JOURNAL-ENTRY_POST')")
    @ResponseBody
    public ResponseEntity<ApiResponse<JournalEntryDetailResponse>> post(@PathVariable Long id) {
        JournalEntry entry = postManualJournalUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.success(msg("msg.success.journal.posted"), webMapper.toDetailResponse(new JournalEntryDetailView(entry, null))));
    }

    @PostMapping("/{id}/reverse")
    @PreAuthorize("hasAuthority('JOURNAL-ENTRY_REVERSE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<JournalEntryDetailResponse>> reverse(@PathVariable Long id,
                                                                           @Valid @RequestBody ReverseJournalRequest request) {
        JournalEntry entry = reverseManualJournalUseCase.execute(id, request.getPostingDate());
        return ResponseEntity.ok(ApiResponse.success(msg("msg.success.journal.reversed"), webMapper.toDetailResponse(new JournalEntryDetailView(entry, null))));
    }

    private List<String> eventTypeFilters() {
        List<String> eventTypes = new java.util.ArrayList<>(Arrays.stream(SchemaEventType.values()).map(Enum::name).toList());
        eventTypes.add("MANUAL");
        return eventTypes;
    }

    private Map<String, Object> buildJournalUI(JournalEntry entry) {
        Map<String, Object> ui = new HashMap<>();
        LookupDto currency = currencyLookupProvider.resolve(entry.getCurrencyId());
        if (currency != null) {
            ui.put("currencyText", currency.name());
            ui.put("currencySubtext", currency.subText());
            Object isDefault = currency.payload() != null ? currency.payload().get("isDefault") : null;
            ui.put("currencyIsDefault", Boolean.TRUE.equals(isDefault));
        }
        return ui;
    }

    private String msg(String messageKey) {
        return messageSource.getMessage(messageKey, null, LocaleContextHolder.getLocale());
    }
}
