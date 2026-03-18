package com.solusi.erp.inventory.mapper;

import com.solusi.erp.core.mapper.AuditMapperHelper;
import com.solusi.erp.inventory.dto.StockAdjustmentLineRequest;
import com.solusi.erp.inventory.dto.StockAdjustmentLineResponse;
import com.solusi.erp.inventory.dto.StockAdjustmentRequest;
import com.solusi.erp.inventory.dto.StockAdjustmentResponse;
import com.solusi.erp.inventory.model.StockAdjustment;
import com.solusi.erp.inventory.model.StockAdjustmentLine;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {AuditMapperHelper.class})
public interface StockAdjustmentMapper {

    @Mapping(target = "currencyId", source = "totalCost.currency.id")
    @Mapping(target = "currencyAlias", source = "totalCost.currency.alias")
    @Mapping(target = "exchangeRate", source = "totalCost.exchangeRate")
    @Mapping(target = "facilityId", source = "facility.id")
    @Mapping(target = "facilityName", source = "facility.name")
    @Mapping(target = "totalAmountOriginal", source = "totalCost.originalAmount")
    @Mapping(target = "totalAmountLocal", source = "totalCost.localAmount")
    StockAdjustmentResponse toResponse(StockAdjustment entity);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productCode", source = "product.code")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "gridId", source = "grid.id")
    @Mapping(target = "gridCode", source = "grid.code")
    @Mapping(target = "containerId", source = "container.id")
    @Mapping(target = "containerCode", source = "container.code")
    @Mapping(target = "facilityName", source = "container.grid.facility.name")
    StockAdjustmentLineResponse toLineResponse(StockAdjustmentLine entity);

    List<StockAdjustmentResponse> toResponseList(List<StockAdjustment> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "totalCost.currency", ignore = true)
    @Mapping(target = "totalCost.exchangeRate", source = "exchangeRate")
    @Mapping(target = "facility", ignore = true)
    @Mapping(target = "lines", ignore = true)
    StockAdjustment toEntity(StockAdjustmentRequest request);

    @Mapping(target = "facilityId", source = "facility.id")
    @Mapping(target = "facilityName", source = "facility.name")
    StockAdjustmentRequest toRequest(StockAdjustment entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "header", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "grid", ignore = true)
    @Mapping(target = "container", ignore = true)
    StockAdjustmentLine toLineEntity(StockAdjustmentLineRequest request);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productCode", source = "product.code")
    @Mapping(target = "gridId", source = "grid.id")
    @Mapping(target = "gridCode", source = "grid.code")
    @Mapping(target = "containerId", source = "container.id")
    @Mapping(target = "containerCode", source = "container.code")
    StockAdjustmentLineRequest toLineRequest(StockAdjustmentLine entity);

    @AfterMapping
    default void linkLines(@MappingTarget StockAdjustment entity) {
        if (entity.getLines() != null) {
            entity.getLines().forEach(line -> line.setHeader(entity));
        }
    }
}
