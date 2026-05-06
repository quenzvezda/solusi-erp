package com.solusi.erp.accounting.schema.web.mapper;

import com.solusi.erp.accounting.coa.domain.port.CoaLookupProvider;
import com.solusi.erp.accounting.journal.domain.model.JournalPosition;
import com.solusi.erp.accounting.journal.domain.model.JournalVariable;
import com.solusi.erp.accounting.schema.domain.model.AccountingSchema;
import com.solusi.erp.accounting.schema.domain.model.AccountingSchemaLine;
import com.solusi.erp.accounting.schema.web.dto.SchemaDetailResponse;
import com.solusi.erp.accounting.schema.web.dto.SchemaSaveRequest;
import com.solusi.erp.accounting.schema.web.dto.SchemaSummaryResponse;
import com.solusi.erp.core.dto.BaseAuditResponse;
import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.core.mapper.AuditMapperHelper;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class SchemaWebMapper {
    @Autowired
    protected AuditMapperHelper auditMapperHelper;
    @Autowired
    protected CoaLookupProvider coaLookupProvider;

    @Mapping(target = "eventType", expression = "java(domain.getEventType() != null ? domain.getEventType().name() : null)")
    public abstract SchemaSummaryResponse toSummaryResponse(AccountingSchema domain);

    @Mapping(target = "eventType", expression = "java(domain.getEventType() != null ? domain.getEventType().name() : null)")
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

    public AccountingSchemaLine toDomainLine(SchemaSaveRequest.SchemaLineRequest dto) {
        if (dto == null) {
            return null;
        }
        return new AccountingSchemaLine(
                dto.getId(),
                JournalVariable.valueOf(dto.getVariable()),
                dto.getAccountId(),
                JournalPosition.valueOf(dto.getPosition())
        );
    }

    @AfterMapping
    protected void mapLines(AccountingSchema domain, @MappingTarget SchemaSaveRequest target) {
        if (domain.getLines() != null) {
            target.setLines(domain.getLines().stream().map(this::toLineRequest).collect(Collectors.toList()));
        }
    }

    @AfterMapping
    protected void mapLines(AccountingSchema domain, @MappingTarget SchemaDetailResponse target) {
        if (domain.getLines() != null) {
            target.setLines(domain.getLines().stream().map(this::toLineRequest).collect(Collectors.toList()));
        }
    }

    @AfterMapping
    protected void mapLines(AccountingSchema domain, @MappingTarget SchemaSummaryResponse target) {
        if (domain.getLines() != null) {
            target.setLines(domain.getLines().stream().map(this::toLineRequest).collect(Collectors.toList()));
        }
    }

    protected SchemaSaveRequest.SchemaLineRequest toLineRequest(AccountingSchemaLine domainLine) {
        if (domainLine == null) {
            return null;
        }
        SchemaSaveRequest.SchemaLineRequest req = new SchemaSaveRequest.SchemaLineRequest();
        req.setId(domainLine.getId());
        req.setAccountId(domainLine.getAccountId());
        if (domainLine.getVar() != null) {
            req.setVariable(domainLine.getVar().name());
        }
        if (domainLine.getPosition() != null) {
            req.setPosition(domainLine.getPosition().name());
        }
        LookupDto lookup = coaLookupProvider.resolve(domainLine.getAccountId());
        if (lookup != null) {
            req.setAccountCode(lookup.subText());
            req.setAccountName(lookup.name());
        }
        return req;
    }
}
