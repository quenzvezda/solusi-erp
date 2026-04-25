package com.solusi.erp.purchasing.purchaserequisition.web.mapper;

import com.solusi.erp.core.dto.BaseAuditResponse;
import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.core.mapper.AuditMapperHelper;
import com.solusi.erp.inventory.facility.domain.port.FacilityLookupProvider;
import com.solusi.erp.inventory.product.domain.port.ProductLookupProvider;
import com.solusi.erp.inventory.uom.domain.port.UomLookupProvider;
import com.solusi.erp.master.currency.domain.port.CurrencyLookupProvider;
import com.solusi.erp.master.party.domain.port.PartyLookupProvider;
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
    protected PartyLookupProvider partyLookupProvider;
    @Autowired
    protected FacilityLookupProvider facilityLookupProvider;
    @Autowired
    protected ProductLookupProvider productLookupProvider;
    @Autowired
    protected UomLookupProvider uomLookupProvider;
    @Autowired
    protected CurrencyLookupProvider currencyLookupProvider;

    @Mapping(target = "currencyCode", source = "currencyId", qualifiedByName = "getCurrencyCode")
    @Mapping(target = "requesterName", source = "requesterId", qualifiedByName = "getRequesterName")
    public abstract PurchaseRequisitionSummaryResponse toSummaryResponse(PurchaseRequisition domain);

    @Mapping(target = "requesterName", source = "requesterId", qualifiedByName = "getRequesterName")
    @Mapping(target = "facilityName", source = "facilityId", qualifiedByName = "getFacilityName")
    @Mapping(target = "supplierName", source = "suggestedSupplierId", qualifiedByName = "getSupplierName")
    @Mapping(target = "currencyCode", source = "currencyId", qualifiedByName = "getCurrencyCode")
    public abstract PurchaseRequisitionDetailResponse toDetailResponse(PurchaseRequisition domain);

    public abstract PurchaseRequisitionSaveRequest toSaveRequest(PurchaseRequisition domain);

    @Mapping(target = "productName", source = "productId", qualifiedByName = "getProductName")
    @Mapping(target = "uomName", source = "uomId", qualifiedByName = "getUomName")
    @Mapping(target = "productSubtext", source = "productId", qualifiedByName = "getProductSubtext")
    @Mapping(target = "uomSubtext", source = "uomId", qualifiedByName = "getUomSubtext")
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
        LookupDto dto = partyLookupProvider.resolve(id);
        return dto != null ? dto.name() : null;
    }

    @Named("getFacilityName")
    protected String getFacilityName(Long id) {
        if (id == null) return null;
        LookupDto dto = facilityLookupProvider.resolve(id);
        return dto != null ? dto.name() : null;
    }

    @Named("getProductName")
    protected String getProductName(Long id) {
        if (id == null) return null;
        LookupDto dto = productLookupProvider.resolve(id);
        return dto != null ? dto.name() : null;
    }

    @Named("getProductSubtext")
    protected String getProductSubtext(Long id) {
        if (id == null) return null;
        LookupDto dto = productLookupProvider.resolve(id);
        return dto != null ? dto.subText() : null;
    }

    @Named("getUomName")
    protected String getUomName(Long id) {
        if (id == null) return null;
        LookupDto dto = uomLookupProvider.resolve(id);
        return dto != null ? dto.name() : null;
    }

    @Named("getUomSubtext")
    protected String getUomSubtext(Long id) {
        if (id == null) return null;
        LookupDto dto = uomLookupProvider.resolve(id);
        return dto != null ? dto.subText() : null;
    }

    @Named("getSupplierName")
    protected String getSupplierName(Long id) {
        if (id == null) return null;
        LookupDto dto = partyLookupProvider.resolve(id);
        return dto != null ? dto.name() : null;
    }

    @Named("getCurrencyCode")
    protected String getCurrencyCode(Long id) {
        if (id == null) return null;
        LookupDto dto = currencyLookupProvider.resolve(id);
        if (dto == null || dto.payload() == null) return null;
        Object alias = dto.payload().get("alias");
        return alias != null ? alias.toString() : null;
    }
}
