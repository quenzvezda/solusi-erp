package com.solusi.erp.master.partyroletype.web.controller;

import com.solusi.erp.master.partyroletype.application.usecase.command.CreatePartyRoleTypeUseCase;
import com.solusi.erp.master.partyroletype.application.usecase.command.DeletePartyRoleTypeUseCase;
import com.solusi.erp.master.partyroletype.application.usecase.command.UpdatePartyRoleTypeUseCase;
import com.solusi.erp.master.partyroletype.application.usecase.query.FindPartyRoleTypesUseCase;
import com.solusi.erp.master.partyroletype.application.usecase.query.GetPartyRoleTypeEditViewUseCase;
import com.solusi.erp.master.partyroletype.domain.model.PartyRoleType;
import com.solusi.erp.master.partyroletype.web.dto.PartyRoleTypeSummaryResponse;
import com.solusi.erp.master.partyroletype.web.mapper.PartyRoleTypeWebMapper;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PartyRoleTypeControllerTest {

    @Test
    void listShouldReturnListViewAndModel() {
        CreatePartyRoleTypeUseCase createPartyRoleTypeUseCase = mock(CreatePartyRoleTypeUseCase.class);
        UpdatePartyRoleTypeUseCase updatePartyRoleTypeUseCase = mock(UpdatePartyRoleTypeUseCase.class);
        DeletePartyRoleTypeUseCase deletePartyRoleTypeUseCase = mock(DeletePartyRoleTypeUseCase.class);
        FindPartyRoleTypesUseCase findPartyRoleTypesUseCase = mock(FindPartyRoleTypesUseCase.class);
        GetPartyRoleTypeEditViewUseCase getPartyRoleTypeEditViewUseCase = mock(GetPartyRoleTypeEditViewUseCase.class);
        PartyRoleTypeWebMapper webMapper = mock(PartyRoleTypeWebMapper.class);
        MessageSource messageSource = mock(MessageSource.class);

        PartyRoleTypeController controller = new PartyRoleTypeController(
                createPartyRoleTypeUseCase, updatePartyRoleTypeUseCase, deletePartyRoleTypeUseCase,
                findPartyRoleTypesUseCase, getPartyRoleTypeEditViewUseCase, webMapper, messageSource);

        PartyRoleType domainPRT = PartyRoleType.createNew("PRT-001", "Customer", null, true);
        com.solusi.erp.core.domain.model.Page<PartyRoleType> domainPage =
                new com.solusi.erp.core.domain.model.Page<>(List.of(domainPRT), 0, 20, 1L);
        when(findPartyRoleTypesUseCase.execute(any(), any())).thenReturn(domainPage);

        PartyRoleTypeSummaryResponse summary = new PartyRoleTypeSummaryResponse();
        summary.setId(1L);
        summary.setCode("PRT-001");
        summary.setName("Customer");
        when(webMapper.toSummaryResponse(any(PartyRoleType.class))).thenReturn(summary);

        org.springframework.data.domain.Pageable springPageable =
                org.springframework.data.domain.PageRequest.of(0, 20);
        Model model = new ExtendedModelMap();

        String view = controller.list(null, springPageable, model);

        assertEquals("master/party-role-types/list", view);
        Object pageObj = model.getAttribute("page");
        assertThat(pageObj).isInstanceOf(org.springframework.data.domain.Page.class);
        org.springframework.data.domain.Page<?> springPage =
                (org.springframework.data.domain.Page<?>) pageObj;
        assertEquals(1, springPage.getTotalElements());
        assertEquals("PRT-001", ((PartyRoleTypeSummaryResponse) springPage.getContent().get(0)).getCode());
    }
}
