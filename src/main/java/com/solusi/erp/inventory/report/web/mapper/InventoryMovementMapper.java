package com.solusi.erp.inventory.report.web.mapper;

import com.solusi.erp.core.mapper.AuditMapperHelper;
import com.solusi.erp.inventory.container.infrastructure.persistence.ContainerJpaRepository;
import com.solusi.erp.inventory.facility.infrastructure.persistence.FacilityJpaRepository;
import com.solusi.erp.inventory.grid.infrastructure.persistence.GridJpaRepository;
import com.solusi.erp.inventory.product.infrastructure.persistence.JpaProductRepository;
import com.solusi.erp.inventory.report.web.dto.InventoryMovementResponse;
import com.solusi.erp.inventory.stock.infrastructure.persistence.InventoryMovementEntity;
import com.solusi.erp.master.currency.domain.repository.CurrencyRepository;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", uses = {AuditMapperHelper.class})
public abstract class InventoryMovementMapper {

    @Autowired
    protected CurrencyRepository currencyRepository;

    @Autowired
    protected JpaProductRepository productRepository;

    @Autowired
    protected ContainerJpaRepository containerJpaRepository;

    @Autowired
    protected GridJpaRepository gridJpaRepository;

    @Autowired
    protected FacilityJpaRepository facilityJpaRepository;

    @Mapping(target = "productCode", ignore = true)
    @Mapping(target = "productName", ignore = true)
    @Mapping(target = "containerCode", ignore = true)
    @Mapping(target = "facilityName", ignore = true)
    @Mapping(target = "unitCostOriginal", source = "unitCost.originalAmount")
    @Mapping(target = "unitCostLocal", source = "unitCost.localAmount")
    @Mapping(target = "currencyAlias", source = "unitCost.currencyId", qualifiedByName = "resolveCurrencyAlias")
    public abstract InventoryMovementResponse toResponse(InventoryMovementEntity entity);

    @AfterMapping
    protected void enrichResponse(InventoryMovementEntity entity, @MappingTarget InventoryMovementResponse response) {
        if (entity.getProductId() != null) {
            productRepository.findById(entity.getProductId()).ifPresent(p -> {
                response.setProductCode(p.getCode());
                response.setProductName(p.getName());
            });
        }
        if (entity.getContainerId() != null) {
            containerJpaRepository.findById(entity.getContainerId()).ifPresent(c -> {
                response.setContainerCode(c.getCode());
                if (c.getGridId() != null) {
                    gridJpaRepository.findById(c.getGridId()).ifPresent(g -> {
                        if (g.getFacilityId() != null) {
                            facilityJpaRepository.findById(g.getFacilityId())
                                    .ifPresent(f -> response.setFacilityName(f.getName()));
                        }
                    });
                }
            });
        }
    }

    @Named("resolveCurrencyAlias")
    protected String resolveCurrencyAlias(Long currencyId) {
        if (currencyId == null) {
            return null;
        }
        return currencyRepository.findById(currencyId).map(c -> c.getAlias()).orElse(null);
    }
}
