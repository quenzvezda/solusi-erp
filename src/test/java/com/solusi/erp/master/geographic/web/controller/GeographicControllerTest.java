package com.solusi.erp.master.geographic.web.controller;

import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.master.geographic.application.usecase.command.CreateGeographicUseCase;
import com.solusi.erp.master.geographic.application.usecase.command.DeleteGeographicUseCase;
import com.solusi.erp.master.geographic.application.usecase.command.UpdateGeographicUseCase;
import com.solusi.erp.master.geographic.application.usecase.query.FindGeographicsUseCase;
import com.solusi.erp.master.geographic.application.usecase.query.GetGeographicEditViewUseCase;
import com.solusi.erp.master.geographic.domain.model.Geographic;
import com.solusi.erp.master.geographic.domain.port.GeographicLookupProvider;
import com.solusi.erp.master.geographic.web.dto.GeographicSaveRequest;
import com.solusi.erp.master.geographic.web.dto.GeographicSummaryResponse;
import com.solusi.erp.master.geographic.web.mapper.GeographicWebMapper;
import com.solusi.erp.master.shared.model.GeographicType;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.PageRequest;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

public class GeographicControllerTest {

    @Test
    public void listShouldReturnListViewAndModel() {
        CreateGeographicUseCase createUseCase = mock(CreateGeographicUseCase.class);
        UpdateGeographicUseCase updateUseCase = mock(UpdateGeographicUseCase.class);
        DeleteGeographicUseCase deleteUseCase = mock(DeleteGeographicUseCase.class);
        FindGeographicsUseCase findUseCase = mock(FindGeographicsUseCase.class);
        GetGeographicEditViewUseCase getEditViewUseCase = mock(GetGeographicEditViewUseCase.class);
        GeographicWebMapper webMapper = mock(GeographicWebMapper.class);
        MessageSource messageSource = mock(MessageSource.class);
        GeographicLookupProvider lookupProvider = mock(GeographicLookupProvider.class);

        GeographicController controller = new GeographicController(
                createUseCase, updateUseCase, deleteUseCase,
                findUseCase, getEditViewUseCase, webMapper, messageSource, lookupProvider);

        Geographic domainGeo = Geographic.createNew("ID", "Indonesia", GeographicType.COUNTRY,
                null, null, Boolean.TRUE);
        com.solusi.erp.core.domain.model.Page<Geographic> domainPage =
                new com.solusi.erp.core.domain.model.Page<>(List.of(domainGeo), 0, 20, 1L);
        when(findUseCase.execute(any(), any(), any())).thenReturn(domainPage);

        GeographicSummaryResponse summary = new GeographicSummaryResponse();
        summary.setId(1L);
        summary.setCode("ID");
        summary.setName("Indonesia");
        summary.setType(GeographicType.COUNTRY);
        summary.setIsActive(Boolean.TRUE);
        when(webMapper.toSummaryResponse(any(Geographic.class))).thenReturn(summary);

        org.springframework.data.domain.Pageable springPageable = PageRequest.of(0, 20);
        Model model = new ExtendedModelMap();

        String view = controller.list(null, null, springPageable, model);

        assertEquals("master/geographic/list", view);
        Object pageObj = model.getAttribute("page");
        assertThat(pageObj).isInstanceOf(org.springframework.data.domain.Page.class);
        org.springframework.data.domain.Page<?> springPage =
                (org.springframework.data.domain.Page<?>) pageObj;
        assertEquals(1, springPage.getTotalElements());
        Object first = springPage.getContent().get(0);
        assertThat(first).isInstanceOf(GeographicSummaryResponse.class);
        assertEquals("ID", ((GeographicSummaryResponse) first).getCode());
    }

    @Test
    public void showCreateFormWithParentIdShouldPopulateParentSubtext() {
        CreateGeographicUseCase createUseCase = mock(CreateGeographicUseCase.class);
        UpdateGeographicUseCase updateUseCase = mock(UpdateGeographicUseCase.class);
        DeleteGeographicUseCase deleteUseCase = mock(DeleteGeographicUseCase.class);
        FindGeographicsUseCase findUseCase = mock(FindGeographicsUseCase.class);
        GetGeographicEditViewUseCase getEditViewUseCase = mock(GetGeographicEditViewUseCase.class);
        GeographicWebMapper webMapper = mock(GeographicWebMapper.class);
        MessageSource messageSource = mock(MessageSource.class);
        GeographicLookupProvider lookupProvider = mock(GeographicLookupProvider.class);

        GeographicController controller = new GeographicController(
                createUseCase, updateUseCase, deleteUseCase,
                findUseCase, getEditViewUseCase, webMapper, messageSource, lookupProvider);

        Geographic parentGeo = Geographic.createNew("ID", "Indonesia", GeographicType.COUNTRY,
                null, null, Boolean.TRUE);
        when(getEditViewUseCase.execute(1L)).thenReturn(Optional.of(parentGeo));
        when(lookupProvider.resolve(1L)).thenReturn(new LookupDto(1L, "Indonesia", "ID"));

        Model model = new ExtendedModelMap();
        String view = controller.showCreateForm(1L, model);

        assertEquals("master/geographic/form", view);
        GeographicSaveRequest request = (GeographicSaveRequest) model.getAttribute("geographicRequest");
        assertNotNull(request);
        assertEquals(1L, request.getParentId());
        assertEquals("Indonesia", request.getParentName());
        assertEquals("ID", request.getParentSubtext());
        assertEquals(GeographicType.STATE_PROVINCE, request.getType());
    }

    @Test
    public void showEditFormShouldPopulateParentSubtext() {
        CreateGeographicUseCase createUseCase = mock(CreateGeographicUseCase.class);
        UpdateGeographicUseCase updateUseCase = mock(UpdateGeographicUseCase.class);
        DeleteGeographicUseCase deleteUseCase = mock(DeleteGeographicUseCase.class);
        FindGeographicsUseCase findUseCase = mock(FindGeographicsUseCase.class);
        GetGeographicEditViewUseCase getEditViewUseCase = mock(GetGeographicEditViewUseCase.class);
        GeographicWebMapper webMapper = mock(GeographicWebMapper.class);
        MessageSource messageSource = mock(MessageSource.class);
        GeographicLookupProvider lookupProvider = mock(GeographicLookupProvider.class);

        GeographicController controller = new GeographicController(
                createUseCase, updateUseCase, deleteUseCase,
                findUseCase, getEditViewUseCase, webMapper, messageSource, lookupProvider);

        Geographic parentGeo = Geographic.createNew("ID", "Indonesia", GeographicType.COUNTRY,
                null, null, Boolean.TRUE);
        Geographic childGeo = Geographic.createNew("JKT", "Jakarta", GeographicType.STATE_PROVINCE,
                1L, "Indonesia", Boolean.TRUE);
        
        GeographicSaveRequest saveRequest = new GeographicSaveRequest();
        saveRequest.setCode("JKT");
        saveRequest.setName("Jakarta");
        saveRequest.setType(GeographicType.STATE_PROVINCE);
        saveRequest.setParentId(1L);
        saveRequest.setParentName("Indonesia");
        
        when(getEditViewUseCase.execute(2L)).thenReturn(Optional.of(childGeo));
        when(webMapper.toSaveRequest(childGeo)).thenReturn(saveRequest);
        when(lookupProvider.resolve(1L)).thenReturn(new LookupDto(1L, "Indonesia", "ID"));

        Model model = new ExtendedModelMap();
        String view = controller.showEditForm(2L, model);

        assertEquals("master/geographic/form", view);
        GeographicSaveRequest request = (GeographicSaveRequest) model.getAttribute("geographicRequest");
        assertNotNull(request);
        assertEquals(1L, request.getParentId());
        assertEquals("Indonesia", request.getParentName());
        assertEquals("ID", request.getParentSubtext());
    }
}

