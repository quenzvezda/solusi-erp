package com.solusi.erp.inventory.goodsissue.web.mapper;

import com.solusi.erp.core.dto.BaseAuditResponse;
import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.core.mapper.AuditMapperHelper;
import com.solusi.erp.inventory.container.domain.port.ContainerLookupProvider;
import com.solusi.erp.inventory.facility.domain.port.FacilityLookupProvider;
import com.solusi.erp.inventory.goodsissue.application.usecase.command.GoodsIssueLineCommand;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssue;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssueLine;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssueReferenceType;
import com.solusi.erp.inventory.goodsissue.domain.port.GoodsIssueReferenceLookupProvider;
import com.solusi.erp.inventory.goodsissue.web.dto.GoodsIssueDetailResponse;
import com.solusi.erp.inventory.goodsissue.web.dto.GoodsIssueLineDetailResponse;
import com.solusi.erp.inventory.goodsissue.web.dto.GoodsIssueSaveLineRequest;
import com.solusi.erp.inventory.goodsissue.web.dto.GoodsIssueSaveRequest;
import com.solusi.erp.inventory.goodsissue.web.dto.GoodsIssueSummaryResponse;
import com.solusi.erp.inventory.grid.domain.port.GridLookupProvider;
import com.solusi.erp.inventory.product.domain.port.ProductLookupProvider;
import com.solusi.erp.inventory.uom.domain.port.UomLookupProvider;
import com.solusi.erp.master.currency.domain.port.CurrencyLookupProvider;
import com.solusi.erp.master.party.domain.port.PartyLookupProvider;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class GoodsIssueWebMapper {

    @Autowired
    protected AuditMapperHelper auditMapperHelper;
    @Autowired
    protected PartyLookupProvider partyLookupProvider;
    @Autowired
    protected FacilityLookupProvider facilityLookupProvider;
    @Autowired
    protected GridLookupProvider gridLookupProvider;
    @Autowired
    protected ContainerLookupProvider containerLookupProvider;
    @Autowired
    protected CurrencyLookupProvider currencyLookupProvider;
    @Autowired
    protected ProductLookupProvider productLookupProvider;
    @Autowired
    protected UomLookupProvider uomLookupProvider;
    @Autowired
    protected GoodsIssueReferenceLookupProvider referenceLookupProvider;

    @Mapping(target = "lineCount", expression = "java(domain.getLines() != null ? domain.getLines().size() : 0)")
    @Mapping(target = "partyName", source = "partyId", qualifiedByName = "getPartyName")
    @Mapping(target = "facilityName", source = "facilityId", qualifiedByName = "getFacilityName")
    @Mapping(target = "referenceType", source = "referenceType", qualifiedByName = "getReferenceTypeName")
    @Mapping(target = "referenceCode", expression = "java(resolveReferenceCode(domain.getReferenceType(), domain.getReferenceId(), domain.getReferenceCode()))")
    @Mapping(target = "status", source = "status", qualifiedByName = "getStatusName")
    @Mapping(target = "totalAmount", expression = "java(totalAmount(domain.getLines()))")
    public abstract GoodsIssueSummaryResponse toSummaryResponse(GoodsIssue domain);

    @Mapping(target = "partyName", source = "partyId", qualifiedByName = "getPartyName")
    @Mapping(target = "facilityName", source = "facilityId", qualifiedByName = "getFacilityName")
    @Mapping(target = "currencyCode", source = "currencyId", qualifiedByName = "getCurrencyCode")
    @Mapping(target = "referenceType", source = "referenceType", qualifiedByName = "getReferenceTypeName")
    @Mapping(target = "referenceCode", expression = "java(resolveReferenceCode(domain.getReferenceType(), domain.getReferenceId(), domain.getReferenceCode()))")
    @Mapping(target = "partyType", source = "partyType", qualifiedByName = "getStatusName")
    @Mapping(target = "status", source = "status", qualifiedByName = "getStatusName")
    @Mapping(target = "notes", source = "note")
    public abstract GoodsIssueDetailResponse toDetailResponse(GoodsIssue domain);

    public abstract GoodsIssueSaveRequest toSaveRequest(GoodsIssue domain);

    @Mapping(target = "productName", source = "productId", qualifiedByName = "getProductName")
    @Mapping(target = "productCode", source = "productId", qualifiedByName = "getProductCode")
    @Mapping(target = "uomCode", source = "uomId", qualifiedByName = "getUomCode")
    @Mapping(target = "facilityName", source = "facilityId", qualifiedByName = "getFacilityName")
    @Mapping(target = "facilityCode", source = "facilityId", qualifiedByName = "getFacilityCode")
    @Mapping(target = "gridName", source = "gridId", qualifiedByName = "getGridName")
    @Mapping(target = "gridCode", source = "gridId", qualifiedByName = "getGridCode")
    @Mapping(target = "containerName", source = "containerId", qualifiedByName = "getContainerName")
    @Mapping(target = "containerCode", source = "containerId", qualifiedByName = "getContainerCode")
    public abstract GoodsIssueLineDetailResponse toLineDetailResponse(GoodsIssueLine line);

    @Mapping(target = "productName", source = "productId", qualifiedByName = "getProductName")
    @Mapping(target = "productCode", source = "productId", qualifiedByName = "getProductCode")
    @Mapping(target = "uomName", source = "uomId", qualifiedByName = "getUomName")
    @Mapping(target = "uomCode", source = "uomId", qualifiedByName = "getUomCode")
    @Mapping(target = "facilityName", source = "facilityId", qualifiedByName = "getFacilityName")
    @Mapping(target = "facilityCode", source = "facilityId", qualifiedByName = "getFacilityCode")
    @Mapping(target = "gridName", source = "gridId", qualifiedByName = "getGridName")
    @Mapping(target = "gridCode", source = "gridId", qualifiedByName = "getGridCode")
    @Mapping(target = "containerName", source = "containerId", qualifiedByName = "getContainerName")
    @Mapping(target = "containerCode", source = "containerId", qualifiedByName = "getContainerCode")
    public abstract GoodsIssueSaveLineRequest toSaveLineRequest(GoodsIssueLine line);

    public abstract GoodsIssueLineCommand toLineCommand(GoodsIssueSaveLineRequest request);

    public List<GoodsIssueLineCommand> toLineCommands(List<GoodsIssueSaveLineRequest> requests) {
        if (requests == null) return List.of();
        return requests.stream().map(this::toLineCommand).toList();
    }

    public List<GoodsIssueLineDetailResponse> toLineDetailResponses(List<GoodsIssueLine> lines) {
        if (lines == null) return List.of();
        return lines.stream().map(this::toLineDetailResponse).toList();
    }

    @AfterMapping
    protected void mapAuditFields(GoodsIssue domain, @MappingTarget BaseAuditResponse target) {
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
    protected void enrichSaveRequest(GoodsIssue domain, @MappingTarget GoodsIssueSaveRequest target) {
        target.setReferenceCode(resolveReferenceCode(domain.getReferenceType(), domain.getReferenceId(), domain.getReferenceCode()));
        target.setPartyName(getPartyName(domain.getPartyId()));
        target.setFacilityName(getFacilityName(domain.getFacilityId()));
        target.setCurrencyCode(getCurrencyCode(domain.getCurrencyId()));
        target.setStatus(getStatusName(domain.getStatus()));
        target.setNotes(domain.getNote());
    }

    @Named("getPartyName")
    protected String getPartyName(Long id) {
        LookupDto dto = id == null ? null : partyLookupProvider.resolve(id);
        return dto != null ? dto.name() : null;
    }

    @Named("getFacilityName")
    protected String getFacilityName(Long id) {
        LookupDto dto = id == null ? null : facilityLookupProvider.resolve(id);
        return dto != null ? dto.name() : null;
    }

    @Named("getFacilityCode")
    protected String getFacilityCode(Long id) {
        LookupDto dto = id == null ? null : facilityLookupProvider.resolve(id);
        return dto != null ? dto.subText() : null;
    }

    @Named("getGridName")
    protected String getGridName(Long id) {
        LookupDto dto = id == null ? null : gridLookupProvider.resolve(id);
        return dto != null ? dto.name() : null;
    }

    @Named("getGridCode")
    protected String getGridCode(Long id) {
        LookupDto dto = id == null ? null : gridLookupProvider.resolve(id);
        return dto != null ? dto.subText() : null;
    }

    @Named("getCurrencyCode")
    protected String getCurrencyCode(Long id) {
        LookupDto dto = id == null ? null : currencyLookupProvider.resolve(id);
        return dto != null ? dto.subText() : null;
    }

    @Named("getProductName")
    protected String getProductName(Long id) {
        LookupDto dto = id == null ? null : productLookupProvider.resolve(id);
        return dto != null ? dto.name() : null;
    }

    @Named("getProductCode")
    protected String getProductCode(Long id) {
        LookupDto dto = id == null ? null : productLookupProvider.resolve(id);
        return dto != null ? dto.subText() : null;
    }

    @Named("getUomCode")
    protected String getUomCode(Long id) {
        LookupDto dto = id == null ? null : uomLookupProvider.resolve(id);
        return dto != null ? dto.subText() : null;
    }

    @Named("getUomName")
    protected String getUomName(Long id) {
        LookupDto dto = id == null ? null : uomLookupProvider.resolve(id);
        return dto != null ? dto.name() : null;
    }

    @Named("getContainerName")
    protected String getContainerName(Long id) {
        LookupDto dto = id == null ? null : containerLookupProvider.resolve(id);
        return dto != null ? dto.name() : null;
    }

    @Named("getContainerCode")
    protected String getContainerCode(Long id) {
        LookupDto dto = id == null ? null : containerLookupProvider.resolve(id);
        return dto != null ? dto.subText() : null;
    }

    protected String resolveReferenceCode(GoodsIssueReferenceType referenceType, Long referenceId, String fallback) {
        if (fallback != null && !fallback.isBlank()) {
            return fallback;
        }
        return referenceType == null || referenceId == null ? null : referenceLookupProvider.resolveReferenceCode(referenceType, referenceId);
    }

    @Named("getReferenceTypeName")
    protected String getReferenceTypeName(GoodsIssueReferenceType referenceType) {
        return referenceType == null ? null : referenceType.name();
    }

    @Named("getStatusName")
    protected String getStatusName(Object status) {
        return status == null ? null : status.toString();
    }

    protected java.math.BigDecimal totalAmount(List<GoodsIssueLine> lines) {
        if (lines == null) return java.math.BigDecimal.ZERO;
        return lines.stream()
                .map(GoodsIssueLine::getInventoryAmount)
                .filter(amount -> amount != null)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
    }
}
