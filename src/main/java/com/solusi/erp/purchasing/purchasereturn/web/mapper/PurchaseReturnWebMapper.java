package com.solusi.erp.purchasing.purchasereturn.web.mapper;

import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.core.mapper.AuditMapperHelper;
import com.solusi.erp.inventory.container.domain.port.ContainerLookupProvider;
import com.solusi.erp.inventory.facility.domain.port.FacilityLookupProvider;
import com.solusi.erp.inventory.grid.domain.port.GridLookupProvider;
import com.solusi.erp.inventory.product.domain.port.ProductLookupProvider;
import com.solusi.erp.inventory.uom.domain.port.UomLookupProvider;
import com.solusi.erp.master.currency.domain.port.CurrencyLookupProvider;
import com.solusi.erp.master.party.domain.port.PartyLookupProvider;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.command.PurchaseReturnLineCommand;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.query.EligibleGoodsReceiptRow;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.query.GetPurchaseReturnCreateViewUseCase.PurchaseReturnCreateView;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.query.ReturnableGrLineSlice;
import com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturn;
import com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturnLine;
import com.solusi.erp.purchasing.purchasereturn.web.dto.PurchaseReturnDetailResponse;
import com.solusi.erp.purchasing.purchasereturn.web.dto.PurchaseReturnLineDetailResponse;
import com.solusi.erp.purchasing.purchasereturn.web.dto.PurchaseReturnSaveLineRequest;
import com.solusi.erp.purchasing.purchasereturn.web.dto.PurchaseReturnSaveRequest;
import com.solusi.erp.purchasing.purchasereturn.web.dto.PurchaseReturnSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PurchaseReturnWebMapper {

    private final AuditMapperHelper auditMapperHelper;
    private final PartyLookupProvider partyLookupProvider;
    private final FacilityLookupProvider facilityLookupProvider;
    private final CurrencyLookupProvider currencyLookupProvider;
    private final ProductLookupProvider productLookupProvider;
    private final UomLookupProvider uomLookupProvider;
    private final GridLookupProvider gridLookupProvider;
    private final ContainerLookupProvider containerLookupProvider;

    public PurchaseReturnSaveRequest toCreateRequest(PurchaseReturnCreateView view) {
        PurchaseReturnSaveRequest request = new PurchaseReturnSaveRequest();
        mapSource(view.source(), request);
        request.setLines(view.lines().stream().map(this::toPrefillLine).toList());
        return request;
    }

    public PurchaseReturnSaveRequest toSaveRequest(PurchaseReturn domain) {
        PurchaseReturnSaveRequest request = new PurchaseReturnSaveRequest();
        request.setId(domain.getId());
        request.setVersion(version(domain));
        request.setReturnDate(domain.getReturnDate());
        request.setGoodsReceiptId(domain.getReferenceId());
        request.setGoodsReceiptCode(domain.getReferenceCode());
        request.setPurchaseOrderId(domain.getPurchaseOrderId());
        request.setPurchaseOrderCode(domain.getPurchaseOrderCode());
        request.setSupplierId(domain.getSupplierId());
        request.setSupplierName(name(partyLookupProvider.resolve(domain.getSupplierId())));
        request.setFacilityId(domain.getFacilityId());
        request.setFacilityName(name(facilityLookupProvider.resolve(domain.getFacilityId())));
        request.setCurrencyId(domain.getCurrencyId());
        request.setCurrencyCode(subText(currencyLookupProvider.resolve(domain.getCurrencyId())));
        request.setExchangeRate(domain.getExchangeRate());
        request.setReason(domain.getReason());
        request.setNote(domain.getNote());
        request.setLines(domain.getLines().stream().map(this::toSaveLine).toList());
        return request;
    }

    public PurchaseReturnSummaryResponse toSummaryResponse(PurchaseReturn domain) {
        PurchaseReturnSummaryResponse response = new PurchaseReturnSummaryResponse();
        mapAudit(domain, response);
        response.setCode(domain.getCode());
        response.setReturnDate(domain.getReturnDate());
        response.setSupplierName(name(partyLookupProvider.resolve(domain.getSupplierId())));
        response.setGoodsReceiptId(domain.getReferenceId());
        response.setGoodsReceiptCode(domain.getReferenceCode());
        response.setPurchaseOrderId(domain.getPurchaseOrderId());
        response.setPurchaseOrderCode(domain.getPurchaseOrderCode());
        response.setStatus(domain.getStatus());
        response.setReversalDate(domain.getReversalDate());
        response.setReversalReason(domain.getReversalReason());
        response.setReversedByUserId(domain.getReversedByUserId());
        response.setReversalJournalEntryId(domain.getReversalJournalEntryId());
        response.setTotalQuantity(totalQuantity(domain.getLines()));
        response.setTotalAmount(totalAmount(domain.getLines()));
        return response;
    }

    public PurchaseReturnDetailResponse toDetailResponse(PurchaseReturn domain) {
        PurchaseReturnDetailResponse response = new PurchaseReturnDetailResponse();
        mapAudit(domain, response);
        response.setCode(domain.getCode());
        response.setReturnDate(domain.getReturnDate());
        response.setGoodsReceiptId(domain.getReferenceId());
        response.setGoodsReceiptCode(domain.getReferenceCode());
        response.setPurchaseOrderId(domain.getPurchaseOrderId());
        response.setPurchaseOrderCode(domain.getPurchaseOrderCode());
        response.setSupplierId(domain.getSupplierId());
        response.setSupplierName(name(partyLookupProvider.resolve(domain.getSupplierId())));
        response.setFacilityId(domain.getFacilityId());
        response.setFacilityName(name(facilityLookupProvider.resolve(domain.getFacilityId())));
        response.setCurrencyId(domain.getCurrencyId());
        response.setCurrencyCode(subText(currencyLookupProvider.resolve(domain.getCurrencyId())));
        response.setExchangeRate(domain.getExchangeRate());
        response.setStatus(domain.getStatus());
        response.setReason(domain.getReason());
        response.setNote(domain.getNote());
        response.setSubmittedByUserId(domain.getSubmittedByUserId());
        response.setGeneratedGoodsIssueId(domain.getGeneratedGoodsIssueId());
        response.setReversalDate(domain.getReversalDate());
        response.setReversalReason(domain.getReversalReason());
        response.setReversedByUserId(domain.getReversedByUserId());
        response.setReversalJournalEntryId(domain.getReversalJournalEntryId());
        response.setTotalQuantity(totalQuantity(domain.getLines()));
        response.setTotalAmount(totalAmount(domain.getLines()));
        response.setLines(domain.getLines().stream().map(this::toLineDetailResponse).toList());
        return response;
    }

    public List<PurchaseReturnLineCommand> toLineCommands(List<PurchaseReturnSaveLineRequest> lines) {
        if (lines == null) {
            return List.of();
        }
        return lines.stream()
                .filter(line -> line.getQuantity() != null && line.getQuantity().compareTo(BigDecimal.ZERO) > 0)
                .map(line -> new PurchaseReturnLineCommand(
                        line.getGoodsReceiptLineId(),
                        line.isSerialized(),
                        line.getQuantity(),
                        line.getBaseQuantity(),
                        line.getContainerId(),
                        line.getSerialNumbers(),
                        line.getReason(),
                        line.getNote()
                ))
                .toList();
    }

    private PurchaseReturnSaveLineRequest toPrefillLine(ReturnableGrLineSlice slice) {
        PurchaseReturnSaveLineRequest target = new PurchaseReturnSaveLineRequest();
        target.setSelectionKey(slice.selectionKey());
        target.setGoodsReceiptLineId(slice.goodsReceiptLineId());
        target.setProductId(slice.productId());
        target.setProductName(slice.productName());
        target.setProductCode(slice.productCode());
        target.setSerialized(slice.serialized());
        target.setQuantity(BigDecimal.ZERO);
        target.setBaseQuantity(BigDecimal.ZERO);
        target.setUomId(slice.uomId());
        target.setUomName(slice.uomName());
        target.setUomCode(slice.uomCode());
        target.setFacilityId(slice.facilityId());
        target.setFacilityName(slice.facilityName());
        target.setGridId(slice.gridId());
        target.setGridName(slice.gridName());
        target.setGridCode(slice.gridCode());
        target.setContainerId(slice.containerId());
        target.setContainerName(slice.containerName());
        target.setContainerCode(slice.containerCode());
        target.setOutstandingQuantity(slice.outstandingQuantity());
        target.setValuationReferenceType(slice.valuationReferenceType());
        target.setValuationReferenceId(slice.valuationReferenceId());
        target.setValuationReferenceLineId(slice.valuationReferenceLineId());
        target.setUnitCost(slice.unitCost());
        target.setInventoryAmount(slice.inventoryAmount());
        target.setTaxReversalAmount(slice.taxReversalAmount());
        target.setClearingAmount(slice.clearingAmount());
        return target;
    }

    private PurchaseReturnSaveLineRequest toSaveLine(PurchaseReturnLine line) {
        PurchaseReturnSaveLineRequest target = new PurchaseReturnSaveLineRequest();
        target.setId(line.getId());
        target.setSelectionKey(selectionKey(line));
        target.setGoodsReceiptLineId(line.getGoodsReceiptLineId());
        target.setProductId(line.getProductId());
        target.setProductName(name(productLookupProvider.resolve(line.getProductId())));
        target.setProductCode(subText(productLookupProvider.resolve(line.getProductId())));
        target.setSerialized(line.isSerialized());
        target.setQuantity(line.getQuantity());
        target.setBaseQuantity(line.getBaseQuantity());
        target.setUomId(line.getUomId());
        target.setUomName(name(uomLookupProvider.resolve(line.getUomId())));
        target.setUomCode(subText(uomLookupProvider.resolve(line.getUomId())));
        target.setFacilityId(line.getFacilityId());
        target.setFacilityName(name(facilityLookupProvider.resolve(line.getFacilityId())));
        target.setGridId(line.getGridId());
        target.setGridName(name(gridLookupProvider.resolve(line.getGridId())));
        target.setGridCode(subText(gridLookupProvider.resolve(line.getGridId())));
        target.setContainerId(line.getContainerId());
        target.setContainerName(name(containerLookupProvider.resolve(line.getContainerId())));
        target.setContainerCode(subText(containerLookupProvider.resolve(line.getContainerId())));
        target.setSerialNumbers(line.getSerialNumbers());
        target.setOutstandingQuantity(line.getBaseQuantity());
        target.setReason(line.getReason());
        target.setNote(line.getNote());
        target.setValuationReferenceType(line.getValuationReferenceType());
        target.setValuationReferenceId(line.getValuationReferenceId());
        target.setValuationReferenceLineId(line.getValuationReferenceLineId());
        target.setUnitCost(line.getUnitCost());
        target.setInventoryAmount(line.getInventoryAmount());
        target.setTaxReversalAmount(line.getTaxReversalAmount());
        target.setClearingAmount(line.getClearingAmount());
        return target;
    }

    private PurchaseReturnLineDetailResponse toLineDetailResponse(PurchaseReturnLine line) {
        PurchaseReturnLineDetailResponse target = new PurchaseReturnLineDetailResponse();
        target.setId(line.getId());
        target.setGoodsReceiptLineId(line.getGoodsReceiptLineId());
        target.setProductId(line.getProductId());
        target.setProductName(name(productLookupProvider.resolve(line.getProductId())));
        target.setProductCode(subText(productLookupProvider.resolve(line.getProductId())));
        target.setSerialized(line.isSerialized());
        target.setQuantity(line.getQuantity());
        target.setUomId(line.getUomId());
        target.setUomName(name(uomLookupProvider.resolve(line.getUomId())));
        target.setUomCode(subText(uomLookupProvider.resolve(line.getUomId())));
        target.setBaseQuantity(line.getBaseQuantity());
        target.setFacilityId(line.getFacilityId());
        target.setFacilityName(name(facilityLookupProvider.resolve(line.getFacilityId())));
        target.setGridId(line.getGridId());
        target.setGridName(name(gridLookupProvider.resolve(line.getGridId())));
        target.setGridCode(subText(gridLookupProvider.resolve(line.getGridId())));
        target.setContainerId(line.getContainerId());
        target.setContainerName(name(containerLookupProvider.resolve(line.getContainerId())));
        target.setContainerCode(subText(containerLookupProvider.resolve(line.getContainerId())));
        target.setSerialNumbers(line.getSerialNumbers());
        target.setReason(line.getReason());
        target.setNote(line.getNote());
        target.setValuationReferenceType(line.getValuationReferenceType());
        target.setValuationReferenceId(line.getValuationReferenceId());
        target.setValuationReferenceLineId(line.getValuationReferenceLineId());
        target.setUnitCost(line.getUnitCost());
        target.setInventoryAmount(line.getInventoryAmount());
        target.setTaxReversalAmount(line.getTaxReversalAmount());
        target.setClearingAmount(line.getClearingAmount());
        return target;
    }

    private void mapSource(EligibleGoodsReceiptRow source, PurchaseReturnSaveRequest target) {
        target.setGoodsReceiptId(source.goodsReceiptId());
        target.setGoodsReceiptCode(source.goodsReceiptCode());
        target.setPurchaseOrderId(source.purchaseOrderId());
        target.setPurchaseOrderCode(source.purchaseOrderCode());
        target.setSupplierId(source.supplierId());
        target.setSupplierName(source.supplierName());
        target.setFacilityId(source.facilityId());
        target.setFacilityName(source.facilityName());
        target.setCurrencyId(source.currencyId());
        target.setCurrencyCode(source.currencyCode());
        target.setExchangeRate(source.exchangeRate());
    }

    private void mapAudit(PurchaseReturn domain, com.solusi.erp.core.dto.BaseAuditResponse target) {
        target.setId(domain.getId());
        target.setVersion(version(domain));
        target.setCreatedDate(domain.getMetadata().createdDate());
        target.setUpdatedDate(domain.getMetadata().updatedDate());
        target.setCreatedByName(auditMapperHelper.resolveUserDisplayName(domain.getMetadata().createdBy()));
        target.setUpdatedByName(auditMapperHelper.resolveUserDisplayName(domain.getMetadata().updatedBy()));
    }

    private Integer version(PurchaseReturn domain) {
        return domain.getMetadata().version() == null ? null : domain.getMetadata().version().intValue();
    }

    private String selectionKey(PurchaseReturnLine line) {
        return line.isSerialized()
                ? line.getGoodsReceiptLineId() + ":" + line.getContainerId() + ":" + line.getSerialNumbers()
                : line.getGoodsReceiptLineId() + ":" + line.getContainerId();
    }

    private BigDecimal totalQuantity(List<PurchaseReturnLine> lines) {
        return lines.stream().map(PurchaseReturnLine::getQuantity).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal totalAmount(List<PurchaseReturnLine> lines) {
        return lines.stream().map(PurchaseReturnLine::getInventoryAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String name(LookupDto lookup) {
        return lookup == null ? null : lookup.name();
    }

    private String subText(LookupDto lookup) {
        return lookup == null ? null : lookup.subText();
    }
}
