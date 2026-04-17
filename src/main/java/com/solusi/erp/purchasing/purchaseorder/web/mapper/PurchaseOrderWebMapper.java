package com.solusi.erp.purchasing.purchaseorder.web.mapper;

import com.solusi.erp.core.dto.BaseAuditResponse;
import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.core.mapper.AuditMapperHelper;
import com.solusi.erp.inventory.facility.domain.port.FacilityLookupProvider;
import com.solusi.erp.inventory.product.domain.port.ProductLookupProvider;
import com.solusi.erp.inventory.uom.domain.port.UomLookupProvider;
import com.solusi.erp.master.currency.domain.port.CurrencyLookupProvider;
import com.solusi.erp.master.party.domain.port.PartyLookupProvider;
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
    protected PartyLookupProvider partyLookupProvider;
    @Autowired
    protected FacilityLookupProvider facilityLookupProvider;
    @Autowired
    protected CurrencyLookupProvider currencyLookupProvider;
    @Autowired
    protected ProductLookupProvider productLookupProvider;
    @Autowired
    protected UomLookupProvider uomLookupProvider;

    @Mapping(target = "lineCount", expression = "java(domain.getLines() != null ? domain.getLines().size() : 0)")
    @Mapping(target = "supplierName", source = "supplierId", qualifiedByName = "getSupplierName")
    public abstract PurchaseOrderSummaryResponse toSummaryResponse(PurchaseOrder domain);

    @Mapping(target = "supplierName", source = "supplierId", qualifiedByName = "getSupplierName")
    @Mapping(target = "facilityName", source = "facilityId", qualifiedByName = "getFacilityName")
    @Mapping(target = "currencyName", source = "currencyId", qualifiedByName = "getCurrencyName")
    public abstract PurchaseOrderDetailResponse toDetailResponse(PurchaseOrder domain);

    public abstract PurchaseOrderSaveRequest toSaveRequest(PurchaseOrder domain);

    @Mapping(target = "productName", source = "productId", qualifiedByName = "getProductName")
    @Mapping(target = "uomName", source = "uomId", qualifiedByName = "getUomName")
    @Mapping(target = "productSubtext", source = "productId", qualifiedByName = "getProductSubtext")
    @Mapping(target = "uomSubtext", source = "uomId", qualifiedByName = "getUomSubtext")
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
        LookupDto dto = partyLookupProvider.resolve(id);
        return dto != null ? dto.name() : null;
    }

    @Named("getFacilityName")
    protected String getFacilityName(Long id) {
        if (id == null) return null;
        LookupDto dto = facilityLookupProvider.resolve(id);
        return dto != null ? dto.name() : null;
    }

    @Named("getCurrencyName")
    protected String getCurrencyName(Long id) {
        if (id == null) return null;
        LookupDto dto = currencyLookupProvider.resolve(id);
        return dto != null ? dto.name() : null;
    }

    @Named("getProductName")
    protected String getProductName(Long id) {
        if (id == null) return null;
        LookupDto dto = productLookupProvider.resolve(id);
        return dto != null ? dto.name() : null;
    }

    @Named("getUomName")
    protected String getUomName(Long id) {
        if (id == null) return null;
        LookupDto dto = uomLookupProvider.resolve(id);
        return dto != null ? dto.name() : null;
    }

    @Named("getProductSubtext")
    protected String getProductSubtext(Long id) {
        if (id == null) return null;
        LookupDto dto = productLookupProvider.resolve(id);
        return dto != null ? dto.subText() : null;
    }

    @Named("getUomSubtext")
    protected String getUomSubtext(Long id) {
        if (id == null) return null;
        LookupDto dto = uomLookupProvider.resolve(id);
        return dto != null ? dto.subText() : null;
    }
}
