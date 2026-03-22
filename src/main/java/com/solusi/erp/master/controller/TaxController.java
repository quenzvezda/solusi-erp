package com.solusi.erp.master.controller;

import com.solusi.erp.core.annotation.DefaultRedirectUrl;
import com.solusi.erp.core.dto.ApiResponse;
import com.solusi.erp.master.dto.TaxRequest;
import com.solusi.erp.master.dto.TaxResponse;
import com.solusi.erp.master.service.TaxService;
import com.solusi.erp.util.HtmxResponseUtility;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequestMapping("/master/taxes")
@RequiredArgsConstructor
@DefaultRedirectUrl
public class TaxController {

    private final TaxService taxService;
    private final MessageSource messageSource;

    @GetMapping
    @PreAuthorize("hasAuthority('TAX_READ')")
    public String listTaxes(Model model,
            @RequestParam(value = "keyword", required = false) String keyword,
            Pageable pageable) {
        Page<TaxResponse> taxes = taxService.getAllTaxes(keyword, pageable);
        model.addAttribute("page", taxes);
        model.addAttribute("keyword", keyword);
        return "master/tax/list";
    }

    @GetMapping("/create")
    @PreAuthorize("hasAuthority('TAX_CREATE')")
    public String showCreateForm(Model model) {
        TaxRequest request = new TaxRequest();
        request.setIsActive(true);
        request.setIsSubtract(false);
        model.addAttribute("tax", request);
        return "master/tax/form";
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('TAX_CREATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<TaxResponse>> createTax(@Valid @RequestBody TaxRequest request) {
        TaxResponse data = taxService.createTax(request);
        String message = messageSource.getMessage("tax.create.success", null, LocaleContextHolder.getLocale());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(message, data));
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('TAX_UPDATE')")
    public String showEditForm(@PathVariable Long id, Model model) {
        var viewDto = taxService.getTaxEditView(id);
        model.addAttribute("tax", viewDto.getRequest());
        model.addAttribute("auditInfo", viewDto.getAudit());
        return "master/tax/form";
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('TAX_UPDATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<TaxResponse>> updateTax(@PathVariable Long id,
            @Valid @RequestBody TaxRequest request) {
        TaxResponse data = taxService.updateTax(id, request);
        String message = messageSource.getMessage("tax.update.success", null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok(ApiResponse.success(message, data));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('TAX_DELETE')")
    @ResponseBody
    public ResponseEntity<Void> deleteTaxHtmx(@PathVariable Long id) {
        taxService.deleteTax(id);
        String message = messageSource.getMessage("msg.success.delete", null, LocaleContextHolder.getLocale());
        return HtmxResponseUtility.okWithRefreshTableAndSuccess(message);
    }
}
