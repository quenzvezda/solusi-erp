package com.solusi.erp.accounting.journal.web.mapper;

import com.solusi.erp.accounting.journal.domain.model.JournalEntry;
import com.solusi.erp.accounting.journal.domain.model.JournalLine;
import com.solusi.erp.accounting.journal.web.dto.JournalEntryDetailResponse;
import com.solusi.erp.accounting.journal.web.dto.JournalEntryListResponse;
import com.solusi.erp.accounting.journal.web.dto.JournalLineResponse;
import com.solusi.erp.core.dto.BaseAuditResponse;
import com.solusi.erp.core.mapper.AuditMapperHelper;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;

@Mapper(componentModel = "spring")
public abstract class JournalEntryWebMapper {

    @Autowired
    protected AuditMapperHelper auditMapperHelper;

    @Mapping(target = "journalCode", source = "id", qualifiedByName = "formatJournalCode")
    @Mapping(target = "totalDebit", source = "lines", qualifiedByName = "calcTotalDebit")
    @Mapping(target = "totalCredit", source = "lines", qualifiedByName = "calcTotalCredit")
    public abstract JournalEntryListResponse toListResponse(JournalEntry domain);

    @Mapping(target = "journalCode", source = "id", qualifiedByName = "formatJournalCode")
    @Mapping(target = "totalDebit", source = "lines", qualifiedByName = "calcTotalDebit")
    @Mapping(target = "totalCredit", source = "lines", qualifiedByName = "calcTotalCredit")
    public abstract JournalEntryDetailResponse toDetailResponse(JournalEntry domain);

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
        if (lines == null) return BigDecimal.ZERO;
        return lines.stream().map(JournalLine::debitAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Named("calcTotalCredit")
    protected BigDecimal calcTotalCredit(List<JournalLine> lines) {
        if (lines == null) return BigDecimal.ZERO;
        return lines.stream().map(JournalLine::creditAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}