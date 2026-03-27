package com.solusi.erp.inventory.facility.web.controller;

import com.solusi.erp.inventory.facility.application.usecase.command.CreateFacilityUseCase;
import com.solusi.erp.inventory.facility.application.usecase.command.DeleteFacilityUseCase;
import com.solusi.erp.inventory.facility.application.usecase.command.UpdateFacilityUseCase;
import com.solusi.erp.inventory.facility.application.usecase.query.FindFacilitiesUseCase;
import com.solusi.erp.inventory.facility.application.usecase.query.GetFacilityEditViewUseCase;
import com.solusi.erp.inventory.facility.application.usecase.query.GetFacilityLookupUseCase;
import com.solusi.erp.inventory.facility.domain.model.Facility;
import com.solusi.erp.inventory.facility.web.dto.FacilitySummaryResponse;
import com.solusi.erp.inventory.facility.web.mapper.FacilityWebMapper;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.PageRequest;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FacilityControllerTest {

    @Test
    public void listShouldReturnListViewAndModel() {
        CreateFacilityUseCase createUseCase = mock(CreateFacilityUseCase.class);
        UpdateFacilityUseCase updateUseCase = mock(UpdateFacilityUseCase.class);
        DeleteFacilityUseCase deleteUseCase = mock(DeleteFacilityUseCase.class);
        FindFacilitiesUseCase findUseCase = mock(FindFacilitiesUseCase.class);
        GetFacilityEditViewUseCase editViewUseCase = mock(GetFacilityEditViewUseCase.class);
        GetFacilityLookupUseCase lookupUseCase = mock(GetFacilityLookupUseCase.class);
        FacilityWebMapper webMapper = mock(FacilityWebMapper.class);
        MessageSource messageSource = mock(MessageSource.class);

        FacilityController controller = new FacilityController(
            createUseCase, updateUseCase, deleteUseCase,
            findUseCase, editViewUseCase, lookupUseCase,
            webMapper, messageSource
        );

        Facility domainFacility = Facility.createNew("Warehouse A", 1L, "Jl. Test", 10L, "12345", null, true);
        domainFacility.assignCode("FAC-001");
        com.solusi.erp.core.domain.model.Page<Facility> domainPage =
            new com.solusi.erp.core.domain.model.Page<>(List.of(domainFacility), 0, 20, 1L);
        when(findUseCase.execute(any(), any())).thenReturn(domainPage);

        FacilitySummaryResponse summary = new FacilitySummaryResponse();
        summary.setId(1L);
        summary.setCode("FAC-001");
        summary.setName("Warehouse A");
        when(webMapper.toSummaryResponse(any(Facility.class))).thenReturn(summary);

        org.springframework.data.domain.Pageable springPageable = PageRequest.of(0, 20);
        Model model = new ExtendedModelMap();

        String view = controller.list(null, springPageable, model);

        assertEquals("inventory/facilities/list", view);
        Object pageObj = model.getAttribute("page");
        assertThat(pageObj).isInstanceOf(org.springframework.data.domain.Page.class);
        org.springframework.data.domain.Page<?> springPage = (org.springframework.data.domain.Page<?>) pageObj;
        assertEquals(1, springPage.getTotalElements());
        Object first = springPage.getContent().get(0);
        assertThat(first).isInstanceOf(FacilitySummaryResponse.class);
        assertEquals("FAC-001", ((FacilitySummaryResponse) first).getCode());
    }
}
