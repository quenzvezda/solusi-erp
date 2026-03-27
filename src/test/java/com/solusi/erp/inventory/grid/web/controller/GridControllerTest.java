package com.solusi.erp.inventory.grid.web.controller;

import com.solusi.erp.inventory.facility.application.usecase.query.FindFacilitiesUseCase;
import com.solusi.erp.inventory.facility.application.usecase.query.GetFacilityEditViewUseCase;
import com.solusi.erp.inventory.facility.web.mapper.FacilityWebMapper;
import com.solusi.erp.inventory.grid.application.usecase.command.CreateGridUseCase;
import com.solusi.erp.inventory.grid.application.usecase.command.DeleteGridUseCase;
import com.solusi.erp.inventory.grid.application.usecase.command.UpdateGridUseCase;
import com.solusi.erp.inventory.grid.application.usecase.query.FindGridsUseCase;
import com.solusi.erp.inventory.grid.application.usecase.query.GetGridEditViewUseCase;
import com.solusi.erp.inventory.grid.application.usecase.query.GetGridLookupUseCase;
import com.solusi.erp.inventory.grid.domain.model.Grid;
import com.solusi.erp.inventory.grid.web.dto.GridSummaryResponse;
import com.solusi.erp.inventory.grid.web.mapper.GridWebMapper;
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

public class GridControllerTest {

    @Test
    public void listShouldReturnListViewAndModel() {
        CreateGridUseCase createUseCase = mock(CreateGridUseCase.class);
        UpdateGridUseCase updateUseCase = mock(UpdateGridUseCase.class);
        DeleteGridUseCase deleteUseCase = mock(DeleteGridUseCase.class);
        FindGridsUseCase findUseCase = mock(FindGridsUseCase.class);
        GetGridEditViewUseCase editViewUseCase = mock(GetGridEditViewUseCase.class);
        GetGridLookupUseCase lookupUseCase = mock(GetGridLookupUseCase.class);
        GridWebMapper webMapper = mock(GridWebMapper.class);
        FindFacilitiesUseCase findFacilitiesUseCase = mock(FindFacilitiesUseCase.class);
        GetFacilityEditViewUseCase getFacilityEditViewUseCase = mock(GetFacilityEditViewUseCase.class);
        FacilityWebMapper facilityWebMapper = mock(FacilityWebMapper.class);
        MessageSource messageSource = mock(MessageSource.class);

        GridController controller = new GridController(
            createUseCase, updateUseCase, deleteUseCase,
            findUseCase, editViewUseCase, lookupUseCase,
            webMapper, findFacilitiesUseCase, getFacilityEditViewUseCase,
            facilityWebMapper, messageSource
        );

        Grid domainGrid = Grid.createNew(1L, "GRD-001", "Storage Area A", null, true);
        com.solusi.erp.core.domain.model.Page<Grid> domainPage =
            new com.solusi.erp.core.domain.model.Page<>(List.of(domainGrid), 0, 20, 1L);
        when(findUseCase.execute(any(), any(), any())).thenReturn(domainPage);

        GridSummaryResponse summary = new GridSummaryResponse();
        summary.setId(1L);
        summary.setCode("GRD-001");
        summary.setName("Storage Area A");
        when(webMapper.toSummaryResponse(any(Grid.class))).thenReturn(summary);

        org.springframework.data.domain.Pageable springPageable = PageRequest.of(0, 20);
        Model model = new ExtendedModelMap();

        // facilityId=null so getFacilityEditViewUseCase and findFacilitiesUseCase are NOT called
        String view = controller.list(null, null, springPageable, model);

        assertEquals("inventory/grids/list", view);
        Object pageObj = model.getAttribute("page");
        assertThat(pageObj).isInstanceOf(org.springframework.data.domain.Page.class);
        org.springframework.data.domain.Page<?> springPage = (org.springframework.data.domain.Page<?>) pageObj;
        assertEquals(1, springPage.getTotalElements());
    }
}
