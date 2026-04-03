package com.solusi.erp.inventory.container.web.controller;

import com.solusi.erp.inventory.container.application.usecase.command.CreateContainerUseCase;
import com.solusi.erp.inventory.container.application.usecase.command.DeleteContainerUseCase;
import com.solusi.erp.inventory.container.application.usecase.command.UpdateContainerUseCase;
import com.solusi.erp.inventory.container.application.usecase.query.FindContainersUseCase;
import com.solusi.erp.inventory.container.application.usecase.query.GetContainerEditViewUseCase;
import com.solusi.erp.inventory.container.application.usecase.query.GetContainerLookupUseCase;
import com.solusi.erp.inventory.container.domain.model.Container;
import com.solusi.erp.inventory.container.web.dto.ContainerSummaryResponse;
import com.solusi.erp.inventory.container.web.mapper.ContainerWebMapper;
import com.solusi.erp.inventory.grid.application.usecase.query.GetGridEditViewUseCase;
import com.solusi.erp.inventory.grid.domain.port.GridLookupProvider;
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

public class ContainerControllerTest {

    @Test
    public void listShouldReturnListViewAndModel() {
        CreateContainerUseCase createUseCase = mock(CreateContainerUseCase.class);
        UpdateContainerUseCase updateUseCase = mock(UpdateContainerUseCase.class);
        DeleteContainerUseCase deleteUseCase = mock(DeleteContainerUseCase.class);
        FindContainersUseCase findUseCase = mock(FindContainersUseCase.class);
        GetContainerEditViewUseCase editViewUseCase = mock(GetContainerEditViewUseCase.class);
        GetContainerLookupUseCase lookupUseCase = mock(GetContainerLookupUseCase.class);
        ContainerWebMapper webMapper = mock(ContainerWebMapper.class);
        GetGridEditViewUseCase getGridEditViewUseCase = mock(GetGridEditViewUseCase.class);
        GridWebMapper gridWebMapper = mock(GridWebMapper.class);
        GridLookupProvider gridLookupProvider = mock(GridLookupProvider.class);
        MessageSource messageSource = mock(MessageSource.class);

        ContainerController controller = new ContainerController(
            createUseCase, updateUseCase, deleteUseCase,
            findUseCase, editViewUseCase, lookupUseCase,
            webMapper, getGridEditViewUseCase, gridWebMapper, gridLookupProvider, messageSource
        );

        Container domainContainer = Container.createNew(1L, "Bin A1", null, null, null, null, null, null, true);
        domainContainer.assignCode("CNT-001");
        com.solusi.erp.core.domain.model.Page<Container> domainPage =
            new com.solusi.erp.core.domain.model.Page<>(List.of(domainContainer), 0, 20, 1L);
        when(findUseCase.execute(any(), any(), any())).thenReturn(domainPage);

        ContainerSummaryResponse summary = new ContainerSummaryResponse();
        summary.setId(1L);
        summary.setCode("CNT-001");
        summary.setName("Bin A1");
        when(webMapper.toSummaryResponse(any(Container.class))).thenReturn(summary);

        org.springframework.data.domain.Pageable springPageable = PageRequest.of(0, 20);
        Model model = new ExtendedModelMap();

        String view = controller.list(null, null, springPageable, model);

        assertEquals("inventory/containers/list", view);
        Object pageObj = model.getAttribute("page");
        assertThat(pageObj).isInstanceOf(org.springframework.data.domain.Page.class);
        org.springframework.data.domain.Page<?> springPage = (org.springframework.data.domain.Page<?>) pageObj;
        assertEquals(1, springPage.getTotalElements());
    }
}
