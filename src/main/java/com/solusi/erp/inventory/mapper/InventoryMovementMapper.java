package com.solusi.erp.inventory.mapper;

import com.solusi.erp.core.mapper.AuditMapperHelper;
import com.solusi.erp.inventory.dto.InventoryMovementResponse;
import com.solusi.erp.inventory.model.InventoryMovement;
import com.solusi.erp.master.currency.domain.repository.CurrencyRepository;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", uses = {AuditMapperHelper.class})
public abstract class InventoryMovementMapper {

    @Autowired
    protected CurrencyRepository currencyRepository;

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productCode", source = "product.code")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "containerId", source = "container.id")
    @Mapping(target = "containerCode", source = "container.code")
    @Mapping(target = "facilityName", source = "container.grid.facility.name")
    @Mapping(target = "unitCostOriginal", source = "unitCost.originalAmount")
    @Mapping(target = "unitCostLocal", source = "unitCost.localAmount")
    @Mapping(target = "currencyAlias", source = "unitCost.currencyId", qualifiedByName = "resolveCurrencyAlias")
    public abstract InventoryMovementResponse toResponse(InventoryMovement entity);

    @Named("resolveCurrencyAlias")
    protected String resolveCurrencyAlias(Long currencyId) {
        if (currencyId == null) {
            return null;
        }
        return currencyRepository.findById(currencyId).map(c -> c.getAlias()).orElse(null);
    }
}
