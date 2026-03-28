package com.solusi.erp.master.party.web.controller;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.master.shared.model.AddressType;
import com.solusi.erp.master.shared.model.PartyType;
import com.solusi.erp.master.party.application.usecase.command.CreatePartyUseCase;
import com.solusi.erp.master.party.application.usecase.command.DeletePartyUseCase;
import com.solusi.erp.master.party.application.usecase.command.UpdatePartyUseCase;
import com.solusi.erp.master.party.application.usecase.query.FindPartiesUseCase;
import com.solusi.erp.master.party.application.usecase.query.GetPartyEditViewUseCase;
import com.solusi.erp.master.party.domain.model.Party;
import com.solusi.erp.master.party.web.dto.PartySaveRequest;
import com.solusi.erp.master.party.web.dto.PartySummaryResponse;
import com.solusi.erp.master.party.web.mapper.PartyWebMapper;
import com.solusi.erp.master.party.infrastructure.persistence.PartyIdentificationTypeJpaRepository;
import com.solusi.erp.master.partyroletype.infrastructure.persistence.PartyRoleTypeJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PartyControllerTest {

    @Test
    void listShouldReturnListViewAndModel() {
        CreatePartyUseCase createPartyUseCase = mock(CreatePartyUseCase.class);
        UpdatePartyUseCase updatePartyUseCase = mock(UpdatePartyUseCase.class);
        DeletePartyUseCase deletePartyUseCase = mock(DeletePartyUseCase.class);
        FindPartiesUseCase findPartiesUseCase = mock(FindPartiesUseCase.class);
        GetPartyEditViewUseCase getPartyEditViewUseCase = mock(GetPartyEditViewUseCase.class);
        PartyWebMapper webMapper = mock(PartyWebMapper.class);
        MessageSource messageSource = mock(MessageSource.class);
        PartyRoleTypeJpaRepository roleTypeRepository = mock(PartyRoleTypeJpaRepository.class);
        PartyIdentificationTypeJpaRepository idTypeRepository = mock(PartyIdentificationTypeJpaRepository.class);

        PartyController controller = new PartyController(
                createPartyUseCase, updatePartyUseCase, deletePartyUseCase,
                findPartiesUseCase, getPartyEditViewUseCase, webMapper, messageSource,
                roleTypeRepository, idTypeRepository
        );

        Party domainParty = new Party(
                new AuditMetadata(1L, 1L, null, null, null, null),
                "PTY-001", "Mr.", "Acme Corp", PartyType.ORGANIZATION,
                "Note", true, "a@corp.com", "08123",
                new HashSet<>(), List.of("Customer"), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        com.solusi.erp.core.domain.model.Page<Party> domainPage =
                new com.solusi.erp.core.domain.model.Page<>(List.of(domainParty), 0, 20, 1L);
        when(findPartiesUseCase.execute(any(), any())).thenReturn(domainPage);

        PartySummaryResponse summary = new PartySummaryResponse();
        summary.setId(1L);
        summary.setCode("PTY-001");
        summary.setName("Acme Corp");
        summary.setType(PartyType.ORGANIZATION);
        summary.setRoleNames(List.of("Customer"));
        summary.setIsActive(true);
        when(webMapper.toSummaryResponse(any(Party.class))).thenReturn(summary);

        org.springframework.data.domain.Pageable springPageable =
                org.springframework.data.domain.PageRequest.of(0, 20);
        Model model = new ExtendedModelMap();

        String view = controller.list(null, springPageable, model);

        assertEquals("master/parties/list", view);
        Object pageObj = model.getAttribute("page");
        assertThat(pageObj).isInstanceOf(org.springframework.data.domain.Page.class);
        org.springframework.data.domain.Page<?> springPage =
                (org.springframework.data.domain.Page<?>) pageObj;
        assertEquals(1, springPage.getTotalElements());
        assertEquals("PTY-001", ((PartySummaryResponse) springPage.getContent().get(0)).getCode());
    }

    @Test
    void showCreateFormShouldPopulateDefaultsAndReturnFormView() {
        CreatePartyUseCase createPartyUseCase = mock(CreatePartyUseCase.class);
        UpdatePartyUseCase updatePartyUseCase = mock(UpdatePartyUseCase.class);
        DeletePartyUseCase deletePartyUseCase = mock(DeletePartyUseCase.class);
        FindPartiesUseCase findPartiesUseCase = mock(FindPartiesUseCase.class);
        GetPartyEditViewUseCase getPartyEditViewUseCase = mock(GetPartyEditViewUseCase.class);
        PartyWebMapper webMapper = mock(PartyWebMapper.class);
        MessageSource messageSource = mock(MessageSource.class);
        PartyRoleTypeJpaRepository roleTypeRepository = mock(PartyRoleTypeJpaRepository.class);
        PartyIdentificationTypeJpaRepository idTypeRepository = mock(PartyIdentificationTypeJpaRepository.class);
        when(roleTypeRepository.findAll()).thenReturn(List.of());
        when(idTypeRepository.findAll()).thenReturn(List.of());

        PartyController controller = new PartyController(
                createPartyUseCase, updatePartyUseCase, deletePartyUseCase,
                findPartiesUseCase, getPartyEditViewUseCase, webMapper, messageSource,
                roleTypeRepository, idTypeRepository
        );

        Model model = new ExtendedModelMap();
        String view = controller.showCreateForm(model);

        assertEquals("master/parties/form", view);
        assertThat(model.getAttribute("partyRequest")).isInstanceOf(PartySaveRequest.class);
        assertThat(model.getAttribute("partyTypes")).isEqualTo(PartyType.values());
        assertThat(model.getAttribute("addressTypes")).isEqualTo(AddressType.values());
    }
}

