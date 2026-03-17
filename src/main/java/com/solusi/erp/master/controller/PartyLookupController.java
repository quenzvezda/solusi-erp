package com.solusi.erp.master.controller;

import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.master.repository.PartyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/lookup/parties")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('LOOKUP_PARTY')")
public class PartyLookupController {

    private final PartyRepository partyRepository;
    private final MessageSource messageSource;

    @GetMapping
    public List<LookupDto> search(@RequestParam(value = "q", defaultValue = "") String q) {
        Locale locale = LocaleContextHolder.getLocale();
        return partyRepository.search(q, PageRequest.of(0, 20))
                .stream()
                .map(p -> {
                    String fullName = (StringUtils.hasText(p.getSalutation()) ? p.getSalutation() + " " : "") + p.getName();
                    String typeLabel = messageSource.getMessage("label.party.type." + p.getType().name().toLowerCase(), null, locale);
                    String subText = p.getCode() + " - " + typeLabel;
                    return new LookupDto(p.getId(), fullName, subText);
                })
                .collect(Collectors.toList());
    }
}
