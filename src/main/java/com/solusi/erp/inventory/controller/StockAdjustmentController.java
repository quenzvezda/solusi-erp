package com.solusi.erp.inventory.controller;

import com.solusi.erp.core.annotation.DefaultRedirectUrl;
import com.solusi.erp.core.dto.ApiResponse;
import com.solusi.erp.inventory.dto.StockAdjustmentRequest;
import com.solusi.erp.inventory.dto.StockAdjustmentResponse;
import com.solusi.erp.inventory.model.StockAdjustment;
import com.solusi.erp.inventory.service.StockAdjustmentService;
import com.solusi.erp.inventory.service.ProductService;
import com.solusi.erp.inventory.service.ContainerService;
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
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;

@Slf4j
@Controller
@RequestMapping("/inventory/adjustments")
@RequiredArgsConstructor
@DefaultRedirectUrl
public class StockAdjustmentController {

    private final StockAdjustmentService service;
    private final ProductService productService;
    private final ContainerService containerService;
    private final CurrencyService currencyService;
    private final MessageSource messageSource;

    @GetMapping
    @PreAuthorize("hasAuthority('STOCK-ADJUSTMENT_READ')")
    public String list(@RequestParam(value = "search", required = false) String keyword,
                     @PageableDefault(size = 10) Pageable pageable,
                     Model model) {
        Page<StockAdjustmentResponse> page = service.findAll(keyword, pageable);
        model.addAttribute("page", page);
        model.addAttribute("search", keyword);
        return "inventory/adjustments/list";
    }

    @GetMapping("/create")
    @PreAuthorize("hasAuthority('STOCK-ADJUSTMENT_CREATE')")
    public String createForm(Model model) {
        CurrencyResponse defaultCurr = currencyService.getDefaultCurrency();
        StockAdjustmentRequest request = new StockAdjustmentRequest();
        if (defaultCurr != null) {
            request.setCurrencyId(defaultCurr.getId());
        }
        request.setExchangeRate(BigDecimal.ONE);
        request.setTransactionDate(LocalDate.now());
        
        model.addAttribute("stockAdjustment", request);
        populateFormModels(model);
        return "inventory/adjustments/form";
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('STOCK-ADJUSTMENT_CREATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<StockAdjustmentResponse>> create(@Valid @RequestBody StockAdjustmentRequest request) {
        StockAdjustmentResponse data = service.create(request);
        String message = messageSource.getMessage("msg.success.create", null, LocaleContextHolder.getLocale());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(message, data));
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('STOCK-ADJUSTMENT_UPDATE')")
    public String editForm(@PathVariable Long id, Model model) {
        StockAdjustmentResponse response = service.findById(id);
        if (response.getStatus() == StockAdjustment.AdjustmentStatus.COMPLETED) {
            return "redirect:/inventory/adjustments/" + id;
        }
        
        model.addAttribute("stockAdjustment", service.getEditData(id));
        model.addAttribute("auditInfo", response);
        populateFormModels(model);
        return "inventory/adjustments/form";
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('STOCK-ADJUSTMENT_UPDATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<StockAdjustmentResponse>> update(@PathVariable Long id,
                                                                       @Valid @RequestBody StockAdjustmentRequest request) {
        StockAdjustmentResponse data = service.update(id, request);
        String message = messageSource.getMessage("msg.success.update", null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok(ApiResponse.success(message, data));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('STOCK-ADJUSTMENT_READ')")
    public String view(@PathVariable Long id, Model model) {
        model.addAttribute("stockAdjustment", service.findById(id));
        return "inventory/adjustments/view";
    }

    @PostMapping("/{id}/process")
    @PreAuthorize("hasAuthority('STOCK-ADJUSTMENT_PROCESS')")
    public String process(@PathVariable Long id, RedirectAttributes ra) {
        service.process(id);
        ra.addFlashAttribute("message", "Stock Adjustment processed successfully");
        return "redirect:/inventory/adjustments/" + id;
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('STOCK-ADJUSTMENT_DELETE')")
    @ResponseBody
    public ResponseEntity<Void> deleteHtmx(@PathVariable Long id) {
        service.delete(id);
        String msg = messageSource.getMessage("msg.success.delete", null, LocaleContextHolder.getLocale());
        return HtmxResponseUtility.okWithRefreshTableAndSuccess(msg);
    }

    private void populateFormModels(Model model) {
        model.addAttribute("currencies", currencyService.findAllActive());
        model.addAttribute("defaultCurrency", currencyService.getDefaultCurrency());
    }
}
