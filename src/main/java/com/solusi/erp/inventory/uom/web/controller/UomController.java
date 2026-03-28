package com.solusi.erp.inventory.uom.web.controller;

import com.solusi.erp.core.annotation.DefaultRedirectUrl;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.core.util.PageableMapper;
import com.solusi.erp.core.dto.ApiResponse;
import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.inventory.model.UomType;
import com.solusi.erp.inventory.uom.application.usecase.command.*;
import com.solusi.erp.inventory.uom.application.usecase.query.*;
import com.solusi.erp.inventory.uom.domain.model.UnitOfMeasure;
import com.solusi.erp.inventory.uom.web.dto.*;
import com.solusi.erp.inventory.uom.web.mapper.UomWebMapper;
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
 * Controller for UnitOfMeasure CRUD.
 * Replaces legacy com.solusi.erp.inventory.controller.UnitOfMeasureController.
 */
@Controller
@RequestMapping("/inventory/unit-of-measures")
@RequiredArgsConstructor
@DefaultRedirectUrl
public class UomController {

    private final CreateUomUseCase createUomUseCase;
    private final UpdateUomUseCase updateUomUseCase;
    private final DeleteUomUseCase deleteUomUseCase;
    private final FindUomsUseCase findUomsUseCase;
    private final GetUomEditViewUseCase getUomEditViewUseCase;
    private final GetUomLookupUseCase getUomLookupUseCase;
    private final UomWebMapper webMapper;
    private final MessageSource messageSource;

    @GetMapping
    @PreAuthorize("hasAuthority('UNIT-OF-MEASURE_READ')")
    public String list(@RequestParam(required = false) String keyword,
                       org.springframework.data.domain.Pageable springPageable,
                       Model model) {
        Pageable domainPageable = PageableMapper.toDomain(springPageable);
        com.solusi.erp.core.domain.model.Page<UnitOfMeasure> domainPage =
            findUomsUseCase.execute(keyword, domainPageable);

        List<UomSummaryResponse> content = domainPage.content().stream()
            .map(webMapper::toSummaryResponse)
            .collect(Collectors.toList());

        Page<UomSummaryResponse> springPage = new PageImpl<>(content, springPageable, domainPage.totalElements());
        model.addAttribute("page", springPage);
        model.addAttribute("keyword", keyword);
        return "inventory/unit-of-measures/list";
    }

    @GetMapping("/create")
    @PreAuthorize("hasAuthority('UNIT-OF-MEASURE_CREATE')")
    public String showCreateForm(Model model) {
        model.addAttribute("unitOfMeasureRequest", new UomSaveRequest());
        model.addAttribute("types", UomType.values());
        return "inventory/unit-of-measures/form";
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('UNIT-OF-MEASURE_CREATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<UomDetailResponse>> create(@Valid @RequestBody UomSaveRequest request) {
        UnitOfMeasure domain = createUomUseCase.execute(request.getCode(), request.getName(), request.getType());
        UomDetailResponse data = webMapper.toDetailResponse(domain);
        String msg = messageSource.getMessage("msg.success.create", null, LocaleContextHolder.getLocale());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(msg, data));
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('UNIT-OF-MEASURE_UPDATE')")
    public String showEditForm(@PathVariable Long id, Model model) {
        UnitOfMeasure domain = getUomEditViewUseCase.execute(id)
            .orElseThrow(() -> new RuntimeException("UnitOfMeasure not found"));
        model.addAttribute("unitOfMeasureRequest", webMapper.toSaveRequest(domain));
        model.addAttribute("auditInfo", webMapper.toDetailResponse(domain));
        model.addAttribute("types", UomType.values());
        return "inventory/unit-of-measures/form";
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('UNIT-OF-MEASURE_UPDATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<UomDetailResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UomSaveRequest request) {
        UnitOfMeasure domain = updateUomUseCase.execute(id, request.getName(), request.getType());
        UomDetailResponse data = webMapper.toDetailResponse(domain);
        String msg = messageSource.getMessage("msg.success.update", null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok(ApiResponse.success(msg, data));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('UNIT-OF-MEASURE_DELETE')")
    @ResponseBody
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deleteUomUseCase.execute(id);
        String msg = messageSource.getMessage("msg.success.delete", null, LocaleContextHolder.getLocale());
        return HtmxResponseUtility.okWithRefreshTableAndSuccess(msg);
    }

    @GetMapping("/lookup")
    @PreAuthorize("hasAuthority('LOOKUP_UNIT-OF-MEASURE')")
    @ResponseBody
    public List<LookupDto> lookup(@RequestParam(required = false) String keyword,
                                  @RequestParam(defaultValue = "10") int limit) {
        return getUomLookupUseCase.search(keyword, limit);
    }

    @GetMapping("/lookup/{id}")
    @PreAuthorize("hasAuthority('LOOKUP_UNIT-OF-MEASURE')")
    @ResponseBody
    public LookupDto lookupById(@PathVariable Long id) {
        return getUomLookupUseCase.getById(id);
    }

    @GetMapping("/lookup/by-type")
    @PreAuthorize("hasAuthority('LOOKUP_UNIT-OF-MEASURE')")
    @ResponseBody
    public List<LookupDto> lookupByType(@RequestParam UomType type) {
        return getUomLookupUseCase.findByType(type);
    }
}
