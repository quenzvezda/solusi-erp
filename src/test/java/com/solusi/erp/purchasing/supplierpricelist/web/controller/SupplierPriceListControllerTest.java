package com.solusi.erp.purchasing.supplierpricelist.web.controller;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.purchasing.supplierpricelist.application.usecase.command.*;
import com.solusi.erp.purchasing.supplierpricelist.application.usecase.query.*;
import com.solusi.erp.purchasing.supplierpricelist.domain.model.SupplierPriceList;
import com.solusi.erp.purchasing.supplierpricelist.web.dto.SupplierPriceListSummaryResponse;
import com.solusi.erp.purchasing.supplierpricelist.web.mapper.SupplierPriceListWebMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("SupplierPriceListController Tests")
public class SupplierPriceListControllerTest {

    @Test
    @DisplayName("list returns list view with page model")
    public void listShouldReturnListViewAndModel() {
        CreateSupplierPriceListUseCase createUc = mock(CreateSupplierPriceListUseCase.class);
        UpdateSupplierPriceListUseCase updateUc = mock(UpdateSupplierPriceListUseCase.class);
        DeleteSupplierPriceListUseCase deleteUc = mock(DeleteSupplierPriceListUseCase.class);
        FindSupplierPriceListsUseCase findUc = mock(FindSupplierPriceListsUseCase.class);
        GetSupplierPriceListEditViewUseCase editViewUc = mock(GetSupplierPriceListEditViewUseCase.class);
        SupplierPriceListWebMapper webMapper = mock(SupplierPriceListWebMapper.class);
        MessageSource messageSource = mock(MessageSource.class);

        SupplierPriceListController controller = new SupplierPriceListController(
            createUc, updateUc, deleteUc, findUc, editViewUc, webMapper, messageSource
        );

        AuditMetadata metadata = new AuditMetadata(1L, 1L, null, null, null, null);
        SupplierPriceList domainSpl = new SupplierPriceList(metadata, "SPL-001",
            1L, 2L, 3L, 4L,
            new BigDecimal("100.0000"), new BigDecimal("1.0000"),
            LocalDate.of(2026, 7, 1), null, null, true);

        com.solusi.erp.core.domain.model.Page<SupplierPriceList> domainPage =
            new com.solusi.erp.core.domain.model.Page<>(List.of(domainSpl), 0, 20, 1L);
        when(findUc.execute(any(), any())).thenReturn(domainPage);

        SupplierPriceListSummaryResponse summary = new SupplierPriceListSummaryResponse();
        summary.setId(1L);
        summary.setCode("SPL-001");
        when(webMapper.toSummaryResponse(any(SupplierPriceList.class))).thenReturn(summary);

        org.springframework.data.domain.Pageable springPageable =
            org.springframework.data.domain.PageRequest.of(0, 20);
        Model model = new ExtendedModelMap();

        String view = controller.list(null, springPageable, model);

        assertEquals("purchasing/supplier-price-lists/list", view);
        Object pageObj = model.getAttribute("page");
        assertThat(pageObj).isInstanceOf(org.springframework.data.domain.Page.class);
        org.springframework.data.domain.Page<?> springPage = (org.springframework.data.domain.Page<?>) pageObj;
        assertEquals(1, springPage.getTotalElements());
        Object first = springPage.getContent().get(0);
        assertThat(first).isInstanceOf(SupplierPriceListSummaryResponse.class);
        assertEquals("SPL-001", ((SupplierPriceListSummaryResponse) first).getCode());
    }

    @Test
    @DisplayName("showCreateForm returns form view with empty request")
    public void showCreateFormShouldReturnFormView() {
        CreateSupplierPriceListUseCase createUc = mock(CreateSupplierPriceListUseCase.class);
        UpdateSupplierPriceListUseCase updateUc = mock(UpdateSupplierPriceListUseCase.class);
        DeleteSupplierPriceListUseCase deleteUc = mock(DeleteSupplierPriceListUseCase.class);
        FindSupplierPriceListsUseCase findUc = mock(FindSupplierPriceListsUseCase.class);
        GetSupplierPriceListEditViewUseCase editViewUc = mock(GetSupplierPriceListEditViewUseCase.class);
        SupplierPriceListWebMapper webMapper = mock(SupplierPriceListWebMapper.class);
        MessageSource messageSource = mock(MessageSource.class);

        SupplierPriceListController controller = new SupplierPriceListController(
            createUc, updateUc, deleteUc, findUc, editViewUc, webMapper, messageSource
        );

        Model model = new ExtendedModelMap();
        String view = controller.showCreateForm(model);

        assertEquals("purchasing/supplier-price-lists/form", view);
        assertThat(model.getAttribute("splRequest")).isNotNull();
    }
}
