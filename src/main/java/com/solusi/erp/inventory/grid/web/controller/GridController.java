package com.solusi.erp.inventory.grid.web.controller;

import com.solusi.erp.core.annotation.DefaultRedirectUrl;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.core.infrastructure.util.PageableMapper;
import com.solusi.erp.core.dto.ApiResponse;
import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.inventory.facility.application.usecase.query.FindFacilitiesUseCase;
import com.solusi.erp.inventory.facility.application.usecase.query.GetFacilityEditViewUseCase;
import com.solusi.erp.inventory.facility.web.dto.FacilitySummaryResponse;
import com.solusi.erp.inventory.facility.web.mapper.FacilityWebMapper;
import com.solusi.erp.inventory.grid.application.usecase.command.*;
import com.solusi.erp.inventory.grid.application.usecase.query.*;
import com.solusi.erp.inventory.grid.domain.model.Grid;
import com.solusi.erp.inventory.grid.web.dto.*;
import com.solusi.erp.inventory.grid.web.mapper.GridWebMapper;
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
@RequestMapping("/inventory/grids")
@RequiredArgsConstructor
@DefaultRedirectUrl
public class GridController {

    private final CreateGridUseCase createGridUseCase;
    private final UpdateGridUseCase updateGridUseCase;
    private final DeleteGridUseCase deleteGridUseCase;
    private final FindGridsUseCase findGridsUseCase;
    private final GetGridEditViewUseCase getGridEditViewUseCase;
    private final GetGridLookupUseCase getGridLookupUseCase;
    private final GridWebMapper webMapper;
    private final FindFacilitiesUseCase findFacilitiesUseCase;
    private final GetFacilityEditViewUseCase getFacilityEditViewUseCase;
    private final FacilityWebMapper facilityWebMapper;
    private final MessageSource messageSource;

    @GetMapping
    @PreAuthorize("hasAuthority('GRID_READ')")
    public String list(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) Long facilityId,
                       org.springframework.data.domain.Pageable springPageable,
                       Model model) {
        Pageable domainPageable = PageableMapper.toDomain(springPageable);
        com.solusi.erp.core.domain.model.Page<Grid> domainPage =
            findGridsUseCase.execute(keyword, facilityId, domainPageable);

        List<GridSummaryResponse> content = domainPage.content().stream()
            .map(webMapper::toSummaryResponse)
            .collect(Collectors.toList());

        Page<GridSummaryResponse> springPage = new PageImpl<>(content, springPageable, domainPage.totalElements());
        model.addAttribute("page", springPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("facilityId", facilityId);

        if (facilityId != null) {
            getFacilityEditViewUseCase.execute(facilityId)
                .map(facilityWebMapper::toSummaryResponse)
                .ifPresent(f -> model.addAttribute("selectedFacility", f));
        }

        return "inventory/grids/list";
    }

    @GetMapping("/create")
    @PreAuthorize("hasAuthority('GRID_CREATE')")
    public String showCreateForm(@RequestParam(required = false) Long facilityId, Model model) {
        GridSaveRequest request = new GridSaveRequest();
        if (facilityId != null) {
            request.setFacilityId(facilityId);
        }
        model.addAttribute("gridRequest", request);
        populateFacilities(model);
        return "inventory/grids/form";
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('GRID_CREATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<GridDetailResponse>> create(@Valid @RequestBody GridSaveRequest request) {
        Grid domain = createGridUseCase.execute(
            request.getFacilityId(), request.getCode(), request.getName(), request.getNote(), request.getIsActive());
        GridDetailResponse data = webMapper.toDetailResponse(domain);
        String msg = messageSource.getMessage("msg.success.create", null, LocaleContextHolder.getLocale());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(msg, data));
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('GRID_UPDATE')")
    public String showEditForm(@PathVariable Long id, Model model) {
        Grid domain = getGridEditViewUseCase.execute(id)
            .orElseThrow(() -> new RuntimeException("Grid not found"));
        model.addAttribute("gridRequest", webMapper.toSaveRequest(domain));
        model.addAttribute("auditInfo", webMapper.toDetailResponse(domain));
        populateFacilities(model);
        return "inventory/grids/form";
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('GRID_UPDATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<GridDetailResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody GridSaveRequest request) {
        Grid domain = updateGridUseCase.execute(id, request.getName(), request.getNote(), request.getIsActive());
        GridDetailResponse data = webMapper.toDetailResponse(domain);
        String msg = messageSource.getMessage("msg.success.update", null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok(ApiResponse.success(msg, data));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('GRID_DELETE')")
    @ResponseBody
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deleteGridUseCase.execute(id);
        String msg = messageSource.getMessage("msg.success.delete", null, LocaleContextHolder.getLocale());
        return HtmxResponseUtility.okWithRefreshTableAndSuccess(msg);
    }

    @GetMapping("/lookup")
    @PreAuthorize("hasAuthority('LOOKUP_GRID')")
    @ResponseBody
    public List<LookupDto> lookup(@RequestParam(required = false) String keyword,
                                  @RequestParam(defaultValue = "10") int limit) {
        return getGridLookupUseCase.search(keyword, limit);
    }

    @GetMapping("/lookup/{id}")
    @PreAuthorize("hasAuthority('LOOKUP_GRID')")
    @ResponseBody
    public LookupDto lookupById(@PathVariable Long id) {
        return getGridLookupUseCase.getById(id);
    }

    private void populateFacilities(Model model) {
        Pageable pageable = Pageable.of(0, 1000);
        com.solusi.erp.core.domain.model.Page<com.solusi.erp.inventory.facility.domain.model.Facility> facilityPage =
            findFacilitiesUseCase.execute(null, pageable);
        List<FacilitySummaryResponse> facilities = facilityPage.content().stream()
            .map(facilityWebMapper::toSummaryResponse)
            .collect(Collectors.toList());
        model.addAttribute("facilities", facilities);
    }
}
