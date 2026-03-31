package com.solusi.erp.inventory.facility.web.controller;

import com.solusi.erp.core.annotation.DefaultRedirectUrl;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.core.infrastructure.util.PageableMapper;
import com.solusi.erp.core.dto.ApiResponse;
import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.inventory.facility.application.usecase.command.*;
import com.solusi.erp.inventory.facility.application.usecase.query.*;
import com.solusi.erp.inventory.facility.domain.model.Facility;
import com.solusi.erp.inventory.facility.web.dto.*;
import com.solusi.erp.inventory.facility.web.mapper.FacilityWebMapper;
import com.solusi.erp.master.geographic.infrastructure.persistence.Geographic;
import com.solusi.erp.master.geographic.infrastructure.persistence.GeographicJpaRepository;
import com.solusi.erp.master.party.domain.port.PartyLookupProvider;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/inventory/facilities")
@RequiredArgsConstructor
@DefaultRedirectUrl
public class FacilityController {

    private final CreateFacilityUseCase createFacilityUseCase;
    private final UpdateFacilityUseCase updateFacilityUseCase;
    private final DeleteFacilityUseCase deleteFacilityUseCase;
    private final FindFacilitiesUseCase findFacilitiesUseCase;
    private final GetFacilityEditViewUseCase getFacilityEditViewUseCase;
    private final GetFacilityLookupUseCase getFacilityLookupUseCase;
    private final FacilityWebMapper webMapper;
    private final MessageSource messageSource;
    private final PartyLookupProvider partyLookupProvider;
    private final GeographicJpaRepository geographicJpaRepository;

    @GetMapping
    @PreAuthorize("hasAuthority('FACILITY_READ')")
    public String list(@RequestParam(required = false) String keyword,
                       org.springframework.data.domain.Pageable springPageable,
                       Model model) {
        Pageable domainPageable = PageableMapper.toDomain(springPageable);
        com.solusi.erp.core.domain.model.Page<Facility> domainPage =
            findFacilitiesUseCase.execute(keyword, domainPageable);

        List<FacilitySummaryResponse> content = domainPage.content().stream()
            .map(webMapper::toSummaryResponse)
            .collect(Collectors.toList());

        Page<FacilitySummaryResponse> springPage = new PageImpl<>(content, springPageable, domainPage.totalElements());
        model.addAttribute("page", springPage);
        model.addAttribute("keyword", keyword);
        return "inventory/facilities/list";
    }

    @GetMapping("/create")
    @PreAuthorize("hasAuthority('FACILITY_CREATE')")
    public String showCreateForm(Model model) {
        model.addAttribute("facilityRequest", new FacilitySaveRequest());
        return "inventory/facilities/form";
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('FACILITY_CREATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<FacilityDetailResponse>> create(@Valid @RequestBody FacilitySaveRequest request) {
        Facility domain = createFacilityUseCase.execute(
            request.getName(), request.getOwnerId(), request.getAddressLine1(),
            request.getCityId(), request.getPostalCode(), request.getNote(), request.getIsActive());
        FacilityDetailResponse data = webMapper.toDetailResponse(domain);
        String msg = messageSource.getMessage("msg.success.create", null, LocaleContextHolder.getLocale());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(msg, data));
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('FACILITY_UPDATE')")
    public String showEditForm(@PathVariable Long id, Model model) {
        Facility domain = getFacilityEditViewUseCase.execute(id)
            .orElseThrow(() -> new RuntimeException("Facility not found"));
        model.addAttribute("facilityRequest", webMapper.toSaveRequest(domain));
        model.addAttribute("facilityUI", buildFacilityUI(domain));
        model.addAttribute("auditInfo", webMapper.toDetailResponse(domain));
        return "inventory/facilities/form";
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('FACILITY_UPDATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<FacilityDetailResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody FacilitySaveRequest request) {
        Facility domain = updateFacilityUseCase.execute(
            id, request.getName(), request.getOwnerId(), request.getAddressLine1(),
            request.getCityId(), request.getPostalCode(), request.getNote(), request.getIsActive());
        FacilityDetailResponse data = webMapper.toDetailResponse(domain);
        String msg = messageSource.getMessage("msg.success.update", null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok(ApiResponse.success(msg, data));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('FACILITY_DELETE')")
    @ResponseBody
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deleteFacilityUseCase.execute(id);
        String msg = messageSource.getMessage("msg.success.delete", null, LocaleContextHolder.getLocale());
        return HtmxResponseUtility.okWithRefreshTableAndSuccess(msg);
    }

    @GetMapping("/lookup")
    @PreAuthorize("hasAuthority('LOOKUP_FACILITY')")
    @ResponseBody
    public List<LookupDto> lookup(@RequestParam(required = false) String keyword,
                                  @RequestParam(defaultValue = "10") int limit) {
        return getFacilityLookupUseCase.search(keyword, limit);
    }

    @GetMapping("/lookup/{id}")
    @PreAuthorize("hasAuthority('LOOKUP_FACILITY')")
    @ResponseBody
    public LookupDto lookupById(@PathVariable Long id) {
        return getFacilityLookupUseCase.getById(id);
    }

    private Map<String, Object> buildFacilityUI(Facility domain) {
        Map<String, Object> ui = new HashMap<>();
        // Owner: uses PartyLookupProvider port (new standard — single source of truth)
        LookupDto ownerLookup = partyLookupProvider.resolve(domain.getOwnerId());
        ui.put("ownerName", ownerLookup != null ? ownerLookup.name() : domain.getOwnerName());
        ui.put("ownerCode", ownerLookup != null ? ownerLookup.subText() : "");
        // City: uses GeographicJpaRepository directly (old approach — kept for comparison)
        ui.put("cityName", domain.getCityName());
        ui.put("cityCode", buildCitySubText(domain.getCityId()));
        return ui;
    }

    private String buildCitySubText(Long cityId) {
        if (cityId == null) return "";
        return geographicJpaRepository.findById(cityId)
                .map(Geographic::getCode)
                .orElse("");
    }
}
