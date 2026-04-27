package com.solusi.erp.inventory.goodsreceipt.web.mapper;

import com.solusi.erp.core.dto.BaseAuditResponse;
import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.core.mapper.AuditMapperHelper;
import com.solusi.erp.inventory.facility.domain.port.FacilityLookupProvider;
import com.solusi.erp.inventory.goodsreceipt.application.usecase.command.GoodsReceiptLineCommand;
import com.solusi.erp.inventory.goodsreceipt.domain.model.GoodsReceipt;
import com.solusi.erp.inventory.goodsreceipt.domain.model.GoodsReceiptLine;
import com.solusi.erp.inventory.goodsreceipt.domain.model.GoodsReceiptReferenceType;
import com.solusi.erp.inventory.goodsreceipt.web.dto.*;
import com.solusi.erp.inventory.product.domain.port.ProductLookupProvider;
import com.solusi.erp.inventory.uom.domain.port.UomLookupProvider;
import com.solusi.erp.master.currency.domain.port.CurrencyLookupProvider;
import com.solusi.erp.master.party.domain.port.PartyLookupProvider;
import com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrder;
import com.solusi.erp.purchasing.purchaseorder.domain.repository.PurchaseOrderRepository;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class GoodsReceiptWebMapper {

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
    @Autowired
    protected PurchaseOrderRepository purchaseOrderRepository;

    @Mapping(target = "lineCount", expression = "java(domain.getLines() != null ? domain.getLines().size() : 0)")
    @Mapping(target = "supplierName", source = "supplierId", qualifiedByName = "getSupplierName")
    @Mapping(target = "referenceType", source = "referenceType", qualifiedByName = "getReferenceTypeName")
    @Mapping(target = "referenceCode", expression = "java(resolveReferenceCode(domain.getReferenceType(), domain.getReferenceId()))")
    @Mapping(target = "status", source = "status", qualifiedByName = "getStatusName")
    public abstract GoodsReceiptSummaryResponse toSummaryResponse(GoodsReceipt domain);

    @Mapping(target = "supplierName", source = "supplierId", qualifiedByName = "getSupplierName")
    @Mapping(target = "facilityName", source = "facilityId", qualifiedByName = "getFacilityName")
    @Mapping(target = "currencyCode", source = "currencyId", qualifiedByName = "getCurrencyCode")
    @Mapping(target = "referenceType", source = "referenceType", qualifiedByName = "getReferenceTypeName")
    @Mapping(target = "referenceId", source = "referenceId")
    @Mapping(target = "referenceCode", expression = "java(resolveReferenceCode(domain.getReferenceType(), domain.getReferenceId()))")
    @Mapping(target = "status", source = "status", qualifiedByName = "getStatusName")
    public abstract GoodsReceiptDetailResponse toDetailResponse(GoodsReceipt domain);

    public abstract GoodsReceiptSaveRequest toSaveRequest(GoodsReceipt domain);

    @Mapping(target = "productName", source = "productId", qualifiedByName = "getProductName")
    @Mapping(target = "productCode", source = "productId", qualifiedByName = "getProductCode")
    @Mapping(target = "uomCode", source = "uomId", qualifiedByName = "getUomCode")
    public abstract GoodsReceiptLineDetailResponse toLineDetailResponse(GoodsReceiptLine line);

    public abstract GoodsReceiptSaveLineRequest toSaveLineRequest(GoodsReceiptLine line);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "poLineId", source = "poLineId")
    @Mapping(target = "productId", source = "productId")
    @Mapping(target = "serialized", source = "serialized")
    @Mapping(target = "quantityReceived", source = "quantityReceived")
    @Mapping(target = "uomId", source = "uomId")
    @Mapping(target = "containerId", source = "containerId")
    @Mapping(target = "serialNumber", source = "serialNumber")
    public abstract GoodsReceiptLineCommand toLineCommand(GoodsReceiptSaveLineRequest request);

    public List<GoodsReceiptLineCommand> toLineCommands(List<GoodsReceiptSaveLineRequest> requests) {
        if (requests == null) return List.of();
        return requests.stream().map(this::toLineCommand).toList();
    }

    public List<GoodsReceiptLineDetailResponse> toLineDetailResponses(List<GoodsReceiptLine> lines) {
        if (lines == null) return List.of();
        return lines.stream().map(this::toLineDetailResponse).toList();
    }

    public List<GoodsReceiptSummaryResponse> toSummaryList(List<GoodsReceipt> domains) {
        if (domains == null) return List.of();
        return domains.stream().map(this::toSummaryResponse).toList();
    }

    @AfterMapping
    protected void mapAuditFields(GoodsReceipt domain, @MappingTarget BaseAuditResponse target) {
        if (domain.getMetadata() != null) {
            target.setId(domain.getId());
            target.setVersion(domain.getMetadata().version() != null ? domain.getMetadata().version().intValue() : null);
            target.setCreatedDate(domain.getMetadata().createdDate());
            target.setUpdatedDate(domain.getMetadata().updatedDate());
            target.setCreatedByName(auditMapperHelper.resolveUserDisplayName(domain.getMetadata().createdBy()));
            target.setUpdatedByName(auditMapperHelper.resolveUserDisplayName(domain.getMetadata().updatedBy()));
        }
    }

    @AfterMapping
    protected void mapLineDetailFields(GoodsReceiptLine line, @MappingTarget GoodsReceiptLineDetailResponse target) {
        // These fields are already mapped, but we might need additional processing
        // The target already has all mapped fields from the line
    }

    @AfterMapping
    protected void enrichSaveRequest(GoodsReceipt domain, @MappingTarget GoodsReceiptSaveRequest target) {
        target.setReferenceCode(resolveReferenceCode(domain.getReferenceType(), domain.getReferenceId()));
        target.setSupplierName(getSupplierName(domain.getSupplierId()));
        target.setFacilityName(getFacilityName(domain.getFacilityId()));
        target.setCurrencyCode(getCurrencyCode(domain.getCurrencyId()));
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

    @Named("getCurrencyCode")
    protected String getCurrencyCode(Long id) {
        if (id == null) return null;
        LookupDto dto = currencyLookupProvider.resolve(id);
        return dto != null ? dto.subText() : null;
    }

    @Named("getProductName")
    protected String getProductName(Long id) {
        if (id == null) return null;
        LookupDto dto = productLookupProvider.resolve(id);
        return dto != null ? dto.name() : null;
    }

    @Named("getProductCode")
    protected String getProductCode(Long id) {
        if (id == null) return null;
        LookupDto dto = productLookupProvider.resolve(id);
        return dto != null ? dto.subText() : null;
    }

    @Named("getUomCode")
    protected String getUomCode(Long id) {
        if (id == null) return null;
        LookupDto dto = uomLookupProvider.resolve(id);
        return dto != null ? dto.subText() : null;
    }

    protected String resolveReferenceCode(GoodsReceiptReferenceType referenceType, Long referenceId) {
        if (referenceType == null || referenceId == null) {
            return null;
        }
        if (referenceType == GoodsReceiptReferenceType.PURCHASE_ORDER) {
            return purchaseOrderRepository.findById(referenceId)
                    .map(PurchaseOrder::getCode)
                    .orElse(null);
        }
        return null;
    }

    @Named("getReferenceTypeName")
    protected String getReferenceTypeName(GoodsReceiptReferenceType referenceType) {
        if (referenceType == null) {
            return null;
        }
        return referenceType.name();
    }

    @Named("getStatusName")
    protected String getStatusName(Object status) {
        if (status == null) return null;
        return status.toString();
    }
}
