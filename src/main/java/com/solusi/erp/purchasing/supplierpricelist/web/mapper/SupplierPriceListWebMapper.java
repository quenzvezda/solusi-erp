package com.solusi.erp.purchasing.supplierpricelist.web.mapper;

import com.solusi.erp.core.dto.BaseAuditResponse;
import com.solusi.erp.core.mapper.AuditMapperHelper;
import com.solusi.erp.inventory.product.infrastructure.persistence.JpaProductRepository;
import com.solusi.erp.inventory.uom.infrastructure.persistence.UomJpaRepository;
import com.solusi.erp.master.currency.infrastructure.persistence.CurrencyJpaRepository;
import com.solusi.erp.master.party.infrastructure.persistence.PartyJpaRepository;
import com.solusi.erp.purchasing.supplierpricelist.domain.model.SupplierPriceList;
import com.solusi.erp.purchasing.supplierpricelist.web.dto.SupplierPriceListDetailResponse;
import com.solusi.erp.purchasing.supplierpricelist.web.dto.SupplierPriceListSaveRequest;
import com.solusi.erp.purchasing.supplierpricelist.web.dto.SupplierPriceListSummaryResponse;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class SupplierPriceListWebMapper {

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

    @Mapping(target = "supplierName", source = "supplierId", qualifiedByName = "getSupplierName")
    @Mapping(target = "productName", source = "productId", qualifiedByName = "getProductName")
    @Mapping(target = "uomName", source = "uomId", qualifiedByName = "getUomName")
    @Mapping(target = "currencyName", source = "currencyId", qualifiedByName = "getCurrencyName")
    @Mapping(target = "currencySymbol", source = "currencyId", qualifiedByName = "getCurrencySymbol")
    public abstract SupplierPriceListSummaryResponse toSummaryResponse(SupplierPriceList domain);

    @Mapping(target = "supplierName", source = "supplierId", qualifiedByName = "getSupplierName")
    @Mapping(target = "productName", source = "productId", qualifiedByName = "getProductName")
    @Mapping(target = "uomName", source = "uomId", qualifiedByName = "getUomName")
    @Mapping(target = "currencyName", source = "currencyId", qualifiedByName = "getCurrencyName")
    public abstract SupplierPriceListDetailResponse toDetailResponse(SupplierPriceList domain);

    @Mapping(target = "supplierName", source = "supplierId", qualifiedByName = "getSupplierName")
    @Mapping(target = "productName", source = "productId", qualifiedByName = "getProductName")
    @Mapping(target = "uomName", source = "uomId", qualifiedByName = "getUomName")
    @Mapping(target = "currencyName", source = "currencyId", qualifiedByName = "getCurrencyName")
    public abstract SupplierPriceListSaveRequest toSaveRequest(SupplierPriceList domain);

    @AfterMapping
    protected void mapAuditFields(SupplierPriceList domain, @MappingTarget BaseAuditResponse target) {
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

    @Named("getCurrencyName")
    protected String getCurrencyName(Long id) {
        if (id == null) return null;
        return currencyRepository.findById(id).map(c -> c.getName()).orElse(null);
    }

    @Named("getCurrencySymbol")
    protected String getCurrencySymbol(Long id) {
        if (id == null) return null;
        return currencyRepository.findById(id).map(c -> c.getSymbol()).orElse(null);
    }
}
