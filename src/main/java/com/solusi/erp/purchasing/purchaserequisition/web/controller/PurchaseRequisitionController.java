package com.solusi.erp.purchasing.purchaserequisition.web.controller;

import com.solusi.erp.core.annotation.DefaultRedirectUrl;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.core.infrastructure.util.PageableMapper;
import com.solusi.erp.core.dto.ApiResponse;
import com.solusi.erp.purchasing.purchaserequisition.application.usecase.command.*;
import com.solusi.erp.purchasing.purchaserequisition.application.usecase.query.*;
import com.solusi.erp.purchasing.purchaserequisition.domain.model.PurchaseRequisition;
import com.solusi.erp.purchasing.purchaserequisition.domain.model.PurchaseRequisitionPriority;
import com.solusi.erp.purchasing.purchaserequisition.domain.model.PurchaseRequisitionStatus;
import com.solusi.erp.purchasing.purchaserequisition.web.dto.*;
import com.solusi.erp.purchasing.purchaserequisition.web.mapper.PurchaseRequisitionWebMapper;
import com.solusi.erp.util.HtmxResponseUtility;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/purchasing/purchase-requisitions")
@RequiredArgsConstructor
@DefaultRedirectUrl
public class PurchaseRequisitionController {

    private final CreatePurchaseRequisitionUseCase createPurchaseRequisitionUseCase;
    private final UpdatePurchaseRequisitionUseCase updatePurchaseRequisitionUseCase;
    private final DeletePurchaseRequisitionUseCase deletePurchaseRequisitionUseCase;
    private final SubmitPurchaseRequisitionUseCase submitPurchaseRequisitionUseCase;
    private final CancelPurchaseRequisitionUseCase cancelPurchaseRequisitionUseCase;
    private final FindPurchaseRequisitionsUseCase findPurchaseRequisitionsUseCase;
    private final GetPurchaseRequisitionEditViewUseCase getPurchaseRequisitionEditViewUseCase;
    private final PurchaseRequisitionWebMapper webMapper;
    private final MessageSource messageSource;

    @GetMapping
    @PreAuthorize("hasAuthority('PR_READ')")
    public String list(@RequestParam(required = false) String keyword,
                       org.springframework.data.domain.Pageable springPageable,
                       Model model) {
        Pageable domainPageable = PageableMapper.toDomain(springPageable);
        com.solusi.erp.core.domain.model.Page<PurchaseRequisition> domainPage =
            findPurchaseRequisitionsUseCase.execute(keyword, domainPageable);

        List<PurchaseRequisitionSummaryResponse> content = domainPage.content().stream()
            .map(webMapper::toSummaryResponse)
            .collect(Collectors.toList());

        Page<PurchaseRequisitionSummaryResponse> springPage =
            new PageImpl<>(content, springPageable, domainPage.totalElements());
        model.addAttribute("page", springPage);
        model.addAttribute("keyword", keyword);
        return "purchasing/purchase-requisitions/list";
    }

    @GetMapping("/create")
    @PreAuthorize("hasAuthority('PR_CREATE')")
    public String showCreateForm(Model model) {
        PurchaseRequisitionSaveRequest request = new PurchaseRequisitionSaveRequest();
        request.setRequestDate(LocalDate.now());
        request.setPriority(PurchaseRequisitionPriority.NORMAL);
        model.addAttribute("prRequest", request);
        return "purchasing/purchase-requisitions/form";
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('PR_CREATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<PurchaseRequisitionDetailResponse>> create(
            @Valid @RequestBody PurchaseRequisitionSaveRequest request) {
        List<LineInput> lines = webMapper.toLineInputs(request.getLines());
        PurchaseRequisition domain = createPurchaseRequisitionUseCase.execute(
            request.getRequestDate(), request.getRequesterId(), request.getFacilityId(),
            request.getDepartment(), request.getPriority(),
            request.getNote(), lines
        );
        PurchaseRequisitionDetailResponse data = webMapper.toDetailResponse(domain);
        String msg = messageSource.getMessage("msg.success.create", null, LocaleContextHolder.getLocale());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(msg, data));
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('PR_UPDATE')")
    public String showEditForm(@PathVariable Long id, Model model) {
        PurchaseRequisition domain = getPurchaseRequisitionEditViewUseCase.execute(id)
            .orElseThrow(() -> new RuntimeException("Purchase requisition not found"));
        if (domain.getStatus() != PurchaseRequisitionStatus.DRAFT) {
            return "redirect:/purchasing/purchase-requisitions/view/" + id;
        }
        model.addAttribute("prRequest", webMapper.toSaveRequest(domain));
        model.addAttribute("auditInfo", webMapper.toDetailResponse(domain));
        return "purchasing/purchase-requisitions/form";
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('PR_UPDATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<PurchaseRequisitionDetailResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody PurchaseRequisitionSaveRequest request) {
        List<LineInput> lines = webMapper.toLineInputs(request.getLines());
        PurchaseRequisition domain = updatePurchaseRequisitionUseCase.execute(
            id, request.getRequestDate(), request.getFacilityId(),
            request.getDepartment(), request.getPriority(),
            request.getNote(), lines
        );
        PurchaseRequisitionDetailResponse data = webMapper.toDetailResponse(domain);
        String msg = messageSource.getMessage("msg.success.update", null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok(ApiResponse.success(msg, data));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PR_DELETE')")
    @ResponseBody
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deletePurchaseRequisitionUseCase.execute(id);
        String msg = messageSource.getMessage("msg.success.delete", null, LocaleContextHolder.getLocale());
        return HtmxResponseUtility.okWithRefreshTableAndSuccess(msg);
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAuthority('PR_SUBMIT')")
    @ResponseBody
    public ResponseEntity<ApiResponse<PurchaseRequisitionDetailResponse>> submit(
            @PathVariable Long id,
            @RequestParam Long approverId) {
        PurchaseRequisition domain = submitPurchaseRequisitionUseCase.execute(id, approverId);
        PurchaseRequisitionDetailResponse data = webMapper.toDetailResponse(domain);
        String msg = messageSource.getMessage("msg.success.submit", null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok(ApiResponse.success(msg, data));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('PR_UPDATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<PurchaseRequisitionDetailResponse>> cancel(@PathVariable Long id) {
        PurchaseRequisition domain = cancelPurchaseRequisitionUseCase.execute(id);
        PurchaseRequisitionDetailResponse data = webMapper.toDetailResponse(domain);
        String msg = messageSource.getMessage("msg.success.cancel", null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok(ApiResponse.success(msg, data));
    }

    @GetMapping("/view/{id}")
    @PreAuthorize("hasAuthority('PR_READ')")
    public String view(@PathVariable Long id, Model model) {
        PurchaseRequisition domain = getPurchaseRequisitionEditViewUseCase.execute(id)
            .orElseThrow(() -> new RuntimeException("Purchase requisition not found"));
        model.addAttribute("pr", webMapper.toDetailResponse(domain));
        return "purchasing/purchase-requisitions/view";
    }
}
