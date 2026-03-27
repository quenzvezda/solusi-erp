package com.solusi.erp.inventory.controller;

import com.solusi.erp.inventory.dto.StockCardFilter;
import com.solusi.erp.inventory.product.application.usecase.query.FindProductsUseCase;
import com.solusi.erp.inventory.product.application.usecase.query.GetProductUseCase;
import com.solusi.erp.inventory.product.web.mapper.ProductWebMapper;
import com.solusi.erp.inventory.service.InventoryReportService;
import com.solusi.erp.inventory.container.application.usecase.query.GetContainerLookupUseCase;
import com.solusi.erp.core.domain.model.Pageable;
import lombok.RequiredArgsConstructor;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@Controller
@RequestMapping("/inventory/reports")
@RequiredArgsConstructor
public class InventoryReportController {

    private final InventoryReportService reportService;
    private final GetProductUseCase getProductUseCase;
    private final FindProductsUseCase findProductsUseCase;
    private final GetContainerLookupUseCase getContainerLookupUseCase;
    private final ProductWebMapper webMapper;

    @GetMapping("/on-hand")
    @PreAuthorize("hasAuthority('ON-HAND_READ')")
    public String onHandList(@RequestParam(value = "search", required = false) String keyword,
                            @PageableDefault(size = 10) org.springframework.data.domain.Pageable springPageable,
                            Model model) {
        model.addAttribute("page", reportService.getOnHandSummary(keyword, springPageable));
        model.addAttribute("search", keyword);
        return "inventory/reports/on-hand/list";
    }

    @GetMapping("/on-hand/{productId}")
    @PreAuthorize("hasAuthority('ON-HAND_READ')")
    public String onHandDetail(@PathVariable Long productId, Model model) {
        model.addAttribute("product", getProductUseCase.execute(productId)
            .map(webMapper::toDetailResponse)
            .orElseThrow(() -> new RuntimeException("Product not found")));
        model.addAttribute("details", reportService.getOnHandDetail(productId));
        return "inventory/reports/on-hand/detail";
    }

    @GetMapping("/stock-card")
    @PreAuthorize("hasAuthority('STOCK-CARD_READ')")
    public String stockCard(@ModelAttribute("filter") StockCardFilter filter,
                           @PageableDefault(size = 20) org.springframework.data.domain.Pageable springPageable,
                           Model model) {
        model.addAttribute("page", reportService.getStockCard(filter, springPageable));
        
        // Use lookup or unpaged find for dropdown
        var products = findProductsUseCase.execute(null, Pageable.of(0, 1000)).content().stream()
            .map(webMapper::toSummaryResponse)
            .collect(Collectors.toList());
            
        model.addAttribute("products", products);
        model.addAttribute("containers", getContainerLookupUseCase.findAll());
        return "inventory/reports/stock-card/list";
    }
}
