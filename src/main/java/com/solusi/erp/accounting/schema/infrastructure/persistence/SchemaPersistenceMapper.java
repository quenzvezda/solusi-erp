package com.solusi.erp.accounting.schema.infrastructure.persistence;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.accounting.schema.domain.model.SchemaEventType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SchemaPersistenceMapper {

    @Mapping(target = "metadata", expression = "java(toAuditMetadata(entity))")
    @Mapping(target = "eventType", source = "eventType", qualifiedByName = "toSchemaEventType")
    com.solusi.erp.accounting.schema.domain.model.AccountingSchema toDomain(AccountingSchema entity);

    @Mapping(target = "id", source = "metadata.id")
    @Mapping(target = "version", expression = "java(domain.getMetadata().version() != null ? domain.getMetadata().version().intValue() : null)")
    @Mapping(target = "createdDate", source = "metadata.createdDate")
    @Mapping(target = "createdBy", source = "metadata.createdBy")
    @Mapping(target = "updatedDate", source = "metadata.updatedDate")
    @Mapping(target = "updatedBy", source = "metadata.updatedBy")
    @Mapping(target = "eventType", source = "eventType", qualifiedByName = "eventTypeToString")
    AccountingSchema toEntity(com.solusi.erp.accounting.schema.domain.model.AccountingSchema domain);

    default AuditMetadata toAuditMetadata(AccountingSchema entity) {
        return new AuditMetadata(
                entity.getId(),
                entity.getVersion() != null ? entity.getVersion().longValue() : null,
                entity.getCreatedDate(),
                entity.getCreatedBy(),
                entity.getUpdatedDate(),
                entity.getUpdatedBy()
        );
    }

    @Named("toSchemaEventType")
    default SchemaEventType toSchemaEventType(String value) {
        return value != null ? SchemaEventType.valueOf(value) : null;
    }

    @Named("eventTypeToString")
    default String eventTypeToString(SchemaEventType value) {
        return value != null ? value.name() : null;
    }
}
