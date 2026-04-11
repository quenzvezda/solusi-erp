package com.solusi.erp.accounting.coa.infrastructure.persistence;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.accounting.coa.domain.model.AccountType;
import com.solusi.erp.accounting.coa.domain.model.NormalBalance;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CoaPersistenceMapper {

    @Mapping(target = "metadata", expression = "java(toAuditMetadata(entity))")
    @Mapping(target = "accountType", source = "accountType", qualifiedByName = "toAccountType")
    @Mapping(target = "normalBalance", source = "normalBalance", qualifiedByName = "toNormalBalance")
    com.solusi.erp.accounting.coa.domain.model.ChartOfAccount toDomain(ChartOfAccount entity);

    @Mapping(target = "id", source = "metadata.id")
    @Mapping(target = "version", expression = "java(domain.getMetadata().version() != null ? domain.getMetadata().version().intValue() : null)")
    @Mapping(target = "createdDate", source = "metadata.createdDate")
    @Mapping(target = "createdBy", source = "metadata.createdBy")
    @Mapping(target = "updatedDate", source = "metadata.updatedDate")
    @Mapping(target = "updatedBy", source = "metadata.updatedBy")
    @Mapping(target = "accountType", source = "accountType", qualifiedByName = "accountTypeToString")
    @Mapping(target = "normalBalance", source = "normalBalance", qualifiedByName = "normalBalanceToString")
    ChartOfAccount toEntity(com.solusi.erp.accounting.coa.domain.model.ChartOfAccount domain);

    default AuditMetadata toAuditMetadata(ChartOfAccount entity) {
        return new AuditMetadata(
                entity.getId(),
                entity.getVersion() != null ? entity.getVersion().longValue() : null,
                entity.getCreatedDate(),
                entity.getCreatedBy(),
                entity.getUpdatedDate(),
                entity.getUpdatedBy()
        );
    }

    @Named("toAccountType")
    default AccountType toAccountType(String value) {
        return value != null ? AccountType.valueOf(value) : null;
    }

    @Named("toNormalBalance")
    default NormalBalance toNormalBalance(String value) {
        return value != null ? NormalBalance.valueOf(value) : null;
    }

    @Named("accountTypeToString")
    default String accountTypeToString(AccountType value) {
        return value != null ? value.name() : null;
    }

    @Named("normalBalanceToString")
    default String normalBalanceToString(NormalBalance value) {
        return value != null ? value.name() : null;
    }
}
