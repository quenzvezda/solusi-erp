package com.solusi.erp.accounting.period.infrastructure.persistence;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.accounting.period.domain.model.PeriodStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PeriodPersistenceMapper {

    @Mapping(target = "metadata", expression = "java(toAuditMetadata(entity))")
    @Mapping(target = "status", source = "status", qualifiedByName = "toPeriodStatus")
    @Mapping(target = "periodNumber", expression = "java(entity.getPeriodNumber() != null ? entity.getPeriodNumber() : 0)")
    com.solusi.erp.accounting.period.domain.model.AccountingPeriod toDomain(AccountingPeriod entity);

    @Mapping(target = "id", source = "metadata.id")
    @Mapping(target = "version", expression = "java(domain.getMetadata().version() != null ? domain.getMetadata().version().intValue() : null)")
    @Mapping(target = "createdDate", source = "metadata.createdDate")
    @Mapping(target = "createdBy", source = "metadata.createdBy")
    @Mapping(target = "updatedDate", source = "metadata.updatedDate")
    @Mapping(target = "updatedBy", source = "metadata.updatedBy")
    @Mapping(target = "status", source = "status", qualifiedByName = "statusToString")
    @Mapping(target = "periodNumber", source = "periodNumber")
    AccountingPeriod toEntity(com.solusi.erp.accounting.period.domain.model.AccountingPeriod domain);

    default AuditMetadata toAuditMetadata(AccountingPeriod entity) {
        return new AuditMetadata(
                entity.getId(),
                entity.getVersion() != null ? entity.getVersion().longValue() : null,
                entity.getCreatedDate(),
                entity.getCreatedBy(),
                entity.getUpdatedDate(),
                entity.getUpdatedBy()
        );
    }

    @Named("toPeriodStatus")
    default PeriodStatus toPeriodStatus(String value) {
        return value != null ? PeriodStatus.valueOf(value) : null;
    }

    @Named("statusToString")
    default String statusToString(PeriodStatus value) {
        return value != null ? value.name() : null;
    }
}
