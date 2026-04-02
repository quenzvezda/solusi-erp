package com.solusi.erp.inventory.stock.infrastructure.persistence;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.model.CurrencyAmount;
import com.solusi.erp.inventory.stock.domain.model.CostAmount;
import com.solusi.erp.inventory.stock.domain.model.ValuationLayer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ValuationLayerPersistenceMapper {

    @Mapping(target = "metadata", expression = "java(toAuditMetadata(entity))")
    @Mapping(target = "unitCost", source = "unitCost", qualifiedByName = "toCostAmount")
    ValuationLayer toDomain(ValuationLayerEntity entity);

    @Mapping(target = "id", source = "metadata.id")
    @Mapping(target = "version", expression = "java(domain.getMetadata().version() != null ? domain.getMetadata().version().intValue() : null)")
    @Mapping(target = "createdDate", source = "metadata.createdDate")
    @Mapping(target = "createdBy", source = "metadata.createdBy")
    @Mapping(target = "updatedDate", source = "metadata.updatedDate")
    @Mapping(target = "updatedBy", source = "metadata.updatedBy")
    @Mapping(target = "unitCost", source = "unitCost", qualifiedByName = "toCurrencyAmount")
    ValuationLayerEntity toEntity(ValuationLayer domain);

    default AuditMetadata toAuditMetadata(ValuationLayerEntity entity) {
        return new AuditMetadata(
            entity.getId(),
            entity.getVersion() != null ? entity.getVersion().longValue() : null,
            entity.getCreatedDate(),
            entity.getCreatedBy(),
            entity.getUpdatedDate(),
            entity.getUpdatedBy()
        );
    }

    @Named("toCostAmount")
    default CostAmount toCostAmount(CurrencyAmount ca) {
        if (ca == null) return null;
        return new CostAmount(ca.getCurrencyId(), ca.getExchangeRate(),
                ca.getOriginalAmount(), ca.getLocalAmount());
    }

    @Named("toCurrencyAmount")
    default CurrencyAmount toCurrencyAmount(CostAmount cost) {
        if (cost == null) return null;
        return CurrencyAmount.builder()
                .currencyId(cost.currencyId())
                .exchangeRate(cost.exchangeRate())
                .originalAmount(cost.originalAmount())
                .localAmount(cost.localAmount())
                .build();
    }
}
