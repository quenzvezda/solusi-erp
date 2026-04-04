package com.solusi.erp.master.party.web.controller;

import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.master.party.application.usecase.query.FindPartiesAvailableForUserUseCase;
import com.solusi.erp.master.party.application.usecase.query.FindPartiesForLookupUseCase;
import com.solusi.erp.master.party.domain.model.Party;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/lookup/parties")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('LOOKUP_PARTY')")
public class PartyLookupController {

    private final FindPartiesForLookupUseCase findPartiesForLookupUseCase;
    private final FindPartiesAvailableForUserUseCase findPartiesAvailableForUserUseCase;
    private final MessageSource messageSource;

    @GetMapping
    public List<LookupDto> search(@RequestParam(value = "q", defaultValue = "") String q) {
        Locale locale = LocaleContextHolder.getLocale();
        return findPartiesForLookupUseCase.execute(q).stream()
                .map(p -> mapToLookupDto(p, locale))
                .collect(Collectors.toList());
    }

    @GetMapping("/by-role-type")
    public List<LookupDto> searchByRoleType(
            @RequestParam(value = "q", defaultValue = "") String q,
            @RequestParam(value = "roleTypeCode") String roleTypeCode,
            @RequestParam(value = "excludePartyId", required = false) Long excludePartyId) {
        Locale locale = LocaleContextHolder.getLocale();
        return findPartiesForLookupUseCase.executeByRoleType(q, roleTypeCode, excludePartyId).stream()
                .map(p -> mapToLookupDto(p, locale))
                .collect(Collectors.toList());
    }

    @GetMapping("/available-for-user")
    public List<LookupDto> availableForUser(
            @RequestParam(value = "q", defaultValue = "") String q,
            @RequestParam(value = "excludePartyId", required = false) Long excludePartyId,
            @RequestParam(value = "limit", defaultValue = "10") int limit) {
        Locale locale = LocaleContextHolder.getLocale();
        return findPartiesAvailableForUserUseCase.execute(q, excludePartyId).stream()
                .map(p -> mapToLookupDto(p, locale))
                .collect(Collectors.toList());
    }

    private LookupDto mapToLookupDto(Party p, Locale locale) {
        String fullName = (StringUtils.hasText(p.getSalutation()) ? p.getSalutation() + " " : "") + p.getName();
        String typeLabel = messageSource.getMessage(
                "label.party.type." + p.getType().name().toLowerCase(), null, locale);
        String subText = p.getCode() + " - " + typeLabel;
        return new LookupDto(p.getId(), fullName, subText);
    }
}
