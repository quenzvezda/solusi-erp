package com.solusi.erp.inventory.uomconversion.web.controller;

import com.solusi.erp.core.annotation.DefaultRedirectUrl;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.core.dto.ApiResponse;
import com.solusi.erp.inventory.model.UomType;
import com.solusi.erp.inventory.uom.application.usecase.query.GetUomLookupUseCase;
import com.solusi.erp.inventory.uomconversion.application.usecase.command.*;
import com.solusi.erp.inventory.uomconversion.application.usecase.query.*;
import com.solusi.erp.inventory.uomconversion.domain.model.UomConversion;
import com.solusi.erp.inventory.uomconversion.web.dto.*;
import com.solusi.erp.inventory.uomconversion.web.mapper.UomConversionWebMapper;
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
 * Controller for UomConversion CRUD.
 * Replaces legacy com.solusi.erp.inventory.controller.ProductUomConversionController.
 */
@Controller
@RequestMapping("/inventory/uom-conversions")
@RequiredArgsConstructor
@DefaultRedirectUrl
public class UomConversionController {

    private final CreateUomConversionUseCase createUomConversionUseCase;
    private final UpdateUomConversionUseCase updateUomConversionUseCase;
    private final DeleteUomConversionUseCase deleteUomConversionUseCase;
    private final FindUomConversionsUseCase findUomConversionsUseCase;
    private final GetUomConversionEditViewUseCase getUomConversionEditViewUseCase;
    private final GetUomLookupUseCase getUomLookupUseCase;
    private final UomConversionWebMapper webMapper;
    private final MessageSource messageSource;

    @GetMapping
    @PreAuthorize("hasAuthority('UOM-CONVERSION_READ')")
    public String list(@RequestParam(required = false) String keyword,
                       org.springframework.data.domain.Pageable springPageable,
                       Model model) {
        Pageable domainPageable = Pageable.of(springPageable.getPageNumber(), springPageable.getPageSize());
        com.solusi.erp.core.domain.model.Page<UomConversion> domainPage =
            findUomConversionsUseCase.execute(keyword, domainPageable);

        List<UomConversionSummaryResponse> content = domainPage.content().stream()
            .map(webMapper::toSummaryResponse)
            .collect(Collectors.toList());

        Page<UomConversionSummaryResponse> springPage =
            new PageImpl<>(content, springPageable, domainPage.totalElements());
        model.addAttribute("page", springPage);
        model.addAttribute("keyword", keyword);
        return "inventory/uom-conversions/list";
    }

    @GetMapping("/create")
    @PreAuthorize("hasAuthority('UOM-CONVERSION_CREATE')")
    public String showCreateForm(Model model) {
        model.addAttribute("uomConversionRequest", new UomConversionSaveRequest());
        model.addAttribute("uoms", getUomLookupUseCase.findByType(UomType.UNIT));
        return "inventory/uom-conversions/form";
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('UOM-CONVERSION_CREATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<UomConversionDetailResponse>> create(
            @Valid @RequestBody UomConversionSaveRequest request) {
        UomConversion domain = createUomConversionUseCase.execute(
            request.getProductId(), request.getFromUomId(), request.getConversionFactor());
        UomConversionDetailResponse data = webMapper.toDetailResponse(domain);
        String msg = messageSource.getMessage("msg.success.create", null, LocaleContextHolder.getLocale());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(msg, data));
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('UOM-CONVERSION_UPDATE')")
    public String showEditForm(@PathVariable Long id, Model model) {
        UomConversion domain = getUomConversionEditViewUseCase.execute(id)
            .orElseThrow(() -> new RuntimeException("UomConversion not found"));

        model.addAttribute("uomConversionRequest", webMapper.toSaveRequest(domain));
        model.addAttribute("uomUIForm", new UomConversionUIInfo(
            domain.getProductName(), domain.getProductCode(), domain.getToUomName()));
        model.addAttribute("auditInfo", webMapper.toDetailResponse(domain));
        model.addAttribute("uoms", getUomLookupUseCase.findByType(UomType.UNIT));
        return "inventory/uom-conversions/form";
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('UOM-CONVERSION_UPDATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<UomConversionDetailResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UomConversionSaveRequest request) {
        UomConversion domain = updateUomConversionUseCase.execute(
            id, request.getFromUomId(), request.getConversionFactor());
        UomConversionDetailResponse data = webMapper.toDetailResponse(domain);
        String msg = messageSource.getMessage("msg.success.update", null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok(ApiResponse.success(msg, data));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('UOM-CONVERSION_DELETE')")
    @ResponseBody
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deleteUomConversionUseCase.execute(id);
        String msg = messageSource.getMessage("msg.success.delete", null, LocaleContextHolder.getLocale());
        return HtmxResponseUtility.okWithRefreshTableAndSuccess(msg);
    }
}
