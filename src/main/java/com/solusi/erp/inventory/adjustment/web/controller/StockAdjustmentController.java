package com.solusi.erp.inventory.adjustment.web.controller;

import com.solusi.erp.core.annotation.DefaultRedirectUrl;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.core.infrastructure.util.PageableMapper;
import com.solusi.erp.core.dto.ApiResponse;
import com.solusi.erp.inventory.adjustment.application.usecase.command.*;
import com.solusi.erp.inventory.adjustment.application.usecase.query.*;
import com.solusi.erp.inventory.adjustment.domain.model.AdjustmentStatus;
import com.solusi.erp.inventory.adjustment.domain.model.StockAdjustment;
import com.solusi.erp.inventory.adjustment.web.dto.*;
import com.solusi.erp.inventory.adjustment.web.mapper.StockAdjustmentWebMapper;
import com.solusi.erp.master.currency.application.usecase.query.FindActiveCurrenciesUseCase;
import com.solusi.erp.master.currency.application.usecase.query.GetDefaultCurrencyUseCase;
import com.solusi.erp.master.currency.domain.model.Currency;
import com.solusi.erp.master.currency.web.dto.CurrencySummaryResponse;
import com.solusi.erp.master.currency.web.mapper.CurrencyWebMapper;
import com.solusi.erp.util.HtmxResponseUtility;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
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
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for StockAdjustment CRUD.
 * Replaces legacy com.solusi.erp.inventory.controller.StockAdjustmentController.
 */
@Slf4j
@Controller
@RequestMapping("/inventory/adjustments")
@RequiredArgsConstructor
@DefaultRedirectUrl
public class StockAdjustmentController {

    private final CreateStockAdjustmentUseCase createUseCase;
    private final UpdateStockAdjustmentUseCase updateUseCase;
    private final DeleteStockAdjustmentUseCase deleteUseCase;
    private final ProcessStockAdjustmentUseCase processUseCase;
    private final FindStockAdjustmentsUseCase findUseCase;
    private final GetStockAdjustmentUseCase getUseCase;
    private final GetStockAdjustmentEditViewUseCase getEditViewUseCase;
    private final StockAdjustmentWebMapper webMapper;
    private final FindActiveCurrenciesUseCase findActiveCurrenciesUseCase;
    private final GetDefaultCurrencyUseCase getDefaultCurrencyUseCase;
    private final CurrencyWebMapper currencyWebMapper;
    private final MessageSource messageSource;

    @GetMapping
    @PreAuthorize("hasAuthority('STOCK-ADJUSTMENT_READ')")
    public String list(@RequestParam(value = "search", required = false) String keyword,
                       @PageableDefault(size = 10) org.springframework.data.domain.Pageable springPageable,
                       Model model) {
        Pageable domainPageable = PageableMapper.toDomain(springPageable);
        com.solusi.erp.core.domain.model.Page<StockAdjustment> domainPage = findUseCase.execute(keyword, domainPageable);

        List<StockAdjustmentSummaryResponse> content = domainPage.content().stream()
                .map(webMapper::toSummaryResponse)
                .collect(Collectors.toList());

        org.springframework.data.domain.Page<StockAdjustmentSummaryResponse> page =
                new PageImpl<>(content, springPageable, domainPage.totalElements());

        model.addAttribute("page", page);
        model.addAttribute("search", keyword);
        return "inventory/adjustments/list";
    }

    @GetMapping("/create")
    @PreAuthorize("hasAuthority('STOCK-ADJUSTMENT_CREATE')")
    public String createForm(Model model) {
        Currency defaultCurr = getDefaultCurrencyUseCase.execute().orElse(null);
        StockAdjustmentSaveRequest request = new StockAdjustmentSaveRequest();
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
    public ResponseEntity<ApiResponse<StockAdjustmentDetailResponse>> create(
            @Valid @RequestBody StockAdjustmentSaveRequest request) {
        List<LineCommand> lines = webMapper.toLineCommands(request.getLines());
        StockAdjustment domain = createUseCase.execute(
                request.getTransactionDate(), request.getNote(), request.getFacilityId(),
                request.getCurrencyId(), request.getExchangeRate(), lines);
        StockAdjustmentDetailResponse data = webMapper.toDetailResponse(domain);
        String msg = messageSource.getMessage("msg.success.create", null, LocaleContextHolder.getLocale());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(msg, data));
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('STOCK-ADJUSTMENT_UPDATE')")
    public String editForm(@PathVariable Long id, Model model) {
        StockAdjustment domain = getEditViewUseCase.execute(id)
                .orElseThrow(() -> new RuntimeException("StockAdjustment not found: " + id));
        if (domain.getStatus() == AdjustmentStatus.COMPLETED) {
            return "redirect:/inventory/adjustments/view/" + id;
        }
        model.addAttribute("stockAdjustment", webMapper.toSaveRequest(domain));
        populateFormModels(model);
        return "inventory/adjustments/form";
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('STOCK-ADJUSTMENT_UPDATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<StockAdjustmentDetailResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody StockAdjustmentSaveRequest request) {
        List<LineCommand> lines = webMapper.toLineCommands(request.getLines());
        StockAdjustment domain = updateUseCase.execute(
                id, request.getTransactionDate(), request.getNote(), request.getFacilityId(),
                request.getCurrencyId(), request.getExchangeRate(), lines);
        StockAdjustmentDetailResponse data = webMapper.toDetailResponse(domain);
        String msg = messageSource.getMessage("msg.success.update", null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok(ApiResponse.success(msg, data));
    }

    @GetMapping("/view/{id}")
    @PreAuthorize("hasAuthority('STOCK-ADJUSTMENT_READ')")
    public String view(@PathVariable Long id, Model model) {
        StockAdjustment domain = getUseCase.execute(id)
                .orElseThrow(() -> new RuntimeException("StockAdjustment not found: " + id));
        model.addAttribute("stockAdjustment", webMapper.toDetailResponse(domain));
        return "inventory/adjustments/view";
    }

    @PostMapping("/{id}/process")
    @PreAuthorize("hasAuthority('STOCK-ADJUSTMENT_PROCESS')")
    public String process(@PathVariable Long id, RedirectAttributes ra) {
        processUseCase.execute(id);
        String msg = messageSource.getMessage("msg.success.ajax.generic", null, LocaleContextHolder.getLocale());
        ra.addFlashAttribute("message", msg);
        return "redirect:/inventory/adjustments/view/" + id;
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('STOCK-ADJUSTMENT_DELETE')")
    @ResponseBody
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deleteUseCase.execute(id);
        String msg = messageSource.getMessage("msg.success.delete", null, LocaleContextHolder.getLocale());
        return HtmxResponseUtility.okWithRefreshTableAndSuccess(msg);
    }

    private void populateFormModels(Model model) {
        List<CurrencySummaryResponse> currencies = findActiveCurrenciesUseCase.execute().stream()
                .map(currencyWebMapper::toSummaryResponse)
                .collect(Collectors.toList());
        CurrencySummaryResponse defaultCurrency = getDefaultCurrencyUseCase.execute()
                .map(currencyWebMapper::toSummaryResponse)
                .orElse(null);
        model.addAttribute("currencies", currencies);
        model.addAttribute("defaultCurrency", defaultCurrency);
    }
}
