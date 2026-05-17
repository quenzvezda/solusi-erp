package com.solusi.erp.accounting.schema.infrastructure.persistence;

import com.solusi.erp.accounting.journal.domain.model.JournalPosition;
import com.solusi.erp.accounting.journal.domain.model.JournalVariable;
import com.solusi.erp.accounting.schema.domain.model.AccountingSchemaLine;
import com.solusi.erp.accounting.schema.domain.model.SchemaEventType;
import com.solusi.erp.core.domain.model.AuditMetadata;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SchemaPersistenceMapper {

    public com.solusi.erp.accounting.schema.domain.model.AccountingSchema toDomain(AccountingSchema entity) {
        if (entity == null) {
            return null;
        }
        List<AccountingSchemaLine> lines = entity.getLines().stream()
                .map(this::toDomainLine)
                .collect(Collectors.toList());
        return com.solusi.erp.accounting.schema.domain.model.AccountingSchema.reconstitute(
                new AuditMetadata(
                        entity.getId(),
                        entity.getVersion() != null ? Long.valueOf(entity.getVersion()) : null,
                        entity.getCreatedDate(),
                        entity.getCreatedBy(),
                        entity.getUpdatedDate(),
                        entity.getUpdatedBy()
                ),
                entity.getEventType() != null ? SchemaEventType.valueOf(entity.getEventType()) : null,
                entity.getDescription(),
                entity.getIsActive(),
                lines
        );
    }

    public AccountingSchema toEntity(com.solusi.erp.accounting.schema.domain.model.AccountingSchema domain) {
        if (domain == null) {
            return null;
        }
        AccountingSchema entity = new AccountingSchema();
        if (domain.getMetadata() != null) {
            entity.setId(domain.getId());
            entity.setVersion(domain.getMetadata().version() != null ? domain.getMetadata().version().intValue() : null);
        }
        if (domain.getEventType() != null) {
            entity.setEventType(domain.getEventType().name());
        }
        entity.setDescription(domain.getDescription());
        entity.setIsActive(domain.getIsActive());
        if (domain.getLines() != null) {
            List<AccountingSchemaLineEntity> lineEntities = domain.getLines().stream()
                    .map(this::toEntityLine)
                    .collect(Collectors.toList());
            entity.setLines(lineEntities);
        }
        return entity;
    }

    public AccountingSchemaLine toDomainLine(AccountingSchemaLineEntity entity) {
        if (entity == null) {
            return null;
        }
        return new AccountingSchemaLine(
                entity.getId(),
                JournalVariable.valueOf(entity.getVariable()),
                entity.getAccountId(),
                JournalPosition.valueOf(entity.getPosition())
        );
    }

    public AccountingSchemaLineEntity toEntityLine(AccountingSchemaLine domain) {
        if (domain == null) {
            return null;
        }
        AccountingSchemaLineEntity entity = new AccountingSchemaLineEntity();
        entity.setId(domain.id());
        if (domain.getVar() != null) {
            entity.setVariable(domain.getVar().name());
        }
        entity.setAccountId(domain.getAccountId());
        if (domain.getPosition() != null) {
            entity.setPosition(domain.getPosition().name());
        }
        return entity;
    }
}
