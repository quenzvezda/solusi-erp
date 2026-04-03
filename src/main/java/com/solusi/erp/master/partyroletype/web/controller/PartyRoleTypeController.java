package com.solusi.erp.master.partyroletype.web.controller;

import com.solusi.erp.core.annotation.DefaultRedirectUrl;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.core.infrastructure.util.PageableMapper;
import com.solusi.erp.core.dto.ApiResponse;
import com.solusi.erp.core.domain.model.DeleteResult;
import com.solusi.erp.master.partyroletype.application.usecase.command.CreatePartyRoleTypeUseCase;
import com.solusi.erp.master.partyroletype.application.usecase.command.DeletePartyRoleTypeUseCase;
import com.solusi.erp.master.partyroletype.application.usecase.command.UpdatePartyRoleTypeUseCase;
import com.solusi.erp.master.partyroletype.application.usecase.query.FindPartyRoleTypesUseCase;
import com.solusi.erp.master.partyroletype.application.usecase.query.GetPartyRoleTypeEditViewUseCase;
import com.solusi.erp.master.partyroletype.domain.model.PartyRoleType;
import com.solusi.erp.master.partyroletype.web.dto.PartyRoleTypeDetailResponse;
import com.solusi.erp.master.partyroletype.web.dto.PartyRoleTypeSaveRequest;
import com.solusi.erp.master.partyroletype.web.dto.PartyRoleTypeSummaryResponse;
import com.solusi.erp.master.partyroletype.web.mapper.PartyRoleTypeWebMapper;
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
 * Controller for PartyRoleType CRUD.
 */
@Controller
@RequestMapping("/master/party-role-types")
@RequiredArgsConstructor
@DefaultRedirectUrl
public class PartyRoleTypeController {

    private final CreatePartyRoleTypeUseCase createPartyRoleTypeUseCase;
    private final UpdatePartyRoleTypeUseCase updatePartyRoleTypeUseCase;
    private final DeletePartyRoleTypeUseCase deletePartyRoleTypeUseCase;
    private final FindPartyRoleTypesUseCase findPartyRoleTypesUseCase;
    private final GetPartyRoleTypeEditViewUseCase getPartyRoleTypeEditViewUseCase;
    private final PartyRoleTypeWebMapper webMapper;
    private final MessageSource messageSource;

    @GetMapping
    @PreAuthorize("hasAuthority('PARTY-ROLE-TYPE_READ')")
    public String list(@RequestParam(required = false) String keyword,
                       org.springframework.data.domain.Pageable springPageable,
                       Model model) {
        Pageable domainPageable = PageableMapper.toDomain(springPageable);
        com.solusi.erp.core.domain.model.Page<PartyRoleType> domainPage =
                findPartyRoleTypesUseCase.execute(keyword, domainPageable);

        List<PartyRoleTypeSummaryResponse> content = domainPage.content().stream()
                .map(webMapper::toSummaryResponse)
                .collect(Collectors.toList());

        Page<PartyRoleTypeSummaryResponse> springPage = new PageImpl<>(content, springPageable, domainPage.totalElements());
        model.addAttribute("page", springPage);
        model.addAttribute("keyword", keyword);
        return "master/party-role-types/list";
    }

    @GetMapping("/create")
    @PreAuthorize("hasAuthority('PARTY-ROLE-TYPE_CREATE')")
    public String showCreateForm(Model model) {
        PartyRoleTypeSaveRequest request = new PartyRoleTypeSaveRequest();
        request.setIsActive(true);
        model.addAttribute("partyRoleTypeRequest", request);
        return "master/party-role-types/form";
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('PARTY-ROLE-TYPE_CREATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<PartyRoleTypeDetailResponse>> create(
            @Valid @RequestBody PartyRoleTypeSaveRequest request) {
        PartyRoleType domain = createPartyRoleTypeUseCase.execute(
                request.getName(), request.getNote(), request.getIsActive());
        PartyRoleTypeDetailResponse data = webMapper.toDetailResponse(domain);
        String msg = messageSource.getMessage("msg.success.create", null, LocaleContextHolder.getLocale());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(msg, data));
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('PARTY-ROLE-TYPE_UPDATE')")
    public String showEditForm(@PathVariable Long id, Model model) {
        PartyRoleType domain = getPartyRoleTypeEditViewUseCase.execute(id)
                .orElseThrow(() -> new RuntimeException("PartyRoleType not found"));
        model.addAttribute("partyRoleTypeRequest", webMapper.toSaveRequest(domain));
        model.addAttribute("auditInfo", webMapper.toDetailResponse(domain));
        return "master/party-role-types/form";
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('PARTY-ROLE-TYPE_UPDATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<PartyRoleTypeDetailResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody PartyRoleTypeSaveRequest request) {
        PartyRoleType domain = updatePartyRoleTypeUseCase.execute(
                id, request.getName(), request.getNote(), request.getIsActive());
        PartyRoleTypeDetailResponse data = webMapper.toDetailResponse(domain);
        String msg = messageSource.getMessage("msg.success.update", null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok(ApiResponse.success(msg, data));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PARTY-ROLE-TYPE_DELETE')")
    @ResponseBody
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        DeleteResult result = deletePartyRoleTypeUseCase.execute(id);
        if (result == DeleteResult.SOFT_DELETED) {
            String msg = messageSource.getMessage("msg.success.deactivated", null, LocaleContextHolder.getLocale());
            return HtmxResponseUtility.okWithRefreshTableAndWarning(msg);
        }
        String msg = messageSource.getMessage("msg.success.delete", null, LocaleContextHolder.getLocale());
        return HtmxResponseUtility.okWithRefreshTableAndSuccess(msg);
    }
}
