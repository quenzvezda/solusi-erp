package com.solusi.erp.master.geographic.web.controller;

import com.solusi.erp.core.annotation.DefaultRedirectUrl;
import com.solusi.erp.core.domain.model.DeleteResult;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.core.infrastructure.util.PageableMapper;
import com.solusi.erp.core.dto.ApiResponse;
import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.master.geographic.application.usecase.command.*;
import com.solusi.erp.master.geographic.application.usecase.query.*;
import com.solusi.erp.master.geographic.domain.model.Geographic;
import com.solusi.erp.master.geographic.domain.port.GeographicLookupProvider;
import com.solusi.erp.master.geographic.web.dto.*;
import com.solusi.erp.master.geographic.web.mapper.GeographicWebMapper;
import com.solusi.erp.master.shared.model.GeographicType;
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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for Geographic CRUD (Clean DDD / CQRS).
 */
@Controller
@RequestMapping("/master/geographics")
@RequiredArgsConstructor
@DefaultRedirectUrl
public class GeographicController {

    private final CreateGeographicUseCase createGeographicUseCase;
    private final UpdateGeographicUseCase updateGeographicUseCase;
    private final DeleteGeographicUseCase deleteGeographicUseCase;
    private final FindGeographicsUseCase findGeographicsUseCase;
    private final GetGeographicEditViewUseCase getGeographicEditViewUseCase;
    private final GeographicWebMapper webMapper;
    private final MessageSource messageSource;
    private final GeographicLookupProvider geographicLookupProvider;

    @GetMapping
    @PreAuthorize("hasAuthority('GEOGRAPHIC_READ')")
    public String list(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) Long parentId,
                       org.springframework.data.domain.Pageable springPageable,
                       Model model) {
        Pageable domainPageable = PageableMapper.toDomain(springPageable);
        com.solusi.erp.core.domain.model.Page<Geographic> domainPage =
                findGeographicsUseCase.execute(keyword, parentId, domainPageable);

        List<GeographicSummaryResponse> content = domainPage.content().stream()
                .map(webMapper::toSummaryResponse)
                .collect(Collectors.toList());

        Page<GeographicSummaryResponse> springPage = new PageImpl<>(content, springPageable, domainPage.totalElements());
        model.addAttribute("page", springPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("parentId", parentId);

        if (parentId != null) {
            getGeographicEditViewUseCase.execute(parentId)
                    .map(webMapper::toSummaryResponse)
                    .ifPresent(p -> model.addAttribute("currentParent", p));
        }

        return "master/geographic/list";
    }

    @GetMapping("/create")
    @PreAuthorize("hasAuthority('GEOGRAPHIC_CREATE')")
    public String showCreateForm(@RequestParam(required = false) Long parentId, Model model) {
        GeographicSaveRequest request = new GeographicSaveRequest();
        request.setIsActive(Boolean.TRUE);

        if (parentId != null) {
            getGeographicEditViewUseCase.execute(parentId).ifPresent(parent -> {
                request.setParentId(parentId);
                request.setParentName(parent.getName());
                LookupDto parentLookup = geographicLookupProvider.resolve(parentId);
                if (parentLookup != null) {
                    request.setParentSubtext(parentLookup.subText());
                }
                if (parent.getType() == GeographicType.COUNTRY) {
                    request.setType(GeographicType.STATE_PROVINCE);
                } else if (parent.getType() == GeographicType.STATE_PROVINCE) {
                    request.setType(GeographicType.CITY_MUNICIPALITY);
                }
            });
        }

        model.addAttribute("geographicRequest", request);
        model.addAttribute("types", GeographicType.values());
        return "master/geographic/form";
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('GEOGRAPHIC_CREATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<GeographicDetailResponse>> create(
            @Valid @RequestBody GeographicSaveRequest request) {
        Geographic domain = createGeographicUseCase.execute(
                request.getCode(), request.getName(), request.getType(),
                request.getParentId(), request.getParentName(), request.getIsActive());
        GeographicDetailResponse data = webMapper.toDetailResponse(domain);
        String msg = messageSource.getMessage("msg.success.create", null, LocaleContextHolder.getLocale());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(msg, data));
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('GEOGRAPHIC_UPDATE')")
    public String showEditForm(@PathVariable Long id, Model model) {
        Geographic domain = getGeographicEditViewUseCase.execute(id)
                .orElseThrow(() -> new RuntimeException("Geographic not found"));
        GeographicSaveRequest request = webMapper.toSaveRequest(domain);
        if (request.getParentId() != null) {
            LookupDto parentLookup = geographicLookupProvider.resolve(request.getParentId());
            if (parentLookup != null) {
                request.setParentSubtext(parentLookup.subText());
            }
        }
        model.addAttribute("geographicRequest", request);
        model.addAttribute("auditInfo", webMapper.toDetailResponse(domain));
        model.addAttribute("types", GeographicType.values());
        return "master/geographic/form";
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('GEOGRAPHIC_UPDATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<GeographicDetailResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody GeographicSaveRequest request) {
        Geographic domain = updateGeographicUseCase.execute(
                id, request.getName(), request.getType(),
                request.getParentId(), request.getParentName(), request.getIsActive());
        GeographicDetailResponse data = webMapper.toDetailResponse(domain);
        String msg = messageSource.getMessage("msg.success.update", null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok(ApiResponse.success(msg, data));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('GEOGRAPHIC_DELETE')")
    @ResponseBody
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        DeleteResult result = deleteGeographicUseCase.execute(id);
        if (result == DeleteResult.SOFT_DELETED) {
            String msg = messageSource.getMessage("msg.success.deactivated", null, LocaleContextHolder.getLocale());
            return HtmxResponseUtility.okWithRefreshTableAndWarning(msg);
        }
        String msg = messageSource.getMessage("msg.success.delete", null, LocaleContextHolder.getLocale());
        return HtmxResponseUtility.okWithRefreshTableAndSuccess(msg);
    }

    @GetMapping("/api/hierarchy/{id}")
    @PreAuthorize("hasAuthority('LOOKUP_GEOGRAPHIC')")
    @ResponseBody
    public List<GeographicHierarchyResponse> hierarchy(@PathVariable Long id) {
        List<GeographicHierarchyResponse> result = new ArrayList<>();
        Long cursor = id;
        while (cursor != null) {
            Geographic current = getGeographicEditViewUseCase.execute(cursor)
                    .orElseThrow(() -> new RuntimeException("Geographic not found"));
            result.add(new GeographicHierarchyResponse(
                    current.getId(),
                    current.getCode(),
                    current.getName(),
                    current.getType()
            ));
            cursor = current.getParentId();
        }
        return result;
    }

    public record GeographicHierarchyResponse(
            Long id,
            String code,
            String name,
            GeographicType type
    ) {
    }
}

