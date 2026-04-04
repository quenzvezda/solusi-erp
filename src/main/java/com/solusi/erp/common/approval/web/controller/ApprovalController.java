package com.solusi.erp.common.approval.web.controller;

import com.solusi.erp.common.approval.application.usecase.ProcessApprovalUseCase;
import com.solusi.erp.common.approval.domain.model.ApprovalRequest;
import com.solusi.erp.common.approval.domain.repository.ApprovalRequestRepository;
import com.solusi.erp.common.approval.signature.application.usecase.GetApprovalSignatureUrlUseCase;
import com.solusi.erp.common.approval.signature.application.usecase.SaveApprovalSignatureUseCase;
import com.solusi.erp.common.approval.signature.domain.model.ApprovalSignature;
import com.solusi.erp.common.approval.web.dto.ApprovalSignatureResponse;
import com.solusi.erp.common.approval.web.dto.ApprovalStatusResponse;
import com.solusi.erp.common.approval.web.dto.ProcessApprovalRequest;
import com.solusi.erp.common.approval.web.mapper.ApprovalWebMapper;
import com.solusi.erp.core.dto.ApiResponse;
import com.solusi.erp.core.exception.DomainException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * Web controller for Approval actions and timeline display.
 * Endpoints are generic — usable by any module (News, StockAdjustment, etc.).
 */
@Controller
@RequestMapping("/approval")
@RequiredArgsConstructor
public class ApprovalController {

    private final ProcessApprovalUseCase processApprovalUseCase;
    private final ApprovalRequestRepository approvalRequestRepository;
    private final SaveApprovalSignatureUseCase saveApprovalSignatureUseCase;
    private final GetApprovalSignatureUrlUseCase getApprovalSignatureUrlUseCase;
    private final ApprovalWebMapper webMapper;
    private final MessageSource messageSource;

    /**
     * POST /approval/{requestId}/process
     * Approves or rejects an approval request, optionally saving a digital signature.
     */
    @PostMapping("/{requestId}/process")
    @ResponseBody
    public ResponseEntity<ApiResponse<ApprovalStatusResponse>> process(
            @PathVariable Long requestId,
            @Valid @RequestBody ProcessApprovalRequest request,
            @AuthenticationPrincipal UserDetails principal) {

        Long actorId = resolveUserId(principal);

        ApprovalRequest result;
        if ("APPROVE".equalsIgnoreCase(request.getAction())) {
            result = processApprovalUseCase.approve(requestId, actorId, request.getNotes());
            if (request.getSignatureBase64() != null && !request.getSignatureBase64().isBlank()) {
                saveApprovalSignatureUseCase.execute(requestId, request.getSignatureBase64(), actorId);
            }
        } else if ("REJECT".equalsIgnoreCase(request.getAction())) {
            if (request.getNotes() == null || request.getNotes().isBlank()) {
                throw new DomainException("msg.error.approval.reject.notes-required");
            }
            result = processApprovalUseCase.reject(requestId, actorId, request.getNotes());
        } else {
            throw new DomainException("msg.error.approval.invalid-action");
        }

        String msg = messageSource.getMessage("msg.success.update", null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok(ApiResponse.success(msg, webMapper.toStatusResponse(result)));
    }

    /**
     * GET /approval/{requestId}/history
     * Returns an HTMX fragment containing the approval timeline.
     */
    @GetMapping("/{requestId}/history")
    public String history(@PathVariable Long requestId, Model model) {
        ApprovalRequest request = approvalRequestRepository.findById(requestId)
                .orElseThrow(() -> new DomainException("msg.error.approval.not-found"));

        model.addAttribute("approval", webMapper.toStatusResponse(request));
        return "fragments/approval :: timeline";
    }

    /**
     * GET /approval/{requestId}/signature
     * Returns the signature URL for a given approval request (JSON).
     */
    @GetMapping("/{requestId}/signature")
    @ResponseBody
    public ResponseEntity<ApiResponse<ApprovalSignatureResponse>> signature(@PathVariable Long requestId) {
        Optional<ApprovalSignature> sig = getApprovalSignatureUrlUseCase.findByRequestId(requestId);
        if (sig.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.success("No signature found", null));
        }
        return ResponseEntity.ok(ApiResponse.success("OK", webMapper.toSignatureResponse(sig.get())));
    }

    /** Stub — in production resolve the Party/User ID from SecurityUser. */
    private Long resolveUserId(UserDetails principal) {
        return 1L;
    }
}
