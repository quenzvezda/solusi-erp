package com.solusi.erp.accountspayable.vendorpayment.web.mapper;

import com.solusi.erp.accountspayable.vendorpayment.domain.model.VendorPayment;
import com.solusi.erp.accountspayable.vendorpayment.domain.model.VendorPaymentLine;
import com.solusi.erp.accountspayable.vendorpayment.domain.port.PayableVendorBillQueryPort.PayableVendorBillView;
import com.solusi.erp.accountspayable.vendorpayment.web.dto.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class VendorPaymentWebMapper {

    public VendorPaymentSummaryResponse toSummaryResponse(VendorPayment domain) {
        VendorPaymentSummaryResponse res = new VendorPaymentSummaryResponse();
        res.setId(domain.getId());
        res.setCode(domain.getCode());
        res.setVendorId(domain.getVendorId());
        res.setPaymentDate(domain.getPaymentDate());
        res.setPaymentAmount(domain.getPaymentAmount());
        res.setStatus(domain.getStatus() != null ? domain.getStatus().name() : null);
        return res;
    }

    public VendorPaymentDetailResponse toDetailResponse(VendorPayment domain) {
        VendorPaymentDetailResponse res = new VendorPaymentDetailResponse();
        res.setId(domain.getId());
        res.setCode(domain.getCode());
        res.setVendorId(domain.getVendorId());
        res.setCurrencyId(domain.getCurrencyId());
        res.setBankAccountId(domain.getBankAccountId());
        res.setPaymentDate(domain.getPaymentDate());
        res.setExchangeRate(domain.getExchangeRate());
        res.setPaymentAmount(domain.getPaymentAmount());
        res.setStatus(domain.getStatus() != null ? domain.getStatus().name() : null);
        res.setReference(domain.getReference());
        res.setNotes(domain.getNotes());
        res.setLines(toLineResponses(domain.getLines()));
        return res;
    }

    public VendorPaymentSaveRequest toSaveRequest(VendorPayment domain) {
        VendorPaymentSaveRequest req = new VendorPaymentSaveRequest();
        req.setId(domain.getId());
        req.setVendorId(domain.getVendorId());
        req.setCurrencyId(domain.getCurrencyId());
        req.setBankAccountId(domain.getBankAccountId());
        req.setPaymentDate(domain.getPaymentDate());
        req.setExchangeRate(domain.getExchangeRate());
        req.setPaymentAmount(domain.getPaymentAmount());
        req.setReference(domain.getReference());
        req.setNotes(domain.getNotes());
        req.setLines(toLineRequests(domain.getLines()));
        return req;
    }

    public List<VendorPaymentLine> toLinesDomain(List<VendorPaymentLineRequest> lines) {
        if (lines == null) return List.of();
        return lines.stream().map(this::toLineDomain).toList();
    }

    public PayableVendorBillResponse toPayableResponse(PayableVendorBillView view) {
        PayableVendorBillResponse res = new PayableVendorBillResponse();
        res.setVendorBillId(view.vendorBillId());
        res.setBillCode(view.billCode());
        res.setTotalAmount(view.totalAmount());
        res.setPaidAmount(view.paidAmount());
        res.setOutstandingAmount(view.outstandingAmount());
        return res;
    }

    private VendorPaymentLine toLineDomain(VendorPaymentLineRequest req) {
        return new VendorPaymentLine(req.getId(), req.getVendorBillId(), req.getBillCode(),
                req.getOutstandingAmount(), req.getPaidAmount());
    }

    private List<VendorPaymentLineResponse> toLineResponses(List<VendorPaymentLine> lines) {
        if (lines == null) return List.of();
        return lines.stream().map(this::toLineResponse).toList();
    }

    private VendorPaymentLineResponse toLineResponse(VendorPaymentLine line) {
        VendorPaymentLineResponse res = new VendorPaymentLineResponse();
        res.setId(line.getId());
        res.setVendorBillId(line.getVendorBillId());
        res.setBillCode(line.getBillCode());
        res.setOutstandingAmount(line.getOutstandingAmount());
        res.setPaidAmount(line.getPaidAmount());
        return res;
    }

    private List<VendorPaymentLineRequest> toLineRequests(List<VendorPaymentLine> lines) {
        if (lines == null) return List.of();
        return lines.stream().map(line -> {
            VendorPaymentLineRequest req = new VendorPaymentLineRequest();
            req.setId(line.getId());
            req.setVendorBillId(line.getVendorBillId());
            req.setBillCode(line.getBillCode());
            req.setOutstandingAmount(line.getOutstandingAmount());
            req.setPaidAmount(line.getPaidAmount());
            return req;
        }).toList();
    }
}
