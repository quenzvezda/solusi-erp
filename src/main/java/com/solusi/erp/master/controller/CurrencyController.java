package com.solusi.erp.master.controller;

import com.solusi.erp.core.dto.ApiResponse;
import com.solusi.erp.master.dto.CurrencyRequest;
import com.solusi.erp.master.dto.CurrencyResponse;
import com.solusi.erp.master.service.CurrencyService;
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
@RequestMapping("/master/currencies")
@RequiredArgsConstructor
public class CurrencyController {

    private final CurrencyService currencyService;
    private final MessageSource messageSource;

    @GetMapping
    @PreAuthorize("hasAuthority('CURRENCY_READ')")
    public String listCurrencies(Model model,
            @RequestParam(value = "keyword", required = false) String keyword,
            Pageable pageable) {
        Page<CurrencyResponse> currencies = currencyService.getAllCurrencies(keyword, pageable);
        model.addAttribute("page", currencies);
        model.addAttribute("keyword", keyword);
        return "master/currency/list";
    }

    @GetMapping("/create")
    @PreAuthorize("hasAuthority('CURRENCY_CREATE')")
    public String showCreateForm(Model model) {
        CurrencyRequest request = new CurrencyRequest();
        request.setIsActive(true);
        request.setIsDefault(false);
        model.addAttribute("currency", request);
        return "master/currency/form";
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('CURRENCY_CREATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<CurrencyResponse>> createCurrency(@Valid @RequestBody CurrencyRequest request) {
        CurrencyResponse data = currencyService.createCurrency(request);
        String msg = messageSource.getMessage("currency.create.success", null, LocaleContextHolder.getLocale());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(msg, data));
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('CURRENCY_UPDATE')")
    public String showEditForm(@PathVariable Long id, Model model) {
        CurrencyRequest request = currencyService.getEditData(id);
        CurrencyResponse responseDto = currencyService.getCurrencyById(id);
        
        model.addAttribute("id", id);
        model.addAttribute("currency", request);
        model.addAttribute("auditInfo", responseDto);
        
        return "master/currency/form";
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('CURRENCY_UPDATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<CurrencyResponse>> updateCurrency(@PathVariable Long id,
            @Valid @RequestBody CurrencyRequest request) {
        CurrencyResponse data = currencyService.updateCurrency(id, request);
        String msg = messageSource.getMessage("currency.update.success", null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok(ApiResponse.success(msg, data));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('CURRENCY_DELETE')")
    @ResponseBody
    public ResponseEntity<Void> deleteCurrencyHtmx(@PathVariable Long id) {
        currencyService.deleteCurrency(id);
        String msg = messageSource.getMessage("msg.success.delete", null, LocaleContextHolder.getLocale());
        return HtmxResponseUtility.okWithRefreshTableAndSuccess(msg);
    }
}
