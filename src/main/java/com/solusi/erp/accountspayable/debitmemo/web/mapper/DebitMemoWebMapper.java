package com.solusi.erp.accountspayable.debitmemo.web.mapper;

import com.solusi.erp.accountspayable.debitmemo.application.usecase.query.DebitMemoDetailView;
import com.solusi.erp.accountspayable.debitmemo.application.usecase.query.DebitMemoLineView;
import com.solusi.erp.accountspayable.debitmemo.application.usecase.query.DebitMemoSummaryView;
import com.solusi.erp.accountspayable.debitmemo.web.dto.DebitMemoDetailResponse;
import com.solusi.erp.accountspayable.debitmemo.web.dto.DebitMemoLineResponse;
import com.solusi.erp.accountspayable.debitmemo.web.dto.DebitMemoSummaryResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DebitMemoWebMapper {

    public DebitMemoSummaryResponse toSummaryResponse(DebitMemoSummaryView view) {
        DebitMemoSummaryResponse response = new DebitMemoSummaryResponse();
        response.setId(view.id());
        response.setCode(view.code());
        response.setMemoDate(view.memoDate());
        response.setVendorId(view.vendorId());
        response.setCurrencyId(view.currencyId());
        response.setPurchaseReturnId(view.purchaseReturnId());
        response.setPurchaseReturnCode(view.purchaseReturnCode());
        response.setGrossAmountOriginal(view.grossAmountOriginal());
        response.setSettledAmount(view.settledAmount());
        response.setRefundedAmount(view.refundedAmount());
        response.setRemainingAmount(view.remainingAmount());
        response.setSettlementStatus(view.settlementStatus() != null ? view.settlementStatus().name() : null);
        return response;
    }

    public DebitMemoDetailResponse toDetailResponse(DebitMemoDetailView view) {
        DebitMemoDetailResponse response = new DebitMemoDetailResponse();
        response.setId(view.id());
        response.setCode(view.code());
        response.setPurchaseReturnId(view.purchaseReturnId());
        response.setPurchaseReturnCode(view.purchaseReturnCode());
        response.setGeneratedGoodsIssueId(view.generatedGoodsIssueId());
        response.setVendorId(view.vendorId());
        response.setCurrencyId(view.currencyId());
        response.setMemoDate(view.memoDate());
        response.setGrossAmountOriginal(view.grossAmountOriginal());
        response.setDppAmountOriginal(view.dppAmountOriginal());
        response.setTaxAmountOriginal(view.taxAmountOriginal());
        response.setGrossAmountBase(view.grossAmountBase());
        response.setDppAmountBase(view.dppAmountBase());
        response.setTaxAmountBase(view.taxAmountBase());
        response.setSettledAmount(view.settledAmount());
        response.setRefundedAmount(view.refundedAmount());
        response.setRemainingAmount(view.remainingAmount());
        response.setSettlementStatus(view.settlementStatus() != null ? view.settlementStatus().name() : null);
        response.setSupplierMemoNumber(view.supplierMemoNumber());
        response.setSupplierMemoDate(view.supplierMemoDate());
        response.setTaxDocumentNumber(view.taxDocumentNumber());
        response.setTaxDocumentDate(view.taxDocumentDate());
        response.setNotes(view.notes());
        response.setLines(toLineResponses(view.lines()));
        return response;
    }

    private List<DebitMemoLineResponse> toLineResponses(List<DebitMemoLineView> lines) {
        if (lines == null) {
            return List.of();
        }
        return lines.stream().map(this::toLineResponse).toList();
    }

    private DebitMemoLineResponse toLineResponse(DebitMemoLineView line) {
        DebitMemoLineResponse response = new DebitMemoLineResponse();
        response.setId(line.id());
        response.setPurchaseReturnLineId(line.purchaseReturnLineId());
        response.setProductId(line.productId());
        response.setQuantity(line.quantity());
        response.setUomId(line.uomId());
        response.setDppAmountOriginal(line.dppAmountOriginal());
        response.setTaxAmountOriginal(line.taxAmountOriginal());
        response.setDppAmountBase(line.dppAmountBase());
        response.setTaxAmountBase(line.taxAmountBase());
        return response;
    }
}
