package com.solusi.erp.accountspayable.vendorbill.web.mapper;

import com.solusi.erp.accountspayable.vendorbill.application.usecase.command.VendorBillLineCommand;
import com.solusi.erp.accountspayable.vendorbill.application.usecase.query.VendorBillCreateView;
import com.solusi.erp.accountspayable.vendorbill.application.usecase.query.VendorBillDetailView;
import com.solusi.erp.accountspayable.vendorbill.application.usecase.query.VendorBillLineView;
import com.solusi.erp.accountspayable.vendorbill.application.usecase.query.VendorBillSummaryView;
import com.solusi.erp.accountspayable.vendorbill.web.dto.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class VendorBillWebMapper {

    public VendorBillSaveCommand toCreateCommand(VendorBillSaveRequest request) {
        return toSaveCommand(request);
    }

    public VendorBillSaveCommand toUpdateCommand(Long id, VendorBillSaveRequest request) {
        VendorBillSaveCommand command = toSaveCommand(request);
        return new VendorBillSaveCommand(
                id,
                command.vendorId(),
                command.vendorInvoiceNumber(),
                command.billDate(),
                command.dueDate(),
                command.currencyId(),
                command.exchangeRate(),
                command.notes(),
                command.grIds(),
                command.lines()
        );
    }

    public VendorBillSummaryResponse toSummaryResponse(VendorBillSummaryView view) {
        VendorBillSummaryResponse response = new VendorBillSummaryResponse();
        response.setId(view.id());
        response.setCode(view.code());
        response.setVendorId(view.vendorId());
        response.setVendorInvoiceNumber(view.vendorInvoiceNumber());
        response.setBillDate(view.billDate());
        response.setDueDate(view.dueDate());
        response.setDocumentStatus(view.documentStatus() != null ? view.documentStatus().name() : null);
        response.setSettlementStatus(view.settlementStatus() != null ? view.settlementStatus().name() : null);
        response.setTotalAmount(view.totalAmount());
        response.setPaidAmount(view.paidAmount());
        response.setDebitMemoAppliedAmount(view.debitMemoAppliedAmount());
        response.setOutstandingAmount(view.outstandingAmount());
        return response;
    }

    public VendorBillDetailResponse toDetailResponse(VendorBillDetailView view) {
        VendorBillDetailResponse response = new VendorBillDetailResponse();
        response.setId(view.id());
        response.setCode(view.code());
        response.setVendorId(view.vendorId());
        response.setVendorInvoiceNumber(view.vendorInvoiceNumber());
        response.setBillDate(view.billDate());
        response.setDueDate(view.dueDate());
        response.setCurrencyId(view.currencyId());
        response.setExchangeRate(view.exchangeRate());
        response.setDocumentStatus(view.documentStatus() != null ? view.documentStatus().name() : null);
        response.setSettlementStatus(view.settlementStatus() != null ? view.settlementStatus().name() : null);
        response.setSubtotal(view.subtotal());
        response.setTaxAmount(view.taxAmount());
        response.setTotalAmount(view.totalAmount());
        response.setPaidAmount(view.paidAmount());
        response.setDebitMemoAppliedAmount(view.debitMemoAppliedAmount());
        response.setOutstandingAmount(view.outstandingAmount());
        response.setNotes(view.notes());
        response.setGrIds(view.grIds());
        response.setLines(toLineResponses(view.lines()));
        return response;
    }

    public VendorBillFormView toFormView(VendorBillCreateView view) {
        VendorBillSaveRequest request = new VendorBillSaveRequest();
        request.setVendorId(view.vendorId());
        request.setCurrencyId(view.currencyId());
        request.setExchangeRate(view.exchangeRate());
        return new VendorBillFormView(request, null, null, false, view.billableGrs());
    }

    public List<VendorBillSummaryResponse> toSummaryResponses(List<VendorBillSummaryView> views) {
        if (views == null) {
            return List.of();
        }
        return views.stream().map(this::toSummaryResponse).toList();
    }

    private VendorBillSaveCommand toSaveCommand(VendorBillSaveRequest request) {
        return new VendorBillSaveCommand(
                request.getId(),
                request.getVendorId(),
                request.getVendorInvoiceNumber(),
                request.getBillDate(),
                request.getDueDate(),
                request.getCurrencyId(),
                request.getExchangeRate(),
                request.getNotes(),
                request.getGrIds() == null ? List.of() : List.copyOf(request.getGrIds()),
                toLineCommands(request.getLines())
        );
    }

    private List<VendorBillLineCommand> toLineCommands(List<VendorBillLineRequest> lines) {
        if (lines == null) {
            return List.of();
        }
        return lines.stream().map(this::toLineCommand).toList();
    }

    private VendorBillLineCommand toLineCommand(VendorBillLineRequest line) {
        return new VendorBillLineCommand(
                line.getId(),
                line.getGrLineId(),
                line.getProductId(),
                line.getProductName(),
                line.getDescription(),
                line.getQtyBilled(),
                line.getUomId(),
                line.getUomName(),
                line.getUnitPrice(),
                line.getInventoryAmount(),
                line.getTaxAmount()
        );
    }

    private List<VendorBillLineResponse> toLineResponses(List<VendorBillLineView> lines) {
        if (lines == null) {
            return List.of();
        }
        return lines.stream().map(this::toLineResponse).toList();
    }

    private VendorBillLineResponse toLineResponse(VendorBillLineView line) {
        VendorBillLineResponse response = new VendorBillLineResponse();
        response.setId(line.id());
        response.setGrLineId(line.grLineId());
        response.setProductId(line.productId());
        response.setProductName(line.productName());
        response.setDescription(line.description());
        response.setQtyBilled(line.qtyBilled());
        response.setUomId(line.uomId());
        response.setUomName(line.uomName());
        response.setUnitPrice(line.unitPrice());
        response.setInventoryAmount(line.inventoryAmount());
        response.setTaxAmount(line.taxAmount());
        response.setLineTotal(line.lineTotal());
        return response;
    }
}
