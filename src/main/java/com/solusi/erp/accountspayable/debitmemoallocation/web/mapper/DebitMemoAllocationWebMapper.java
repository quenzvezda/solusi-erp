package com.solusi.erp.accountspayable.debitmemoallocation.web.mapper;

import com.solusi.erp.accountspayable.debitmemoallocation.application.usecase.command.CreateDebitMemoAllocationCommand;
import com.solusi.erp.accountspayable.debitmemoallocation.application.usecase.command.DebitMemoAllocationLineCommand;
import com.solusi.erp.accountspayable.debitmemoallocation.application.usecase.command.ReverseDebitMemoAllocationCommand;
import com.solusi.erp.accountspayable.debitmemoallocation.application.usecase.command.UpdateDebitMemoAllocationCommand;
import com.solusi.erp.accountspayable.debitmemoallocation.application.usecase.query.DebitMemoAllocationDetailView;
import com.solusi.erp.accountspayable.debitmemoallocation.application.usecase.query.DebitMemoAllocationLineView;
import com.solusi.erp.accountspayable.debitmemoallocation.application.usecase.query.DebitMemoAllocationSummaryView;
import com.solusi.erp.accountspayable.debitmemoallocation.domain.port.DebitMemoAllocationSourcePort;
import com.solusi.erp.accountspayable.debitmemoallocation.web.dto.*;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class DebitMemoAllocationWebMapper {

    public DebitMemoAllocationSummaryResponse toSummaryResponse(DebitMemoAllocationSummaryView view) {
        DebitMemoAllocationSummaryResponse response = new DebitMemoAllocationSummaryResponse();
        response.setId(view.id());
        response.setCode(view.code());
        response.setDebitMemoId(view.debitMemoId());
        response.setDebitMemoCode(view.debitMemoCode());
        response.setAllocationDate(view.allocationDate());
        response.setStatus(view.status() != null ? view.status().name() : null);
        response.setTotalAppliedGrossOriginal(view.totalAppliedGrossOriginal());
        response.setTotalApReductionBase(view.totalApReductionBase());
        response.setTotalFxLossBase(view.totalFxLossBase());
        response.setTotalFxGainBase(view.totalFxGainBase());
        return response;
    }

    public DebitMemoAllocationDetailResponse toDetailResponse(DebitMemoAllocationDetailView view) {
        DebitMemoAllocationDetailResponse response = new DebitMemoAllocationDetailResponse();
        response.setId(view.id());
        response.setCode(view.code());
        response.setDebitMemoId(view.debitMemoId());
        response.setDebitMemoCode(view.debitMemoCode());
        response.setAllocationDate(view.allocationDate());
        response.setStatus(view.status() != null ? view.status().name() : null);
        response.setTotalAppliedGrossOriginal(view.totalAppliedGrossOriginal());
        response.setTotalDppOriginal(view.totalDppOriginal());
        response.setTotalTaxOriginal(view.totalTaxOriginal());
        response.setTotalGrirReversalBase(view.totalGrirReversalBase());
        response.setTotalTaxReversalBase(view.totalTaxReversalBase());
        response.setTotalApReductionBase(view.totalApReductionBase());
        response.setTotalFxLossBase(view.totalFxLossBase());
        response.setTotalFxGainBase(view.totalFxGainBase());
        response.setApplyJournalEntryId(view.applyJournalEntryId());
        response.setReversalJournalEntryId(view.reversalJournalEntryId());
        response.setReversalDate(view.reversalDate());
        response.setReversalReason(view.reversalReason());
        response.setNotes(view.notes());
        response.setLines(toLineResponses(view.lines()));
        return response;
    }

    public DebitMemoAllocationSaveRequest toSaveRequest(DebitMemoAllocationDetailView view) {
        DebitMemoAllocationSaveRequest request = new DebitMemoAllocationSaveRequest();
        request.setId(view.id());
        request.setDebitMemoId(view.debitMemoId());
        request.setDebitMemoCode(view.debitMemoCode());
        request.setAllocationDate(view.allocationDate());
        request.setNotes(view.notes());
        request.setLines(view.lines() == null ? List.of() : view.lines().stream().map(this::toLineRequest).toList());
        return request;
    }

    public DebitMemoAllocationSaveRequest newSaveRequest(Long debitMemoId, String debitMemoCode, Long vendorBillId) {
        DebitMemoAllocationSaveRequest request = new DebitMemoAllocationSaveRequest();
        request.setDebitMemoId(debitMemoId);
        request.setDebitMemoCode(debitMemoCode);
        request.setAllocationDate(LocalDate.now());
        if (vendorBillId != null) {
            DebitMemoAllocationLineRequest line = new DebitMemoAllocationLineRequest();
            line.setVendorBillId(vendorBillId);
            request.setLines(List.of(line));
        }
        return request;
    }

    public CreateDebitMemoAllocationCommand toCreateCommand(DebitMemoAllocationSaveRequest request) {
        return new CreateDebitMemoAllocationCommand(
                request.getDebitMemoId(),
                request.getAllocationDate(),
                request.getNotes(),
                toLineCommands(request.getLines())
        );
    }

    public UpdateDebitMemoAllocationCommand toUpdateCommand(Long id, DebitMemoAllocationSaveRequest request) {
        return new UpdateDebitMemoAllocationCommand(
                id,
                request.getAllocationDate(),
                request.getNotes(),
                toLineCommands(request.getLines())
        );
    }

    public ReverseDebitMemoAllocationCommand toReverseCommand(Long id, DebitMemoAllocationReverseRequest request) {
        return new ReverseDebitMemoAllocationCommand(id, request.getReversalDate(), request.getReversalReason());
    }

    public EligibleVendorBillSelectorRow toVendorBillSelectorRow(DebitMemoAllocationSourcePort.EligibleVendorBill view) {
        return new EligibleVendorBillSelectorRow(
                view.id(),
                view.code(),
                view.totalAmount(),
                view.outstandingAmount(),
                view.exchangeRate()
        );
    }

    public EligibleDebitMemoSelectorRow toDebitMemoSelectorRow(DebitMemoAllocationSourcePort.EligibleDebitMemo view) {
        return new EligibleDebitMemoSelectorRow(
                view.id(),
                view.code(),
                view.grossAmountOriginal(),
                view.remainingAmountOriginal()
        );
    }

    private List<DebitMemoAllocationLineResponse> toLineResponses(List<DebitMemoAllocationLineView> lines) {
        if (lines == null) return List.of();
        return lines.stream().map(this::toLineResponse).toList();
    }

    private DebitMemoAllocationLineResponse toLineResponse(DebitMemoAllocationLineView line) {
        DebitMemoAllocationLineResponse response = new DebitMemoAllocationLineResponse();
        response.setId(line.id());
        response.setVendorBillId(line.vendorBillId());
        response.setVendorBillCode(line.vendorBillCode());
        response.setDebitMemoRemainingAtDraft(line.debitMemoRemainingAtDraft());
        response.setVendorBillOutstandingAtDraft(line.vendorBillOutstandingAtDraft());
        response.setAppliedGrossOriginal(line.appliedGrossOriginal());
        response.setAppliedDppOriginal(line.appliedDppOriginal());
        response.setAppliedTaxOriginal(line.appliedTaxOriginal());
        response.setGrirReversalBase(line.grirReversalBase());
        response.setTaxReversalBase(line.taxReversalBase());
        response.setVendorBillExchangeRate(line.vendorBillExchangeRate());
        response.setApReductionBase(line.apReductionBase());
        response.setFxLossBase(line.fxLossBase());
        response.setFxGainBase(line.fxGainBase());
        return response;
    }

    private DebitMemoAllocationLineRequest toLineRequest(DebitMemoAllocationLineView line) {
        DebitMemoAllocationLineRequest request = new DebitMemoAllocationLineRequest();
        request.setId(line.id());
        request.setVendorBillId(line.vendorBillId());
        request.setVendorBillCode(line.vendorBillCode());
        request.setDebitMemoRemainingAtDraft(line.debitMemoRemainingAtDraft());
        request.setVendorBillOutstandingAtDraft(line.vendorBillOutstandingAtDraft());
        request.setAppliedGrossOriginal(line.appliedGrossOriginal());
        return request;
    }

    private List<DebitMemoAllocationLineCommand> toLineCommands(List<DebitMemoAllocationLineRequest> lines) {
        if (lines == null) return List.of();
        return lines.stream()
                .map(line -> new DebitMemoAllocationLineCommand(line.getVendorBillId(), line.getAppliedGrossOriginal()))
                .toList();
    }
}
