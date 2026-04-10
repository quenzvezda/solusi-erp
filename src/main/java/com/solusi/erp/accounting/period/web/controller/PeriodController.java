package com.solusi.erp.accounting.period.web.controller;

import com.solusi.erp.core.annotation.DefaultRedirectUrl;
import com.solusi.erp.core.domain.model.DeleteResult;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.core.dto.ApiResponse;
import com.solusi.erp.core.infrastructure.util.PageableMapper;
import com.solusi.erp.accounting.period.application.usecase.command.*;
import com.solusi.erp.accounting.period.application.usecase.query.*;
import com.solusi.erp.accounting.period.domain.model.FiscalYear;
import com.solusi.erp.accounting.period.web.dto.*;
import com.solusi.erp.accounting.period.web.mapper.PeriodWebMapper;
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

@Controller
@RequestMapping("/accounting/periods")
@RequiredArgsConstructor
@DefaultRedirectUrl
public class PeriodController {

    private final CreateFiscalYearUseCase createFiscalYearUseCase;
    private final UpdateFiscalYearUseCase updateFiscalYearUseCase;
    private final DeleteFiscalYearUseCase deleteFiscalYearUseCase;
    private final ClosePeriodUseCase closePeriodUseCase;
    private final ReopenPeriodUseCase reopenPeriodUseCase;
    private final FindFiscalYearsUseCase findFiscalYearsUseCase;
    private final GetFiscalYearDetailUseCase getFiscalYearDetailUseCase;
    private final PeriodWebMapper webMapper;
    private final MessageSource messageSource;

    @GetMapping
    @PreAuthorize("hasAuthority('ACCOUNTING-PERIOD_READ')")
    public String list(@RequestParam(required = false) String keyword,
                       org.springframework.data.domain.Pageable springPageable,
                       Model model) {
        Pageable domainPageable = PageableMapper.toDomain(springPageable);
        com.solusi.erp.core.domain.model.Page<FiscalYear> domainPage = findFiscalYearsUseCase.execute(keyword, domainPageable);

        List<FiscalYearSummaryResponse> content = domainPage.content().stream()
                .map(webMapper::toSummaryResponse)
                .collect(Collectors.toList());

        Page<FiscalYearSummaryResponse> springPage = new PageImpl<>(content, springPageable, domainPage.totalElements());
        model.addAttribute("page", springPage);
        model.addAttribute("keyword", keyword);
        return "accounting/period/list";
    }

    @GetMapping("/create")
    @PreAuthorize("hasAuthority('ACCOUNTING-PERIOD_CREATE')")
    public String showCreateForm(Model model) {
        FiscalYearSaveRequest request = new FiscalYearSaveRequest();
        request.setIsActive(true);
        model.addAttribute("fyRequest", request);
        return "accounting/period/form";
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('ACCOUNTING-PERIOD_CREATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<FiscalYearDetailResponse>> create(
            @Valid @RequestBody FiscalYearSaveRequest request) {
        FiscalYear domain = createFiscalYearUseCase.execute(
                request.getName(), request.getStartDate(), request.getEndDate(),
                request.getIsActive());
        FiscalYearDetailResponse data = webMapper.toDetailResponse(domain);
        String msg = messageSource.getMessage("msg.success.create", null, LocaleContextHolder.getLocale());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(msg, data));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ACCOUNTING-PERIOD_READ')")
    public String detail(@PathVariable Long id, Model model) {
        FiscalYear domain = getFiscalYearDetailUseCase.execute(id)
                .orElseThrow(() -> new RuntimeException("Fiscal Year not found"));
        model.addAttribute("fy", webMapper.toDetailResponse(domain));
        return "accounting/period/detail";
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('ACCOUNTING-PERIOD_UPDATE')")
    public String showEditForm(@PathVariable Long id, Model model) {
        FiscalYear domain = getFiscalYearDetailUseCase.execute(id)
                .orElseThrow(() -> new RuntimeException("Fiscal Year not found"));
        model.addAttribute("fyRequest", webMapper.toSaveRequest(domain));
        model.addAttribute("auditInfo", webMapper.toDetailResponse(domain));
        return "accounting/period/form";
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('ACCOUNTING-PERIOD_UPDATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<FiscalYearDetailResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody FiscalYearSaveRequest request) {
        FiscalYear domain = updateFiscalYearUseCase.execute(id, request.getName(), request.getIsActive());
        FiscalYearDetailResponse data = webMapper.toDetailResponse(domain);
        String msg = messageSource.getMessage("msg.success.update", null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok(ApiResponse.success(msg, data));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ACCOUNTING-PERIOD_DELETE')")
    @ResponseBody
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        DeleteResult result = deleteFiscalYearUseCase.execute(id);
        if (result == DeleteResult.SOFT_DELETED) {
            String msg = messageSource.getMessage("msg.success.deactivated", null, LocaleContextHolder.getLocale());
            return HtmxResponseUtility.okWithRefreshTableAndWarning(msg);
        }
        String msg = messageSource.getMessage("msg.success.delete", null, LocaleContextHolder.getLocale());
        return HtmxResponseUtility.okWithRefreshTableAndSuccess(msg);
    }

    @PostMapping("/periods/{periodId}/close")
    @PreAuthorize("hasAuthority('ACCOUNTING-PERIOD_UPDATE')")
    @ResponseBody
    public ResponseEntity<Void> closePeriod(@PathVariable Long periodId) {
        closePeriodUseCase.execute(periodId);
        String msg = messageSource.getMessage("msg.success.update", null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok()
                .header("HX-Refresh", "true")
                .build();
    }

    @PostMapping("/periods/{periodId}/reopen")
    @PreAuthorize("hasAuthority('ACCOUNTING-PERIOD_UPDATE')")
    @ResponseBody
    public ResponseEntity<Void> reopenPeriod(@PathVariable Long periodId) {
        reopenPeriodUseCase.execute(periodId);
        String msg = messageSource.getMessage("msg.success.update", null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok()
                .header("HX-Refresh", "true")
                .build();
    }
}
