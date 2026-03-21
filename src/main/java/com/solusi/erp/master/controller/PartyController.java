package com.solusi.erp.master.controller;

import com.solusi.erp.core.annotation.DefaultRedirectUrl;
import com.solusi.erp.core.dto.ApiResponse;
import com.solusi.erp.master.dto.PartyRequest;
import com.solusi.erp.master.dto.PartyResponse;
import com.solusi.erp.master.model.AddressType;
import com.solusi.erp.master.model.PartyType;
import com.solusi.erp.master.service.PartyService;
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
 * Controller for Party (Business Partner) CRUD.
 */
@Controller
@RequestMapping("/master/parties")
@RequiredArgsConstructor
@DefaultRedirectUrl
public class PartyController {

    private final PartyService service;
    private final MessageSource messageSource;

    @GetMapping
    @PreAuthorize("hasAuthority('PARTY_READ')")
    public String list(@RequestParam(value = "keyword", required = false) String keyword,
                       Pageable pageable,
                       Model model) {
        model.addAttribute("page", service.findAll(keyword, pageable));
        model.addAttribute("keyword", keyword);
        return "master/parties/list";
    }

    @GetMapping("/create")
    @PreAuthorize("hasAuthority('PARTY_CREATE')")
    public String showCreateForm(Model model) {
        model.addAttribute("partyRequest", new PartyRequest());
        populateFormOptions(model);
        return "master/parties/form";
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('PARTY_CREATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<PartyResponse>> create(@Valid @RequestBody PartyRequest request) {
        PartyResponse data = service.create(request);
        String message = messageSource.getMessage("msg.success.create", null, LocaleContextHolder.getLocale());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(message, data));
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('PARTY_UPDATE')")
    public String showEditForm(@PathVariable Long id, Model model) {
        var viewDto = service.getPartyEditView(id);
        model.addAttribute("partyRequest", viewDto.getRequest());
        model.addAttribute("auditInfo", viewDto.getAudit());
        populateFormOptions(model);
        return "master/parties/form";
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('PARTY_UPDATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<PartyResponse>> update(@PathVariable Long id,
                                                             @Valid @RequestBody PartyRequest request) {
        PartyResponse data = service.update(id, request);
        String message = messageSource.getMessage("msg.success.update", null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok(ApiResponse.success(message, data));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PARTY_DELETE')")
    @ResponseBody
    public ResponseEntity<Void> deleteHtmx(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok()
                .header("HX-Trigger", "refresh-table")
                .build();
    }

    private void populateFormOptions(Model model) {
        model.addAttribute("partyTypes", PartyType.values());
        model.addAttribute("addressTypes", AddressType.values());
        model.addAttribute("roleTypes", service.findAllRoleTypes());
        model.addAttribute("idTypes", service.findAllIdTypes());
    }
}
