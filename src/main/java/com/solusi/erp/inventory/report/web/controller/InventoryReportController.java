package com.solusi.erp.inventory.report.web.controller;

import com.solusi.erp.inventory.report.web.dto.StockCardFilter;
import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.inventory.container.domain.port.ContainerLookupProvider;
import com.solusi.erp.inventory.product.application.usecase.query.GetProductUseCase;
import com.solusi.erp.inventory.product.domain.port.ProductLookupProvider;
import com.solusi.erp.inventory.product.web.mapper.ProductWebMapper;
import com.solusi.erp.inventory.report.application.usecase.query.GetOnHandDetailUseCase;
import com.solusi.erp.inventory.report.application.usecase.query.GetOnHandSummaryUseCase;
import com.solusi.erp.inventory.report.application.usecase.query.GetStockCardUseCase;
import com.solusi.erp.inventory.stock.domain.model.MovementType;
import com.solusi.erp.inventory.stock.domain.model.ReferenceType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/inventory/reports")
@RequiredArgsConstructor
public class InventoryReportController {

    private final GetOnHandSummaryUseCase getOnHandSummaryUseCase;
    private final GetOnHandDetailUseCase getOnHandDetailUseCase;
    private final GetStockCardUseCase getStockCardUseCase;
    private final GetProductUseCase getProductUseCase;
    private final ProductWebMapper webMapper;
    private final ProductLookupProvider productLookupProvider;
    private final ContainerLookupProvider containerLookupProvider;

    @GetMapping("/on-hand")
    @PreAuthorize("hasAuthority('ON-HAND_READ')")
    public String onHandList(@RequestParam(value = "search", required = false) String keyword,
                            @PageableDefault(size = 10) org.springframework.data.domain.Pageable springPageable,
                            Model model) {
        model.addAttribute("page", getOnHandSummaryUseCase.execute(keyword, springPageable));
        model.addAttribute("search", keyword);
        return "inventory/reports/on-hand/list";
    }

    @GetMapping("/on-hand/{productId}")
    @PreAuthorize("hasAuthority('ON-HAND_READ')")
    public String onHandDetail(@PathVariable Long productId, Model model) {
        model.addAttribute("product", getProductUseCase.execute(productId)
            .map(webMapper::toDetailResponse)
            .orElseThrow(() -> new RuntimeException("Product not found")));
        model.addAttribute("details", getOnHandDetailUseCase.execute(productId));
        return "inventory/reports/on-hand/detail";
    }

    @GetMapping("/stock-card")
    @PreAuthorize("hasAuthority('STOCK-CARD_READ')")
    public String stockCard(@ModelAttribute("filter") StockCardFilter filter,
                           @PageableDefault(size = 20) org.springframework.data.domain.Pageable springPageable,
                           Model model) {
        model.addAttribute("page", getStockCardUseCase.execute(filter, springPageable));
        model.addAttribute("filterUI", buildFilterUI(filter));
        model.addAttribute("movementTypes", MovementType.values());
        model.addAttribute("referenceTypes", ReferenceType.values());
        return "inventory/reports/stock-card/list";
    }

    private Map<String, Object> buildFilterUI(StockCardFilter filter) {
        Map<String, Object> ui = new HashMap<>();
        if (filter.getProductId() != null) {
            LookupDto product = productLookupProvider.resolve(filter.getProductId());
            if (product != null) {
                ui.put("productText", product.name());
                ui.put("productSubtext", product.subText());
            }
        }
        if (filter.getContainerId() != null) {
            LookupDto container = containerLookupProvider.resolve(filter.getContainerId());
            if (container != null) {
                ui.put("containerText", container.name());
                ui.put("containerSubtext", container.subText());
            }
        }
        return ui;
    }
}
