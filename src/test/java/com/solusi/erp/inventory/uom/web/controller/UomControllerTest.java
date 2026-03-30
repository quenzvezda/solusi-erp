package com.solusi.erp.inventory.uom.web.controller;

import com.solusi.erp.inventory.uom.domain.model.UomType;
import com.solusi.erp.inventory.uom.application.usecase.command.CreateUomUseCase;
import com.solusi.erp.inventory.uom.application.usecase.command.DeleteUomUseCase;
import com.solusi.erp.inventory.uom.application.usecase.command.UpdateUomUseCase;
import com.solusi.erp.inventory.uom.application.usecase.query.FindUomsUseCase;
import com.solusi.erp.inventory.uom.application.usecase.query.GetUomEditViewUseCase;
import com.solusi.erp.inventory.uom.application.usecase.query.GetUomLookupUseCase;
import com.solusi.erp.inventory.uom.domain.model.UnitOfMeasure;
import com.solusi.erp.inventory.uom.web.dto.UomSummaryResponse;
import com.solusi.erp.inventory.uom.web.mapper.UomWebMapper;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class UomControllerTest {

    @Test
    public void listShouldReturnListViewAndModel() {
        CreateUomUseCase createUomUseCase = mock(CreateUomUseCase.class);
        UpdateUomUseCase updateUomUseCase = mock(UpdateUomUseCase.class);
        DeleteUomUseCase deleteUomUseCase = mock(DeleteUomUseCase.class);
        FindUomsUseCase findUomsUseCase = mock(FindUomsUseCase.class);
        GetUomEditViewUseCase getUomEditViewUseCase = mock(GetUomEditViewUseCase.class);
        GetUomLookupUseCase getUomLookupUseCase = mock(GetUomLookupUseCase.class);
        UomWebMapper webMapper = mock(UomWebMapper.class);
        MessageSource messageSource = mock(MessageSource.class);

        UomController controller = new UomController(
            createUomUseCase, updateUomUseCase, deleteUomUseCase,
            findUomsUseCase, getUomEditViewUseCase, getUomLookupUseCase,
            webMapper, messageSource
        );

        UnitOfMeasure domainUom = UnitOfMeasure.createNew("KG", "Kilogram", UomType.WEIGHT);
        com.solusi.erp.core.domain.model.Page<UnitOfMeasure> domainPage =
            new com.solusi.erp.core.domain.model.Page<>(List.of(domainUom), 0, 1, 1L);
        when(findUomsUseCase.execute(any(), any())).thenReturn(domainPage);

        UomSummaryResponse summary = new UomSummaryResponse();
        summary.setId(1L);
        summary.setCode("KG");
        summary.setName("Kilogram");
        summary.setType(UomType.WEIGHT);
        when(webMapper.toSummaryResponse(any(UnitOfMeasure.class))).thenReturn(summary);

        org.springframework.data.domain.Pageable springPageable = org.springframework.data.domain.PageRequest.of(0, 20);
        Model model = new ExtendedModelMap();

        String view = controller.list(null, springPageable, model);

        assertEquals("inventory/unit-of-measures/list", view);
        Object pageObj = model.getAttribute("page");
        assertThat(pageObj).isInstanceOf(org.springframework.data.domain.Page.class);
        org.springframework.data.domain.Page<?> springPage = (org.springframework.data.domain.Page<?>) pageObj;
        assertEquals(1, springPage.getTotalElements());
        Object first = springPage.getContent().get(0);
        assertThat(first).isInstanceOf(UomSummaryResponse.class);
        assertEquals("KG", ((UomSummaryResponse) first).getCode());
    }
}
