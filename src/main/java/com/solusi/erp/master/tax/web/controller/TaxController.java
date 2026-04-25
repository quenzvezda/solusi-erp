package com.solusi.erp.master.tax.web.controller;

import com.solusi.erp.core.annotation.DefaultRedirectUrl;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.core.infrastructure.util.PageableMapper;
import com.solusi.erp.core.dto.ApiResponse;
import com.solusi.erp.core.domain.model.DeleteResult;
import com.solusi.erp.master.tax.application.usecase.command.CreateTaxUseCase;
import com.solusi.erp.master.tax.application.usecase.command.DeleteTaxUseCase;
import com.solusi.erp.master.tax.application.usecase.command.UpdateTaxUseCase;
import com.solusi.erp.master.tax.application.usecase.query.FindTaxesUseCase;
import com.solusi.erp.master.tax.application.usecase.query.GetTaxEditViewUseCase;
import com.solusi.erp.master.tax.domain.model.Tax;
import com.solusi.erp.master.tax.domain.model.TaxCalculationMode;
import com.solusi.erp.master.tax.web.dto.TaxDetailResponse;
import com.solusi.erp.master.tax.web.dto.TaxSaveRequest;
import com.solusi.erp.master.tax.web.dto.TaxSummaryResponse;
import com.solusi.erp.master.tax.web.mapper.TaxWebMapper;
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

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for Tax CRUD.
 */
@Controller
@RequestMapping("/master/taxes")
@RequiredArgsConstructor
@DefaultRedirectUrl
public class TaxController {

    private final CreateTaxUseCase createTaxUseCase;
    private final UpdateTaxUseCase updateTaxUseCase;
    private final DeleteTaxUseCase deleteTaxUseCase;
    private final FindTaxesUseCase findTaxesUseCase;
    private final GetTaxEditViewUseCase getTaxEditViewUseCase;
    private final TaxWebMapper webMapper;
    private final MessageSource messageSource;

    @GetMapping
    @PreAuthorize("hasAuthority('TAX_READ')")
    public String list(@RequestParam(required = false) String keyword,
                       org.springframework.data.domain.Pageable springPageable,
                       Model model) {
        Pageable domainPageable = PageableMapper.toDomain(springPageable);
        com.solusi.erp.core.domain.model.Page<Tax> domainPage = findTaxesUseCase.execute(keyword, domainPageable);

        List<TaxSummaryResponse> content = domainPage.content().stream()
                .map(webMapper::toSummaryResponse)
                .collect(Collectors.toList());

        Page<TaxSummaryResponse> springPage = new PageImpl<>(content, springPageable, domainPage.totalElements());
        model.addAttribute("page", springPage);
        model.addAttribute("keyword", keyword);
        return "master/tax/list";
    }

    @GetMapping("/create")
    @PreAuthorize("hasAuthority('TAX_CREATE')")
    public String showCreateForm(Model model) {
        TaxSaveRequest request = new TaxSaveRequest();
        request.setIsActive(true);
        request.setIsSubtract(false);
        request.setCalculationMode(TaxCalculationMode.EXCLUSIVE);
        model.addAttribute("taxRequest", request);
        return "master/tax/form";
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('TAX_CREATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<TaxDetailResponse>> create(@Valid @RequestBody TaxSaveRequest request) {
        Tax domain = createTaxUseCase.execute(
                request.getCode(), request.getName(), request.getRate(),
                request.getNote(), request.getIsSubtract(), request.getIsActive(), request.getCalculationMode());
        TaxDetailResponse data = webMapper.toDetailResponse(domain);
        String msg = messageSource.getMessage("msg.success.create", null, LocaleContextHolder.getLocale());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(msg, data));
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('TAX_UPDATE')")
    public String showEditForm(@PathVariable Long id, Model model) {
        Tax domain = getTaxEditViewUseCase.execute(id)
                .orElseThrow(() -> new RuntimeException("Tax not found"));
        model.addAttribute("taxRequest", webMapper.toSaveRequest(domain));
        model.addAttribute("auditInfo", webMapper.toDetailResponse(domain));
        return "master/tax/form";
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('TAX_UPDATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<TaxDetailResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody TaxSaveRequest request) {
        Tax domain = updateTaxUseCase.execute(
                id, request.getName(), request.getRate(),
                request.getNote(), request.getIsSubtract(), request.getIsActive(), request.getCalculationMode());
        TaxDetailResponse data = webMapper.toDetailResponse(domain);
        String msg = messageSource.getMessage("msg.success.update", null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok(ApiResponse.success(msg, data));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('TAX_DELETE')")
    @ResponseBody
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        DeleteResult result = deleteTaxUseCase.execute(id);
        if (result == DeleteResult.SOFT_DELETED) {
            String msg = messageSource.getMessage("msg.success.deactivated", null, LocaleContextHolder.getLocale());
            return HtmxResponseUtility.okWithRefreshTableAndWarning(msg);
        }
        String msg = messageSource.getMessage("msg.success.delete", null, LocaleContextHolder.getLocale());
        return HtmxResponseUtility.okWithRefreshTableAndSuccess(msg);
    }
}
