package com.solusi.erp.accounting.schema.web.mapper;

import com.solusi.erp.core.dto.BaseAuditResponse;
import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.core.mapper.AuditMapperHelper;
import com.solusi.erp.accounting.coa.domain.port.CoaLookupProvider;
import com.solusi.erp.accounting.schema.domain.model.AccountingSchema;
import com.solusi.erp.accounting.schema.web.dto.SchemaDetailResponse;
import com.solusi.erp.accounting.schema.web.dto.SchemaSaveRequest;
import com.solusi.erp.accounting.schema.web.dto.SchemaSummaryResponse;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class SchemaWebMapper {

    @Autowired
    protected AuditMapperHelper auditMapperHelper;

    @Autowired
    protected CoaLookupProvider coaLookupProvider;

    @Mapping(target = "eventType", expression = "java(domain.getEventType() != null ? domain.getEventType().name() : null)")
    @Mapping(target = "debitAccountName", ignore = true)
    @Mapping(target = "creditAccountName", ignore = true)
    @Mapping(target = "taxAccountName", ignore = true)
    public abstract SchemaSummaryResponse toSummaryResponse(AccountingSchema domain);

    @Mapping(target = "eventType", expression = "java(domain.getEventType() != null ? domain.getEventType().name() : null)")
    @Mapping(target = "debitAccountName", ignore = true)
    @Mapping(target = "creditAccountName", ignore = true)
    @Mapping(target = "taxAccountName", ignore = true)
    public abstract SchemaDetailResponse toDetailResponse(AccountingSchema domain);

    @Mapping(target = "eventType", expression = "java(domain.getEventType() != null ? domain.getEventType().name() : null)")
    public abstract SchemaSaveRequest toSaveRequest(AccountingSchema domain);

    @AfterMapping
    protected void mapAuditFields(AccountingSchema domain, @MappingTarget BaseAuditResponse target) {
        if (domain.getMetadata() != null) {
            target.setId(domain.getId());
            target.setVersion(domain.getMetadata().version() != null ? domain.getMetadata().version().intValue() : null);
            target.setCreatedDate(domain.getMetadata().createdDate());
            target.setUpdatedDate(domain.getMetadata().updatedDate());
            target.setCreatedByName(auditMapperHelper.resolveUserDisplayName(domain.getMetadata().createdBy()));
            target.setUpdatedByName(auditMapperHelper.resolveUserDisplayName(domain.getMetadata().updatedBy()));
        }
    }

    @AfterMapping
    protected void resolveAccountNames(AccountingSchema domain, @MappingTarget SchemaSummaryResponse target) {
        resolveNames(domain, target);
    }

    @AfterMapping
    protected void resolveAccountNames(AccountingSchema domain, @MappingTarget SchemaDetailResponse target) {
        resolveNames(domain, target);
    }

    @AfterMapping
    protected void resolveAccountNames(AccountingSchema domain, @MappingTarget SchemaSaveRequest target) {
        LookupDto debit = coaLookupProvider.resolve(domain.getDebitAccountId());
        LookupDto credit = coaLookupProvider.resolve(domain.getCreditAccountId());
        LookupDto tax = coaLookupProvider.resolve(domain.getTaxAccountId());
        target.setDebitAccountName(formatLookup(debit));
        target.setCreditAccountName(formatLookup(credit));
        target.setTaxAccountName(formatLookup(tax));
    }

    private void resolveNames(AccountingSchema domain, Object target) {
        LookupDto debit = coaLookupProvider.resolve(domain.getDebitAccountId());
        LookupDto credit = coaLookupProvider.resolve(domain.getCreditAccountId());
        LookupDto tax = coaLookupProvider.resolve(domain.getTaxAccountId());
        if (target instanceof SchemaSummaryResponse s) {
            s.setDebitAccountName(formatLookup(debit));
            s.setCreditAccountName(formatLookup(credit));
            s.setTaxAccountName(formatLookup(tax));
        } else if (target instanceof SchemaDetailResponse d) {
            d.setDebitAccountName(formatLookup(debit));
            d.setCreditAccountName(formatLookup(credit));
            d.setTaxAccountName(formatLookup(tax));
        }
    }

    private String formatLookup(LookupDto lookup) {
        if (lookup == null) return null;
        if (lookup.subText() == null || lookup.subText().isBlank()) return lookup.name();
        if (lookup.name() == null || lookup.name().isBlank()) return lookup.subText();
        return lookup.subText() + " - " + lookup.name();
    }
}
