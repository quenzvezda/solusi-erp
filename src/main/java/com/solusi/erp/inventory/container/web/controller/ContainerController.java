package com.solusi.erp.inventory.container.web.controller;

import com.solusi.erp.core.annotation.DefaultRedirectUrl;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.core.util.PageableMapper;
import com.solusi.erp.core.dto.ApiResponse;
import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.inventory.container.application.usecase.command.*;
import com.solusi.erp.inventory.container.application.usecase.query.*;
import com.solusi.erp.inventory.container.domain.model.Container;
import com.solusi.erp.inventory.container.web.dto.*;
import com.solusi.erp.inventory.container.web.mapper.ContainerWebMapper;
import com.solusi.erp.inventory.grid.application.usecase.query.GetGridEditViewUseCase;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/inventory/containers")
@RequiredArgsConstructor
@DefaultRedirectUrl
public class ContainerController {

    private final CreateContainerUseCase createContainerUseCase;
    private final UpdateContainerUseCase updateContainerUseCase;
    private final DeleteContainerUseCase deleteContainerUseCase;
    private final FindContainersUseCase findContainersUseCase;
    private final GetContainerEditViewUseCase getContainerEditViewUseCase;
    private final GetContainerLookupUseCase getContainerLookupUseCase;
    private final ContainerWebMapper webMapper;
    private final GetGridEditViewUseCase getGridEditViewUseCase;
    private final GridWebMapper gridWebMapper;
    private final MessageSource messageSource;

    @GetMapping
    @PreAuthorize("hasAuthority('CONTAINER_READ')")
    public String list(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) Long gridId,
                       org.springframework.data.domain.Pageable springPageable,
                       Model model) {
        Pageable domainPageable = PageableMapper.toDomain(springPageable);
        com.solusi.erp.core.domain.model.Page<Container> domainPage =
            findContainersUseCase.execute(keyword, gridId, domainPageable);

        List<ContainerSummaryResponse> content = domainPage.content().stream()
            .map(webMapper::toSummaryResponse)
            .collect(Collectors.toList());

        Page<ContainerSummaryResponse> springPage = new PageImpl<>(content, springPageable, domainPage.totalElements());
        model.addAttribute("page", springPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("gridId", gridId);

        if (gridId != null) {
            getGridEditViewUseCase.execute(gridId)
                .map(gridWebMapper::toSummaryResponse)
                .ifPresent(g -> model.addAttribute("selectedGrid", g));
        }

        return "inventory/containers/list";
    }

    @GetMapping("/create")
    @PreAuthorize("hasAuthority('CONTAINER_CREATE')")
    public String showCreateForm(@RequestParam(required = false) Long gridId, Model model) {
        ContainerSaveRequest request = new ContainerSaveRequest();
        if (gridId != null) {
            request.setGridId(gridId);
        }
        model.addAttribute("containerRequest", request);
        return "inventory/containers/form";
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('CONTAINER_CREATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<ContainerDetailResponse>> create(@Valid @RequestBody ContainerSaveRequest request) {
        Container domain = createContainerUseCase.execute(
            request.getGridId(), request.getName(), request.getBarcode(),
            request.getLength(), request.getWidth(), request.getHeight(),
            request.getMaxWeight(), request.getNote(), request.getIsActive());
        ContainerDetailResponse data = webMapper.toDetailResponse(domain);
        String msg = messageSource.getMessage("msg.success.create", null, LocaleContextHolder.getLocale());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(msg, data));
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('CONTAINER_UPDATE')")
    public String showEditForm(@PathVariable Long id, Model model) {
        Container domain = getContainerEditViewUseCase.execute(id)
            .orElseThrow(() -> new RuntimeException("Container not found"));
        model.addAttribute("containerRequest", webMapper.toSaveRequest(domain));
        model.addAttribute("containerUI", buildContainerUI(domain));
        model.addAttribute("auditInfo", webMapper.toDetailResponse(domain));
        return "inventory/containers/form";
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('CONTAINER_UPDATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<ContainerDetailResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody ContainerSaveRequest request) {
        Container domain = updateContainerUseCase.execute(
            id, request.getName(), request.getBarcode(),
            request.getLength(), request.getWidth(), request.getHeight(),
            request.getMaxWeight(), request.getNote(), request.getIsActive());
        ContainerDetailResponse data = webMapper.toDetailResponse(domain);
        String msg = messageSource.getMessage("msg.success.update", null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok(ApiResponse.success(msg, data));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('CONTAINER_DELETE')")
    @ResponseBody
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deleteContainerUseCase.execute(id);
        String msg = messageSource.getMessage("msg.success.delete", null, LocaleContextHolder.getLocale());
        return HtmxResponseUtility.okWithRefreshTableAndSuccess(msg);
    }

    @GetMapping("/lookup")
    @PreAuthorize("hasAuthority('LOOKUP_CONTAINER')")
    @ResponseBody
    public List<LookupDto> lookup(@RequestParam(required = false) String keyword,
                                  @RequestParam(defaultValue = "10") int limit) {
        return getContainerLookupUseCase.search(keyword, limit);
    }

    @GetMapping("/lookup/{id}")
    @PreAuthorize("hasAuthority('LOOKUP_CONTAINER')")
    @ResponseBody
    public LookupDto lookupById(@PathVariable Long id) {
        return getContainerLookupUseCase.getById(id);
    }

    private Map<String, Object> buildContainerUI(Container domain) {
        Map<String, Object> ui = new HashMap<>();
        ui.put("gridName", domain.getGridName());
        ui.put("gridCode", "");
        return ui;
    }
}
