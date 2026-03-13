package com.solusi.erp.master.controller;

import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.master.repository.PartyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/lookup/parties")
@RequiredArgsConstructor
public class PartyLookupController {

    private final PartyRepository partyRepository;

    @GetMapping
    public List<LookupDto> search(@RequestParam(value = "q", defaultValue = "") String q) {
        return partyRepository.search(q, PageRequest.of(0, 20))
                .stream()
                .map(p -> new LookupDto(p.getId(), p.getName(), p.getCode()))
                .collect(Collectors.toList());
    }
}
