package com.solusi.erp.inventory.mapper;

import com.solusi.erp.core.mapper.AuditMapperHelper;
import com.solusi.erp.inventory.dto.InventoryMovementResponse;
import com.solusi.erp.inventory.model.InventoryMovement;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {AuditMapperHelper.class})
public interface InventoryMovementMapper {

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productCode", source = "product.code")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "containerId", source = "container.id")
    @Mapping(target = "containerCode", source = "container.code")
    @Mapping(target = "facilityName", source = "container.grid.facility.name")
    @Mapping(target = "unitCostOriginal", source = "unitCost.originalAmount")
    @Mapping(target = "unitCostLocal", source = "unitCost.localAmount")
    @Mapping(target = "currencyAlias", source = "unitCost.currency.alias")
    InventoryMovementResponse toResponse(InventoryMovement entity);
}
