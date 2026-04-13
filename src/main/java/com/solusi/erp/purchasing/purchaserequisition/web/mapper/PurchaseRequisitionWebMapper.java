package com.solusi.erp.purchasing.purchaserequisition.web.mapper;

import com.solusi.erp.core.dto.BaseAuditResponse;
import com.solusi.erp.core.mapper.AuditMapperHelper;
import com.solusi.erp.inventory.facility.infrastructure.persistence.FacilityJpaRepository;
import com.solusi.erp.inventory.product.infrastructure.persistence.JpaProductRepository;
import com.solusi.erp.inventory.uom.infrastructure.persistence.UomJpaRepository;
import com.solusi.erp.master.currency.infrastructure.persistence.CurrencyJpaRepository;
import com.solusi.erp.master.party.infrastructure.persistence.PartyJpaRepository;
import com.solusi.erp.purchasing.purchaserequisition.application.usecase.command.LineInput;
import com.solusi.erp.purchasing.purchaserequisition.domain.model.PurchaseRequisition;
import com.solusi.erp.purchasing.purchaserequisition.domain.model.PurchaseRequisitionLine;
import com.solusi.erp.purchasing.purchaserequisition.web.dto.*;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class PurchaseRequisitionWebMapper {

    @Autowired
    protected AuditMapperHelper auditMapperHelper;
    @Autowired
    protected PartyJpaRepository partyRepository;
    @Autowired
    protected JpaProductRepository productRepository;
    @Autowired
    protected UomJpaRepository uomRepository;
    @Autowired
    protected FacilityJpaRepository facilityRepository;
    @Autowired
    protected CurrencyJpaRepository currencyRepository;

    @Mapping(target = "currencyCode", source = "currencyId", qualifiedByName = "getCurrencyCode")
    @Mapping(target = "requesterName", source = "requesterId", qualifiedByName = "getRequesterName")
    public abstract PurchaseRequisitionSummaryResponse toSummaryResponse(PurchaseRequisition domain);

    @Mapping(target = "requesterName", source = "requesterId", qualifiedByName = "getRequesterName")
    @Mapping(target = "facilityName", source = "facilityId", qualifiedByName = "getFacilityName")
    @Mapping(target = "supplierName", source = "suggestedSupplierId", qualifiedByName = "getSupplierName")
    @Mapping(target = "currencyCode", source = "currencyId", qualifiedByName = "getCurrencyCode")
    public abstract PurchaseRequisitionDetailResponse toDetailResponse(PurchaseRequisition domain);

    @Mapping(target = "requesterName", source = "requesterId", qualifiedByName = "getRequesterName")
    @Mapping(target = "facilityName", source = "facilityId", qualifiedByName = "getFacilityName")
    @Mapping(target = "supplierName", source = "suggestedSupplierId", qualifiedByName = "getSupplierName")
    public abstract PurchaseRequisitionSaveRequest toSaveRequest(PurchaseRequisition domain);

    @Mapping(target = "productName", source = "productId", qualifiedByName = "getProductName")
    @Mapping(target = "uomName", source = "uomId", qualifiedByName = "getUomName")
    public abstract PurchaseRequisitionLineRequest toLineRequest(PurchaseRequisitionLine line);

    @Mapping(target = "productName", source = "productId", qualifiedByName = "getProductName")
    @Mapping(target = "uomName", source = "uomId", qualifiedByName = "getUomName")
    public abstract PurchaseRequisitionLineResponse toLineResponse(PurchaseRequisitionLine line);

    public abstract LineInput toLineInput(PurchaseRequisitionLineRequest request);

    public List<LineInput> toLineInputs(List<PurchaseRequisitionLineRequest> requests) {
        if (requests == null) return List.of();
        return requests.stream().map(this::toLineInput).toList();
    }

    @AfterMapping
    protected void mapAuditFields(PurchaseRequisition domain, @MappingTarget BaseAuditResponse target) {
        if (domain.getMetadata() != null) {
            target.setId(domain.getId());
            target.setVersion(domain.getMetadata().version() != null ? domain.getMetadata().version().intValue() : null);
            target.setCreatedDate(domain.getMetadata().createdDate());
            target.setUpdatedDate(domain.getMetadata().updatedDate());
            target.setCreatedByName(auditMapperHelper.resolveUserDisplayName(domain.getMetadata().createdBy()));
            target.setUpdatedByName(auditMapperHelper.resolveUserDisplayName(domain.getMetadata().updatedBy()));
        }
    }

    @Named("getRequesterName")
    protected String getRequesterName(Long id) {
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

    @Named("getSupplierName")
    protected String getSupplierName(Long id) {
        if (id == null) return null;
        return partyRepository.findById(id).map(p -> {
            String sal = p.getSalutation();
            return (sal != null && !sal.isBlank()) ? sal + " " + p.getName() : p.getName();
        }).orElse(null);
    }

    @Named("getCurrencyCode")
    protected String getCurrencyCode(Long id) {
        if (id == null) return null;
        return currencyRepository.findById(id).map(c -> c.getAlias()).orElse(null);
    }
}
