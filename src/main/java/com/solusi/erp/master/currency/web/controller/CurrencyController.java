package com.solusi.erp.master.currency.web.controller;

import com.solusi.erp.core.annotation.DefaultRedirectUrl;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.core.infrastructure.util.PageableMapper;
import com.solusi.erp.core.dto.ApiResponse;
import com.solusi.erp.core.domain.model.DeleteResult;
import com.solusi.erp.master.currency.application.usecase.command.CreateCurrencyUseCase;
import com.solusi.erp.master.currency.application.usecase.command.DeleteCurrencyUseCase;
import com.solusi.erp.master.currency.application.usecase.command.UpdateCurrencyUseCase;
import com.solusi.erp.master.currency.application.usecase.query.FindCurrenciesUseCase;
import com.solusi.erp.master.currency.application.usecase.query.GetCurrencyEditViewUseCase;
import com.solusi.erp.master.currency.domain.model.Currency;
import com.solusi.erp.master.currency.web.dto.CurrencyDetailResponse;
import com.solusi.erp.master.currency.web.dto.CurrencySaveRequest;
import com.solusi.erp.master.currency.web.dto.CurrencySummaryResponse;
import com.solusi.erp.master.currency.web.mapper.CurrencyWebMapper;
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
 * Controller for Currency CRUD.
 */
@Controller
@RequestMapping("/master/currencies")
@RequiredArgsConstructor
@DefaultRedirectUrl
public class CurrencyController {

    private final CreateCurrencyUseCase createCurrencyUseCase;
    private final UpdateCurrencyUseCase updateCurrencyUseCase;
    private final DeleteCurrencyUseCase deleteCurrencyUseCase;
    private final FindCurrenciesUseCase findCurrenciesUseCase;
    private final GetCurrencyEditViewUseCase getCurrencyEditViewUseCase;
    private final CurrencyWebMapper webMapper;
    private final MessageSource messageSource;

    @GetMapping
    @PreAuthorize("hasAuthority('CURRENCY_READ')")
    public String list(@RequestParam(required = false) String keyword,
                       org.springframework.data.domain.Pageable springPageable,
                       Model model) {
        Pageable domainPageable = PageableMapper.toDomain(springPageable);
        com.solusi.erp.core.domain.model.Page<Currency> domainPage =
                findCurrenciesUseCase.execute(keyword, domainPageable);

        List<CurrencySummaryResponse> content = domainPage.content().stream()
                .map(webMapper::toSummaryResponse)
                .collect(Collectors.toList());

        Page<CurrencySummaryResponse> springPage = new PageImpl<>(content, springPageable, domainPage.totalElements());
        model.addAttribute("page", springPage);
        model.addAttribute("keyword", keyword);
        return "master/currency/list";
    }

    @GetMapping("/create")
    @PreAuthorize("hasAuthority('CURRENCY_CREATE')")
    public String showCreateForm(Model model) {
        CurrencySaveRequest request = new CurrencySaveRequest();
        request.setIsActive(true);
        request.setIsDefault(false);
        model.addAttribute("currencyRequest", request);
        return "master/currency/form";
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('CURRENCY_CREATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<CurrencyDetailResponse>> create(@Valid @RequestBody CurrencySaveRequest request) {
        Currency domain = createCurrencyUseCase.execute(
                request.getSymbol(), request.getAlias(), request.getName(),
                request.getNote(), request.getIsDefault(), request.getIsActive());
        CurrencyDetailResponse data = webMapper.toDetailResponse(domain);
        String msg = messageSource.getMessage("msg.success.create", null, LocaleContextHolder.getLocale());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(msg, data));
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('CURRENCY_UPDATE')")
    public String showEditForm(@PathVariable Long id, Model model) {
        Currency domain = getCurrencyEditViewUseCase.execute(id)
                .orElseThrow(() -> new RuntimeException("Currency not found"));
        model.addAttribute("currencyRequest", webMapper.toSaveRequest(domain));
        model.addAttribute("auditInfo", webMapper.toDetailResponse(domain));
        return "master/currency/form";
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('CURRENCY_UPDATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<CurrencyDetailResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody CurrencySaveRequest request) {
        Currency domain = updateCurrencyUseCase.execute(
                id, request.getSymbol(), request.getName(),
                request.getNote(), request.getIsDefault(), request.getIsActive());
        CurrencyDetailResponse data = webMapper.toDetailResponse(domain);
        String msg = messageSource.getMessage("msg.success.update", null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok(ApiResponse.success(msg, data));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('CURRENCY_DELETE')")
    @ResponseBody
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        DeleteResult result = deleteCurrencyUseCase.execute(id);
        if (result == DeleteResult.SOFT_DELETED) {
            String msg = messageSource.getMessage("msg.success.deactivated", null, LocaleContextHolder.getLocale());
            return HtmxResponseUtility.okWithRefreshTableAndWarning(msg);
        }
        String msg = messageSource.getMessage("msg.success.delete", null, LocaleContextHolder.getLocale());
        return HtmxResponseUtility.okWithRefreshTableAndSuccess(msg);
    }
}
