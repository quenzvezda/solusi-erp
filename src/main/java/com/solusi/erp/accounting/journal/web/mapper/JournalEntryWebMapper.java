package com.solusi.erp.accounting.journal.web.mapper;

import com.solusi.erp.accounting.coa.domain.port.CoaLookupProvider;
import com.solusi.erp.accounting.journal.domain.model.JournalEntry;
import com.solusi.erp.accounting.journal.domain.model.JournalLine;
import com.solusi.erp.accounting.journal.web.dto.JournalEntryDetailResponse;
import com.solusi.erp.accounting.journal.web.dto.JournalEntryListResponse;
import com.solusi.erp.accounting.journal.web.dto.JournalLineResponse;
import com.solusi.erp.core.dto.BaseAuditResponse;
import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.core.mapper.AuditMapperHelper;
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

    @Mapping(target = "journalCode", source = "id", qualifiedByName = "formatJournalCode")
    @Mapping(target = "postingDate", source = "journalDate")
    @Mapping(target = "totalDebit", source = "lines", qualifiedByName = "calcTotalDebit")
    @Mapping(target = "totalCredit", source = "lines", qualifiedByName = "calcTotalCredit")
    public abstract JournalEntryListResponse toListResponse(JournalEntry domain);

    @Mapping(target = "journalCode", source = "id", qualifiedByName = "formatJournalCode")
    @Mapping(target = "postingDate", source = "journalDate")
    @Mapping(target = "totalDebit", source = "lines", qualifiedByName = "calcTotalDebit")
    @Mapping(target = "totalCredit", source = "lines", qualifiedByName = "calcTotalCredit")
    public abstract JournalEntryDetailResponse toDetailResponse(JournalEntry domain);

    @Mapping(target = "accountCode", source = "accountId", qualifiedByName = "resolveAccountCode")
    @Mapping(target = "accountName", source = "accountId", qualifiedByName = "resolveAccountName")
    public abstract JournalLineResponse toLineResponse(JournalLine line);

    @AfterMapping
    protected void mapAuditFields(JournalEntry domain, @MappingTarget BaseAuditResponse target) {
        if (domain.getMetadata() != null) {
            target.setId(domain.getId());
            target.setVersion(domain.getMetadata().version() != null ? domain.getMetadata().version().intValue() : null);
            target.setCreatedDate(domain.getMetadata().createdDate());
            target.setUpdatedDate(domain.getMetadata().updatedDate());
            target.setCreatedByName(auditMapperHelper.resolveUserDisplayName(domain.getMetadata().createdBy()));
            target.setUpdatedByName(auditMapperHelper.resolveUserDisplayName(domain.getMetadata().updatedBy()));
        }
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

    @Named("resolveAccountCode")
    protected String resolveAccountCode(Long accountId) {
        return resolveAccount(accountId).map(LookupDto::subText).orElse(null);
    }

    @Named("resolveAccountName")
    protected String resolveAccountName(Long accountId) {
        return resolveAccount(accountId).map(LookupDto::name).orElse(null);
    }

    private Optional<LookupDto> resolveAccount(Long accountId) {
        if (accountId == null || coaLookupProvider == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(coaLookupProvider.resolve(accountId));
    }
}
