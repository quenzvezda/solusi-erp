package com.solusi.erp.master.controller;

import com.solusi.erp.core.annotation.DefaultRedirectUrl;
import com.solusi.erp.core.dto.ApiResponse;
import com.solusi.erp.master.dto.PartyRoleTypeRequest;
import com.solusi.erp.master.dto.PartyRoleTypeResponse;
import com.solusi.erp.master.service.PartyRoleTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for PartyRoleType (Party Role Type) CRUD Management.
 */
@Controller
@RequestMapping("/master/party-role-types")
@RequiredArgsConstructor
@DefaultRedirectUrl
public class PartyRoleTypeController {

    private final PartyRoleTypeService service;
    private final MessageSource messageSource;

    @GetMapping
    @PreAuthorize("hasAuthority('PARTY-ROLE-TYPE_READ')")
    public String list(@RequestParam(value = "keyword", required = false) String keyword,
            Pageable pageable,
            Model model) {
        model.addAttribute("page", service.findAll(keyword, pageable));
        return "master/party-role-types/list";
    }

    @GetMapping("/create")
    @PreAuthorize("hasAuthority('PARTY-ROLE-TYPE_CREATE')")
    public String showCreateForm(Model model) {
        model.addAttribute("roleTypeRequest", new PartyRoleTypeRequest());
        return "master/party-role-types/form";
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('PARTY-ROLE-TYPE_CREATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<PartyRoleTypeResponse>> create(@Valid @RequestBody PartyRoleTypeRequest request) {
        PartyRoleTypeResponse data = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(msg("msg.success.create"), data));
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('PARTY-ROLE-TYPE_UPDATE')")
    public String showEditForm(@PathVariable Long id, Model model) {
        var viewDto = service.getEditView(id);
        model.addAttribute("roleTypeRequest", viewDto.getRequest());
        model.addAttribute("auditInfo", viewDto.getAudit());
        return "master/party-role-types/form";
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('PARTY-ROLE-TYPE_UPDATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<PartyRoleTypeResponse>> update(@PathVariable Long id, @Valid @RequestBody PartyRoleTypeRequest request) {
        PartyRoleTypeResponse data = service.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(msg("msg.success.update"), data));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PARTY-ROLE-TYPE_DELETE')")
    @ResponseBody
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok()
                .header("HX-Trigger", "refresh-table")
                .build();
    }

    private String msg(String key) {
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }
}
