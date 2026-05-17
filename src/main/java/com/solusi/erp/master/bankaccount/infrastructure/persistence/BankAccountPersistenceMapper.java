package com.solusi.erp.master.bankaccount.infrastructure.persistence;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.master.bankaccount.domain.model.BankAccount;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BankAccountPersistenceMapper {

    @Mapping(target = "metadata", expression = "java(toAuditMetadata(entity))")
    @Mapping(target = "cityId", expression = "java(entity.getCity() != null ? entity.getCity().getId() : null)")
    @Mapping(target = "cityName", expression = "java(entity.getCity() != null ? entity.getCity().getName() : null)")
    @Mapping(target = "partyId", expression = "java(entity.getParty() != null ? entity.getParty().getId() : null)")
    @Mapping(target = "partyName", expression = "java(entity.getParty() != null ? entity.getParty().getName() : null)")
    BankAccount toDomain(com.solusi.erp.master.bankaccount.infrastructure.persistence.BankAccount entity);

    @Mapping(target = "id", source = "metadata.id")
    @Mapping(target = "version", source = "metadata.version")
    @Mapping(target = "createdDate", source = "metadata.createdDate")
    @Mapping(target = "createdBy", source = "metadata.createdBy")
    @Mapping(target = "updatedDate", source = "metadata.updatedDate")
    @Mapping(target = "updatedBy", source = "metadata.updatedBy")
    @Mapping(target = "city", ignore = true)
    @Mapping(target = "party", ignore = true)
    com.solusi.erp.master.bankaccount.infrastructure.persistence.BankAccount toEntity(BankAccount domain);

    default AuditMetadata toAuditMetadata(com.solusi.erp.master.bankaccount.infrastructure.persistence.BankAccount entity) {
        return new AuditMetadata(
            entity.getId(),
            entity.getVersion() != null ? entity.getVersion().longValue() : null,
            entity.getCreatedDate(),
            entity.getCreatedBy(),
            entity.getUpdatedDate(),
            entity.getUpdatedBy()
        );
    }
}

