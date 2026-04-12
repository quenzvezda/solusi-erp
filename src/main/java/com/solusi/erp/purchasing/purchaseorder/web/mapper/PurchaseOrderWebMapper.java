package com.solusi.erp.purchasing.purchaseorder.web.mapper;

import com.solusi.erp.core.dto.BaseAuditResponse;
import com.solusi.erp.core.mapper.AuditMapperHelper;
import com.solusi.erp.inventory.facility.infrastructure.persistence.FacilityJpaRepository;
import com.solusi.erp.inventory.product.infrastructure.persistence.JpaProductRepository;
import com.solusi.erp.inventory.uom.infrastructure.persistence.UomJpaRepository;
import com.solusi.erp.master.currency.infrastructure.persistence.CurrencyJpaRepository;
import com.solusi.erp.master.party.infrastructure.persistence.PartyJpaRepository;
import com.solusi.erp.purchasing.purchaseorder.application.usecase.command.PoLineInput;
import com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrder;
import com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrderLine;
import com.solusi.erp.purchasing.purchaseorder.web.dto.*;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class PurchaseOrderWebMapper {

    @Autowired
    protected AuditMapperHelper auditMapperHelper;
    @Autowired
    protected PartyJpaRepository partyRepository;
    @Autowired
    protected JpaProductRepository productRepository;
    @Autowired
    protected UomJpaRepository uomRepository;
    @Autowired
    protected CurrencyJpaRepository currencyRepository;
    @Autowired
    protected FacilityJpaRepository facilityRepository;

    @Mapping(target = "lineCount", expression = "java(domain.getLines() != null ? domain.getLines().size() : 0)")
    @Mapping(target = "supplierName", source = "supplierId", qualifiedByName = "getSupplierName")
    public abstract PurchaseOrderSummaryResponse toSummaryResponse(PurchaseOrder domain);

    @Mapping(target = "supplierName", source = "supplierId", qualifiedByName = "getSupplierName")
    @Mapping(target = "facilityName", source = "facilityId", qualifiedByName = "getFacilityName")
    @Mapping(target = "currencyName", source = "currencyId", qualifiedByName = "getCurrencyName")
    public abstract PurchaseOrderDetailResponse toDetailResponse(PurchaseOrder domain);

    @Mapping(target = "supplierName", source = "supplierId", qualifiedByName = "getSupplierName")
    @Mapping(target = "facilityName", source = "facilityId", qualifiedByName = "getFacilityName")
    @Mapping(target = "currencyName", source = "currencyId", qualifiedByName = "getCurrencyName")
    public abstract PurchaseOrderSaveRequest toSaveRequest(PurchaseOrder domain);

    @Mapping(target = "productName", source = "productId", qualifiedByName = "getProductName")
    @Mapping(target = "uomName", source = "uomId", qualifiedByName = "getUomName")
    public abstract PurchaseOrderLineRequest toLineRequest(PurchaseOrderLine line);

    @Mapping(target = "productName", source = "productId", qualifiedByName = "getProductName")
    @Mapping(target = "uomName", source = "uomId", qualifiedByName = "getUomName")
    public abstract PurchaseOrderLineResponse toLineResponse(PurchaseOrderLine line);

    public abstract PoLineInput toLineInput(PurchaseOrderLineRequest request);

    public List<PoLineInput> toLineInputs(List<PurchaseOrderLineRequest> requests) {
        if (requests == null) return List.of();
        return requests.stream().map(this::toLineInput).toList();
    }

    @AfterMapping
    protected void mapAuditFields(PurchaseOrder domain, @MappingTarget BaseAuditResponse target) {
        if (domain.getMetadata() != null) {
            target.setId(domain.getId());
            target.setVersion(domain.getMetadata().version() != null ? domain.getMetadata().version().intValue() : null);
            target.setCreatedDate(domain.getMetadata().createdDate());
            target.setUpdatedDate(domain.getMetadata().updatedDate());
            target.setCreatedByName(auditMapperHelper.resolveUserDisplayName(domain.getMetadata().createdBy()));
            target.setUpdatedByName(auditMapperHelper.resolveUserDisplayName(domain.getMetadata().updatedBy()));
        }
    }

    @Named("getSupplierName")
    protected String getSupplierName(Long id) {
        if (id == null) return null;
        return partyRepository.findById(id).map(p -> {
            String sal = p.getSalutation();
            return (sal != null && !sal.isBlank()) ? sal + " " + p.getName() : p.getName();
        }).orElse(null);
    }

    @Named("getFacilityName")
    protected String getFacilityName(Long id) {
        if (id == null) return null;
        return facilityRepository.findById(id).map(f -> f.getName()).orElse(null);
    }

    @Named("getCurrencyName")
    protected String getCurrencyName(Long id) {
        if (id == null) return null;
        return currencyRepository.findById(id).map(c -> c.getName()).orElse(null);
    }

    @Named("getProductName")
    protected String getProductName(Long id) {
        if (id == null) return null;
        return productRepository.findById(id).map(p -> p.getName()).orElse(null);
    }

    @Named("getUomName")
    protected String getUomName(Long id) {
        if (id == null) return null;
        return uomRepository.findById(id).map(u -> u.getName()).orElse(null);
    }
}
