package com.solusi.erp.purchasing.supplierpricelist.web.mapper;

import com.solusi.erp.core.dto.BaseAuditResponse;
import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.core.mapper.AuditMapperHelper;
import com.solusi.erp.inventory.product.domain.port.ProductLookupProvider;
import com.solusi.erp.inventory.uom.domain.port.UomLookupProvider;
import com.solusi.erp.master.currency.domain.port.CurrencyLookupProvider;
import com.solusi.erp.master.party.domain.port.PartyLookupProvider;
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
    protected PartyLookupProvider partyLookupProvider;
    @Autowired
    protected ProductLookupProvider productLookupProvider;
    @Autowired
    protected UomLookupProvider uomLookupProvider;
    @Autowired
    protected CurrencyLookupProvider currencyLookupProvider;

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
        LookupDto dto = partyLookupProvider.resolve(id);
        return dto != null ? dto.name() : null;
    }

    @Named("getProductName")
    protected String getProductName(Long id) {
        LookupDto dto = productLookupProvider.resolve(id);
        return dto != null ? dto.name() : null;
    }

    @Named("getUomName")
    protected String getUomName(Long id) {
        LookupDto dto = uomLookupProvider.resolve(id);
        return dto != null ? dto.name() : null;
    }

    @Named("getCurrencyName")
    protected String getCurrencyName(Long id) {
        LookupDto dto = currencyLookupProvider.resolve(id);
        return dto != null ? dto.name() : null;
    }

    @Named("getCurrencySymbol")
    protected String getCurrencySymbol(Long id) {
        LookupDto dto = currencyLookupProvider.resolve(id);
        if (dto == null || dto.payload() == null) return null;
        return (String) dto.payload().get("symbol");
    }
}
