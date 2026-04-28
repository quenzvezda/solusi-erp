package com.solusi.erp.accounting.coa.web.controller;

import com.solusi.erp.core.annotation.DefaultRedirectUrl;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.core.infrastructure.util.PageableMapper;
import com.solusi.erp.core.dto.ApiResponse;
import com.solusi.erp.core.domain.model.DeleteResult;
import com.solusi.erp.accounting.coa.application.usecase.command.CreateCoaUseCase;
import com.solusi.erp.accounting.coa.application.usecase.command.DeleteCoaUseCase;
import com.solusi.erp.accounting.coa.application.usecase.command.UpdateCoaUseCase;
import com.solusi.erp.accounting.coa.application.usecase.query.FindCoaSelectorUseCase;
import com.solusi.erp.accounting.coa.application.usecase.query.FindCoaUseCase;
import com.solusi.erp.accounting.coa.application.usecase.query.GetCoaEditViewUseCase;
import com.solusi.erp.accounting.coa.domain.model.AccountType;
import com.solusi.erp.accounting.coa.domain.model.ChartOfAccount;
import com.solusi.erp.accounting.coa.web.dto.CoaDetailResponse;
import com.solusi.erp.accounting.coa.web.dto.CoaSaveRequest;
import com.solusi.erp.accounting.coa.web.dto.CoaSummaryResponse;
import com.solusi.erp.accounting.coa.web.mapper.CoaWebMapper;
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
@RequestMapping("/accounting/coa")
@RequiredArgsConstructor
@DefaultRedirectUrl
public class CoaController {

    private final CreateCoaUseCase createCoaUseCase;
    private final UpdateCoaUseCase updateCoaUseCase;
    private final DeleteCoaUseCase deleteCoaUseCase;
    private final FindCoaUseCase findCoaUseCase;
    private final FindCoaSelectorUseCase findCoaSelectorUseCase;
    private final GetCoaEditViewUseCase getCoaEditViewUseCase;
    private final CoaWebMapper webMapper;
    private final MessageSource messageSource;

    @GetMapping
    @PreAuthorize("hasAuthority('ACCOUNTING-COA_READ')")
    public String list(@RequestParam(required = false) String keyword,
                       org.springframework.data.domain.Pageable springPageable,
                       Model model) {
        Pageable domainPageable = PageableMapper.toDomain(springPageable);
        com.solusi.erp.core.domain.model.Page<ChartOfAccount> domainPage = findCoaUseCase.execute(keyword, domainPageable);

        List<CoaSummaryResponse> content = domainPage.content().stream()
                .map(webMapper::toSummaryResponse)
                .collect(Collectors.toList());

        Page<CoaSummaryResponse> springPage = new PageImpl<>(content, springPageable, domainPage.totalElements());
        model.addAttribute("page", springPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("accountTypes", AccountType.values());
        return "accounting/coa/list";
    }

    @GetMapping("/selectors/parent")
    @PreAuthorize("hasAuthority('ACCOUNTING-COA_READ')")
    public String showParentSelector(@RequestParam(required = false) String keyword,
                                     @RequestParam(required = false) String accountType,
                                     Model model) {
        model.addAttribute("selectorRows", findCoaSelectorUseCase.execute(keyword, accountType));
        model.addAttribute("keyword", keyword);
        model.addAttribute("accountType", accountType);
        model.addAttribute("accountTypes", AccountType.values());
        return "accounting/coa/fragments/coa-selector-modal";
    }

    @GetMapping("/create")
    @PreAuthorize("hasAuthority('ACCOUNTING-COA_CREATE')")
    public String showCreateForm(Model model) {
        CoaSaveRequest request = new CoaSaveRequest();
        request.setIsActive(true);
        request.setIsHeader(false);
        request.setLevel(1);
        model.addAttribute("coaRequest", request);
        model.addAttribute("accountTypes", AccountType.values());
        return "accounting/coa/form";
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('ACCOUNTING-COA_CREATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<CoaDetailResponse>> create(@Valid @RequestBody CoaSaveRequest request) {
        AccountType accountType;
        try {
            accountType = AccountType.valueOf(request.getAccountType());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Invalid account type: " + request.getAccountType()));
        }
        ChartOfAccount domain = createCoaUseCase.execute(
                request.getCode(), request.getName(),
                accountType,
                request.getParentId(), request.getLevel(),
                request.getIsHeader(), request.getNote(), request.getIsActive());
        CoaDetailResponse data = webMapper.toDetailResponse(domain);
        String msg = messageSource.getMessage("msg.success.create", null, LocaleContextHolder.getLocale());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(msg, data));
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('ACCOUNTING-COA_UPDATE')")
    public String showEditForm(@PathVariable Long id, Model model) {
        ChartOfAccount domain = getCoaEditViewUseCase.execute(id)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        CoaSaveRequest saveRequest = webMapper.toSaveRequest(domain);

        // Populate parent display fields if parent exists
        if (domain.getParentId() != null) {
            getCoaEditViewUseCase.execute(domain.getParentId())
                    .ifPresent(parent -> {
                        saveRequest.setParentName(parent.getName());
                        saveRequest.setParentCode(parent.getCode());
                    });
        }

        model.addAttribute("coaRequest", saveRequest);
        model.addAttribute("auditInfo", webMapper.toDetailResponse(domain));
        model.addAttribute("accountTypes", AccountType.values());
        return "accounting/coa/form";
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('ACCOUNTING-COA_UPDATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<CoaDetailResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody CoaSaveRequest request) {
        AccountType accountType;
        try {
            accountType = AccountType.valueOf(request.getAccountType());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Invalid account type: " + request.getAccountType()));
        }
        ChartOfAccount domain = updateCoaUseCase.execute(
                id, request.getName(),
                accountType,
                request.getParentId(), request.getLevel(),
                request.getIsHeader(), request.getNote(), request.getIsActive());
        CoaDetailResponse data = webMapper.toDetailResponse(domain);
        String msg = messageSource.getMessage("msg.success.update", null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok(ApiResponse.success(msg, data));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ACCOUNTING-COA_DELETE')")
    @ResponseBody
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        DeleteResult result = deleteCoaUseCase.execute(id);
        if (result == DeleteResult.SOFT_DELETED) {
            String msg = messageSource.getMessage("msg.success.deactivated", null, LocaleContextHolder.getLocale());
            return HtmxResponseUtility.okWithRefreshTableAndWarning(msg);
        }
        String msg = messageSource.getMessage("msg.success.delete", null, LocaleContextHolder.getLocale());
        return HtmxResponseUtility.okWithRefreshTableAndSuccess(msg);
    }
}
