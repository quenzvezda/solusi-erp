package com.solusi.erp.accounting.journal.web.mapper;

import com.solusi.erp.accounting.coa.domain.port.CoaLookupProvider;
import com.solusi.erp.accounting.journal.application.usecase.command.ManualJournalCommand;
import com.solusi.erp.accounting.journal.application.usecase.command.ManualJournalLineCommand;
import com.solusi.erp.accounting.journal.application.usecase.query.JournalEntryDetailView;
import com.solusi.erp.accounting.journal.domain.model.JournalEntry;
import com.solusi.erp.accounting.journal.domain.model.JournalLine;
import com.solusi.erp.accounting.journal.web.dto.JournalEntryDetailResponse;
import com.solusi.erp.accounting.journal.web.dto.JournalEntrySaveRequest;
import com.solusi.erp.accounting.journal.web.dto.JournalEntryListResponse;
import com.solusi.erp.accounting.journal.web.dto.JournalLineSaveRequest;
import com.solusi.erp.accounting.journal.web.dto.JournalLineResponse;
import com.solusi.erp.core.dto.BaseAuditResponse;
import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.core.mapper.AuditMapperHelper;
import com.solusi.erp.master.currency.domain.port.CurrencyLookupProvider;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Mapper(componentModel = "spring")
public abstract class JournalEntryWebMapper {

    @Autowired
    protected AuditMapperHelper auditMapperHelper;

    @Autowired
    protected CoaLookupProvider coaLookupProvider;

    @Autowired
    protected CurrencyLookupProvider currencyLookupProvider;

    @Mapping(target = "journalCode", source = "id", qualifiedByName = "formatJournalCode")
    @Mapping(target = "postingDate", source = "journalDate")
    @Mapping(target = "totalDebit", source = "lines", qualifiedByName = "calcTotalDebit")
    @Mapping(target = "totalCredit", source = "lines", qualifiedByName = "calcTotalCredit")
    public abstract JournalEntryListResponse toListResponse(JournalEntry domain);

    @Mapping(target = "journalCode", source = "id", qualifiedByName = "formatJournalCode")
    @Mapping(target = "postingDate", source = "journalDate")
    @Mapping(target = "totalDebit", source = "lines", qualifiedByName = "calcTotalDebit")
    @Mapping(target = "totalCredit", source = "lines", qualifiedByName = "calcTotalCredit")
    @Mapping(target = "hasMultiCurrencyLines", source = "lines", qualifiedByName = "hasMultiCurrencyLines")
    public abstract JournalEntryDetailResponse toDetailResponse(JournalEntry domain);

    public JournalEntryDetailResponse toDetailResponse(JournalEntryDetailView view) {
        JournalEntryDetailResponse response = toDetailResponse(view.entry());
        response.setReversedById(view.reversedById());
        response.setReversedByCode(formatJournalCode(view.reversedById()));
        response.setReversed(view.reversedById() != null);
        return response;
    }

    @Mapping(target = "accountCode", source = "accountId", qualifiedByName = "resolveAccountCode")
    @Mapping(target = "accountName", source = "accountId", qualifiedByName = "resolveAccountName")
    @Mapping(target = "originalCurrencyCode", source = "originalCurrencyId", qualifiedByName = "resolveCurrencyCode")
    public abstract JournalLineResponse toLineResponse(JournalLine line);

    public ManualJournalCommand toManualJournalCommand(JournalEntrySaveRequest request) {
        List<ManualJournalLineCommand> lines = request.getLines() == null ? List.of() : request.getLines().stream()
                .map(line -> new ManualJournalLineCommand(
                        line.getAccountId(),
                        line.getDebitAmount(),
                        line.getCreditAmount(),
                        line.getDescription()
                ))
                .toList();
        return new ManualJournalCommand(request.getPostingDate(), request.getCurrencyId(), request.getExchangeRate(),
                request.getReferenceNo(), request.getDescription(), lines);
    }

    public JournalEntrySaveRequest toSaveRequest(JournalEntry entry) {
        JournalEntrySaveRequest request = new JournalEntrySaveRequest();
        request.setId(entry.getId());
        request.setVersion(entry.getMetadata() != null && entry.getMetadata().version() != null
                ? entry.getMetadata().version().intValue() : null);
        request.setPostingDate(entry.getJournalDate());
        request.setCurrencyId(entry.getCurrencyId());
        request.setExchangeRate(entry.getExchangeRate());
        request.setReferenceNo(entry.getReferenceNo());
        request.setDescription(entry.getDescription());
        request.setLines(entry.getLines().stream().map(this::toLineSaveRequest).toList());
        return request;
    }

    @AfterMapping
    protected void mapAuditFields(JournalEntry domain, @MappingTarget BaseAuditResponse target) {
        target.setId(domain.getId());
        if (domain.getMetadata() != null) {
            target.setVersion(domain.getMetadata().version() != null ? domain.getMetadata().version().intValue() : null);
            target.setCreatedDate(domain.getMetadata().createdDate());
            target.setUpdatedDate(domain.getMetadata().updatedDate());
            target.setCreatedByName(auditMapperHelper.resolveUserDisplayName(domain.getMetadata().createdBy()));
            target.setUpdatedByName(auditMapperHelper.resolveUserDisplayName(domain.getMetadata().updatedBy()));
        }
    }

    @AfterMapping
    protected void enrichDetail(JournalEntry domain, @MappingTarget JournalEntryDetailResponse target) {
        target.setManual(domain.isManual());
        target.setReversal(domain.isReversal());
        target.setMultiCurrency(hasMultiCurrencyLines(domain.getLines()));
        target.setCurrencyName(resolveCurrencyName(domain.getCurrencyId()));
        target.setCurrencyCode(resolveCurrencyCode(domain.getCurrencyId()));
        target.setReversalOfCode(formatJournalCode(domain.getReversalOfId()));
        target.setTotalOriginalDebit(calcTotalOriginalDebit(domain.getLines()));
        target.setTotalOriginalCredit(calcTotalOriginalCredit(domain.getLines()));
    }

    @Named("formatJournalCode")
    protected String formatJournalCode(Long id) {
        return id != null ? String.format("JNL-%06d", id) : null;
    }

    @Named("calcTotalDebit")
    protected BigDecimal calcTotalDebit(List<JournalLine> lines) {
        if (lines == null) {
            return BigDecimal.ZERO;
        }
        return lines.stream().map(JournalLine::debitAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Named("calcTotalCredit")
    protected BigDecimal calcTotalCredit(List<JournalLine> lines) {
        if (lines == null) {
            return BigDecimal.ZERO;
        }
        return lines.stream().map(JournalLine::creditAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    protected BigDecimal calcTotalOriginalDebit(List<JournalLine> lines) {
        if (lines == null) {
            return BigDecimal.ZERO;
        }
        return lines.stream()
                .map(line -> line.originalDebitAmount() == null ? BigDecimal.ZERO : line.originalDebitAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    protected BigDecimal calcTotalOriginalCredit(List<JournalLine> lines) {
        if (lines == null) {
            return BigDecimal.ZERO;
        }
        return lines.stream()
                .map(line -> line.originalCreditAmount() == null ? BigDecimal.ZERO : line.originalCreditAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Named("hasMultiCurrencyLines")
    protected boolean hasMultiCurrencyLines(List<JournalLine> lines) {
        return lines != null && lines.stream().anyMatch(line -> line.originalCurrencyId() != null);
    }

    @Named("resolveAccountCode")
    protected String resolveAccountCode(Long accountId) {
        return resolveAccount(accountId).map(LookupDto::subText).orElse(null);
    }

    @Named("resolveAccountName")
    protected String resolveAccountName(Long accountId) {
        return resolveAccount(accountId).map(LookupDto::name).orElse(null);
    }

    @Named("resolveCurrencyCode")
    protected String resolveCurrencyCode(Long currencyId) {
        if (currencyId == null || currencyLookupProvider == null) {
            return null;
        }
        LookupDto lookup = currencyLookupProvider.resolve(currencyId);
        return lookup != null ? lookup.subText() : null;
    }

    protected String resolveCurrencyName(Long currencyId) {
        if (currencyId == null || currencyLookupProvider == null) {
            return null;
        }
        LookupDto lookup = currencyLookupProvider.resolve(currencyId);
        return lookup != null ? lookup.name() : null;
    }

    private JournalLineSaveRequest toLineSaveRequest(JournalLine line) {
        JournalLineSaveRequest request = new JournalLineSaveRequest();
        request.setAccountId(line.accountId());
        request.setAccountName(resolveAccountName(line.accountId()));
        request.setAccountCode(resolveAccountCode(line.accountId()));
        request.setDebitAmount(line.originalDebitAmount() != null ? line.originalDebitAmount() : line.debitAmount());
        request.setCreditAmount(line.originalCreditAmount() != null ? line.originalCreditAmount() : line.creditAmount());
        request.setDescription(line.description());
        return request;
    }

    private Optional<LookupDto> resolveAccount(Long accountId) {
        if (accountId == null || coaLookupProvider == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(coaLookupProvider.resolve(accountId));
    }
}
