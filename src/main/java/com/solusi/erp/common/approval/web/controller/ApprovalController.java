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
import com.solusi.erp.core.annotation.DefaultRedirectUrl;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.core.dto.ApiResponse;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.core.infrastructure.util.PageableMapper;
import com.solusi.erp.security.shared.model.SecurityUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Web controller for Approval actions and timeline display.
 * Endpoints are generic — usable by any module (News, StockAdjustment, etc.).
 */
@Controller
@RequestMapping("/common/approval")
@RequiredArgsConstructor
@DefaultRedirectUrl
public class ApprovalController {

    private final ProcessApprovalUseCase processApprovalUseCase;
    private final ApprovalRequestRepository approvalRequestRepository;
    private final SaveApprovalSignatureUseCase saveApprovalSignatureUseCase;
    private final GetApprovalSignatureUrlUseCase getApprovalSignatureUrlUseCase;
    private final ApprovalWebMapper webMapper;
    private final MessageSource messageSource;

    /**
     * GET /common/approval
     * Shows list of PENDING approvals.
     */
    @GetMapping
    @PreAuthorize("hasAuthority('APPROVAL_READ')")
    public String list(org.springframework.data.domain.Pageable springPageable, Model model,
                       @AuthenticationPrincipal UserDetails principal) {
        Long currentApproverPartyId = null;
        if (principal instanceof SecurityUser securityUser) {
            currentApproverPartyId = securityUser.user().getPartyId();
        }

        Pageable domainPageable = PageableMapper.toDomain(springPageable);
        com.solusi.erp.core.domain.model.Page<ApprovalRequest> domainPage;
        if (currentApproverPartyId != null) {
            domainPage = approvalRequestRepository.findPendingApprovalsForApprover(currentApproverPartyId, domainPageable);
        } else {
            domainPage = approvalRequestRepository.findPendingApprovals(domainPageable);
        }

        List<ApprovalStatusResponse> content = domainPage.content().stream()
                .map(webMapper::toStatusResponse)
                .collect(Collectors.toList());

        Page<ApprovalStatusResponse> springPage = new PageImpl<>(content, springPageable, domainPage.totalElements());
        model.addAttribute("page", springPage);
        return "common/approval/list";
    }

    /**
     * POST /common/approval/{requestId}/process
     * Approves or rejects an approval request, optionally saving a digital signature.
     */
    @PostMapping("/{requestId}/process")
    @PreAuthorize("hasAuthority('APPROVAL_PROCESS')")
    @ResponseBody
    public ResponseEntity<ApiResponse<ApprovalStatusResponse>> process(
            @PathVariable Long requestId,
            @Valid @RequestBody ProcessApprovalRequest request,
            @AuthenticationPrincipal UserDetails principal) {

        Long actorPartyId = resolvePartyId(principal);
        String action = request.getAction().toUpperCase();

        ApprovalRequest result = switch (action) {
            case "APPROVE_AND_FINISH" -> {
                ApprovalRequest r = processApprovalUseCase.approve(requestId, actorPartyId, request.getNotes());
                if (request.getSignatureBase64() != null && !request.getSignatureBase64().isBlank()) {
                    saveApprovalSignatureUseCase.execute(requestId, request.getSignatureBase64(), actorPartyId);
                }
                yield r;
            }
            case "APPROVE_AND_FORWARD" -> {
                ApprovalRequest r = processApprovalUseCase.approveAndForward(requestId, actorPartyId, request.getTargetApproverId(), request.getNotes());
                if (request.getSignatureBase64() != null && !request.getSignatureBase64().isBlank()) {
                    saveApprovalSignatureUseCase.execute(requestId, request.getSignatureBase64(), actorPartyId);
                }
                yield r;
            }
            case "FORWARD" -> processApprovalUseCase.forward(requestId, actorPartyId, request.getTargetApproverId(), request.getNotes());
            case "REJECTED" -> processApprovalUseCase.reject(requestId, actorPartyId, request.getNotes());
            default -> throw new DomainException("msg.error.approval.invalid-action");
        };

        String msg = messageSource.getMessage("msg.success.update", null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok(ApiResponse.success(msg, webMapper.toStatusResponse(result)));
    }

    /**
     * GET /common/approval/{requestId}/history
     * Returns an HTMX fragment containing the approval timeline.
     */
    @GetMapping("/{requestId}/history")
    public String history(@PathVariable Long requestId, Model model,
                          @AuthenticationPrincipal UserDetails principal) {
        ApprovalRequest request = approvalRequestRepository.findById(requestId)
                .orElseThrow(() -> new DomainException("msg.error.approval.not-found"));
        model.addAttribute("approval", webMapper.toStatusResponse(request));

        boolean isCurrentApprover = false;
        if (principal instanceof SecurityUser securityUser) {
            Long partyId = securityUser.user().getPartyId();
            isCurrentApprover = partyId != null && partyId.equals(request.getCurrentApproverId());
        }
        model.addAttribute("isCurrentApprover", isCurrentApprover);
        return "fragments/approval :: timeline";
    }

    /**
     * GET /common/approval/{requestId}/signature
     * Returns the signature URL for a given approval request (JSON).
     */
    @GetMapping("/{requestId}/signature")
    @PreAuthorize("hasAuthority('APPROVAL_READ')")
    @ResponseBody
    public ResponseEntity<ApiResponse<ApprovalSignatureResponse>> signature(@PathVariable Long requestId) {
        Optional<ApprovalSignature> sig = getApprovalSignatureUrlUseCase.findByRequestId(requestId);
        if (sig.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.success("No signature found", null));
        }
        return ResponseEntity.ok(ApiResponse.success("OK", webMapper.toSignatureResponse(sig.get())));
    }

    private Long resolvePartyId(UserDetails principal) {
        if (principal instanceof SecurityUser securityUser) {
            Long partyId = securityUser.user().getPartyId();
            if (partyId == null) {
                throw new DomainException("msg.error.approval.no-party-linked");
            }
            return partyId;
        }
        throw new DomainException("msg.error.approval.no-party-linked");
    }
}
