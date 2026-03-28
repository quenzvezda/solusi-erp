package com.solusi.erp.master.party.web.controller;

import com.solusi.erp.core.annotation.DefaultRedirectUrl;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.core.util.PageableMapper;
import com.solusi.erp.core.dto.ApiResponse;
import com.solusi.erp.master.shared.model.AddressType;
import com.solusi.erp.master.shared.model.PartyType;
import com.solusi.erp.master.party.application.usecase.command.CreatePartyUseCase;
import com.solusi.erp.master.party.application.usecase.command.DeletePartyUseCase;
import com.solusi.erp.master.party.application.usecase.command.UpdatePartyUseCase;
import com.solusi.erp.master.party.application.usecase.query.FindPartiesUseCase;
import com.solusi.erp.master.party.application.usecase.query.GetPartyEditViewUseCase;
import com.solusi.erp.master.party.domain.model.Party;
import com.solusi.erp.master.party.domain.model.PartyAddressData;
import com.solusi.erp.master.party.domain.model.PartyContactData;
import com.solusi.erp.master.party.domain.model.PartyIdentificationData;
import com.solusi.erp.master.party.web.dto.PartyAddressRequest;
import com.solusi.erp.master.party.web.dto.PartyContactRequest;
import com.solusi.erp.master.party.web.dto.PartyDetailResponse;
import com.solusi.erp.master.party.web.dto.PartyIdentificationRequest;
import com.solusi.erp.master.party.web.dto.PartySaveRequest;
import com.solusi.erp.master.party.web.dto.PartySummaryResponse;
import com.solusi.erp.master.party.web.mapper.PartyWebMapper;
import com.solusi.erp.master.party.infrastructure.persistence.PartyIdentificationTypeJpaRepository;
import com.solusi.erp.master.partyroletype.infrastructure.persistence.PartyRoleTypeJpaRepository;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/master/parties")
@RequiredArgsConstructor
@DefaultRedirectUrl
public class PartyController {

    private final CreatePartyUseCase createPartyUseCase;
    private final UpdatePartyUseCase updatePartyUseCase;
    private final DeletePartyUseCase deletePartyUseCase;
    private final FindPartiesUseCase findPartiesUseCase;
    private final GetPartyEditViewUseCase getPartyEditViewUseCase;
    private final PartyWebMapper webMapper;
    private final MessageSource messageSource;
    private final PartyRoleTypeJpaRepository roleTypeRepository;
    private final PartyIdentificationTypeJpaRepository idTypeRepository;

    @GetMapping
    @PreAuthorize("hasAuthority('PARTY_READ')")
    public String list(@RequestParam(required = false) String keyword,
                       org.springframework.data.domain.Pageable springPageable,
                       Model model) {
        Pageable domainPageable = PageableMapper.toDomain(springPageable);
        com.solusi.erp.core.domain.model.Page<Party> domainPage = findPartiesUseCase.execute(keyword, domainPageable);
        List<PartySummaryResponse> content = domainPage.content().stream()
                .map(webMapper::toSummaryResponse).collect(Collectors.toList());
        Page<PartySummaryResponse> springPage = new PageImpl<>(content, springPageable, domainPage.totalElements());
        model.addAttribute("page", springPage);
        model.addAttribute("keyword", keyword);
        return "master/parties/list";
    }

    @GetMapping("/create")
    @PreAuthorize("hasAuthority('PARTY_CREATE')")
    public String showCreateForm(Model model) {
        model.addAttribute("partyRequest", new PartySaveRequest());
        populateFormOptions(model);
        return "master/parties/form";
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('PARTY_CREATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<PartyDetailResponse>> create(@Valid @RequestBody PartySaveRequest request) {
        Party domain = createPartyUseCase.execute(
                request.getName(), request.getSalutation(), request.getType(),
                request.getNotes(), request.getIsActive(), request.getEmail(), request.getPhone(),
                toContactDataList(request.getContacts()), toAddressDataList(request.getAddresses()),
                toIdentificationDataList(request.getIdentifications()), request.getRoleIds());
        PartyDetailResponse data = webMapper.toDetailResponse(domain);
        String msg = messageSource.getMessage("msg.success.create", null, LocaleContextHolder.getLocale());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(msg, data));
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('PARTY_UPDATE')")
    public String showEditForm(@PathVariable Long id, Model model) {
        Party domain = getPartyEditViewUseCase.execute(id)
                .orElseThrow(() -> new RuntimeException("Party not found"));
        model.addAttribute("partyRequest", webMapper.toSaveRequest(domain));
        model.addAttribute("auditInfo", webMapper.toDetailResponse(domain));
        populateFormOptions(model);
        return "master/parties/form";
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('PARTY_UPDATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<PartyDetailResponse>> update(@PathVariable Long id,
                                                                    @Valid @RequestBody PartySaveRequest request) {
        Party domain = updatePartyUseCase.execute(
                id, request.getName(), request.getSalutation(), request.getType(),
                request.getNotes(), request.getIsActive(), request.getEmail(), request.getPhone(),
                toContactDataList(request.getContacts()), toAddressDataList(request.getAddresses()),
                toIdentificationDataList(request.getIdentifications()), request.getRoleIds());
        PartyDetailResponse data = webMapper.toDetailResponse(domain);
        String msg = messageSource.getMessage("msg.success.update", null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok(ApiResponse.success(msg, data));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PARTY_DELETE')")
    @ResponseBody
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deletePartyUseCase.execute(id);
        String msg = messageSource.getMessage("msg.success.delete", null, LocaleContextHolder.getLocale());
        return HtmxResponseUtility.okWithRefreshTableAndSuccess(msg);
    }

    private void populateFormOptions(Model model) {
        model.addAttribute("partyTypes", PartyType.values());
        model.addAttribute("addressTypes", AddressType.values());
        model.addAttribute("roleTypes", roleTypeRepository.findAll());
        model.addAttribute("idTypes", idTypeRepository.findAll());
    }

    private List<PartyContactData> toContactDataList(List<PartyContactRequest> list) {
        if (list == null) return Collections.emptyList();
        return list.stream().map(r -> new PartyContactData(r.getId(), r.getLabel(), r.getMobile(),
                r.getPhone(), r.getEmail(), r.getIsActive(), r.getIsDefault())).collect(Collectors.toList());
    }

    private List<PartyAddressData> toAddressDataList(List<PartyAddressRequest> list) {
        if (list == null) return Collections.emptyList();
        return list.stream().map(r -> new PartyAddressData(r.getId(), r.getTypes(), r.getAddressLine1(),
                r.getCityId(), r.getPostalCode(), r.getIsActive(), r.getIsDefault())).collect(Collectors.toList());
    }

    private List<PartyIdentificationData> toIdentificationDataList(List<PartyIdentificationRequest> list) {
        if (list == null) return Collections.emptyList();
        return list.stream().map(r -> new PartyIdentificationData(r.getId(), r.getTypeId(), r.getIdNumber(),
                r.getIssuedDate(), r.getExpiryDate(), r.getIsActive(), r.getIsDefault())).collect(Collectors.toList());
    }
}


