package com.solusi.erp.inventory.controller;

import com.solusi.erp.inventory.dto.StockCardFilter;
import com.solusi.erp.inventory.service.InventoryReportService;
import com.solusi.erp.inventory.service.ProductService;
import com.solusi.erp.inventory.service.ContainerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/inventory/reports")
@RequiredArgsConstructor
public class InventoryReportController {

    private final InventoryReportService reportService;
    private final ProductService productService;
    private final ContainerService containerService;

    @GetMapping("/on-hand")
    @PreAuthorize("hasAuthority('ON-HAND_READ')")
    public String onHandList(@RequestParam(value = "search", required = false) String keyword,
                            @PageableDefault(size = 10) Pageable pageable,
                            Model model) {
        model.addAttribute("page", reportService.getOnHandSummary(keyword, pageable));
        model.addAttribute("search", keyword);
        return "inventory/reports/on-hand/list";
    }

    @GetMapping("/on-hand/{productId}")
    @PreAuthorize("hasAuthority('ON-HAND_READ')")
    public String onHandDetail(@PathVariable Long productId, Model model) {
        model.addAttribute("product", productService.getProductById(productId));
        model.addAttribute("details", reportService.getOnHandDetail(productId));
        return "inventory/reports/on-hand/detail";
    }

    @GetMapping("/stock-card")
    @PreAuthorize("hasAuthority('STOCK-CARD_READ')")
    public String stockCard(@ModelAttribute("filter") StockCardFilter filter,
                           @PageableDefault(size = 20) Pageable pageable,
                           Model model) {
        model.addAttribute("page", reportService.getStockCard(filter, pageable));
        model.addAttribute("products", productService.findAll());
        model.addAttribute("containers", containerService.findAll());
        return "inventory/reports/stock-card/list";
    }
}
