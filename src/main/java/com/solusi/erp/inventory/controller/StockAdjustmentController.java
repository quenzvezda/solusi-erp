package com.solusi.erp.inventory.controller;

import com.solusi.erp.inventory.dto.StockAdjustmentRequest;
import com.solusi.erp.inventory.dto.StockAdjustmentResponse;
import com.solusi.erp.inventory.model.StockAdjustment;
import com.solusi.erp.inventory.service.StockAdjustmentService;
import com.solusi.erp.inventory.service.ProductService;
import com.solusi.erp.inventory.service.ContainerService;
import com.solusi.erp.master.service.CurrencyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequestMapping("/inventory/adjustments")
@RequiredArgsConstructor
public class StockAdjustmentController {

    private final StockAdjustmentService service;
    private final ProductService productService;
    private final ContainerService containerService;
    private final CurrencyService currencyService;

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
        com.solusi.erp.master.dto.CurrencyResponse defaultCurr = currencyService.getDefaultCurrency();
        StockAdjustmentRequest request = new StockAdjustmentRequest();
        if (defaultCurr != null) {
            request.setCurrencyId(defaultCurr.getId());
        }
        request.setExchangeRate(java.math.BigDecimal.ONE);
        request.setTransactionDate(java.time.LocalDate.now());
        
        model.addAttribute("stockAdjustment", request);
        populateFormModels(model);
        return "inventory/adjustments/form";
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('STOCK-ADJUSTMENT_CREATE')")
    public String create(@Valid @ModelAttribute("stockAdjustment") StockAdjustmentRequest request,
                        BindingResult result, Model model, RedirectAttributes ra) {
        log.info("Creating stock adjustment with {} lines", request.getLines().size());
        if (result.hasErrors()) {
            log.warn("Validation errors: {}", result.getAllErrors());
            populateFormModels(model);
            return "inventory/adjustments/form";
        }
        try {
            service.create(request);
            ra.addFlashAttribute("message", "Stock Adjustment created successfully");
            return "redirect:/inventory/adjustments";
        } catch (Exception e) {
            log.error("Error creating adjustment", e);
            ra.addFlashAttribute("error", e.getMessage());
            populateFormModels(model);
            return "inventory/adjustments/form";
        }
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasAuthority('STOCK-ADJUSTMENT_UPDATE')")
    public String editForm(@PathVariable Long id, Model model) {
        StockAdjustmentResponse response = service.findById(id);
        if (response.getStatus() == StockAdjustment.AdjustmentStatus.COMPLETED) {
            return "redirect:/inventory/adjustments/" + id;
        }
        model.addAttribute("stockAdjustment", service.getEditData(id));
        populateFormModels(model);
        return "inventory/adjustments/form";
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
        try {
            service.process(id);
            ra.addFlashAttribute("message", "Stock Adjustment processed successfully");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/inventory/adjustments/" + id;
    }

    private void populateFormModels(Model model) {
        model.addAttribute("currencies", currencyService.findAllActive());
        model.addAttribute("defaultCurrency", currencyService.getDefaultCurrency());
    }
}
