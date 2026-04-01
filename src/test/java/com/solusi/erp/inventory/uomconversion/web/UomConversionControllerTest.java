package com.solusi.erp.inventory.uomconversion.web;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.dto.ApiResponse;
import com.solusi.erp.inventory.uom.domain.model.UomType;
import com.solusi.erp.inventory.uom.application.usecase.query.GetUomLookupUseCase;
import com.solusi.erp.inventory.product.domain.port.ProductLookupProvider;
import com.solusi.erp.inventory.uomconversion.application.usecase.command.*;
import com.solusi.erp.inventory.uomconversion.application.usecase.query.*;
import com.solusi.erp.inventory.uomconversion.domain.model.UomConversion;
import com.solusi.erp.inventory.uomconversion.web.controller.UomConversionController;
import com.solusi.erp.inventory.uomconversion.web.dto.*;
import com.solusi.erp.inventory.uomconversion.web.mapper.UomConversionWebMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class UomConversionControllerTest {

    CreateUomConversionUseCase createUseCase;
    UpdateUomConversionUseCase updateUseCase;
    DeleteUomConversionUseCase deleteUseCase;
    FindUomConversionsUseCase findUseCase;
    GetUomConversionEditViewUseCase editViewUseCase;
    GetUomLookupUseCase getUomLookupUseCase;
    UomConversionWebMapper webMapper;
    ProductLookupProvider productLookupProvider;
    MessageSource messageSource;
    UomConversionController controller;

    @BeforeEach
    void setUp() {
        createUseCase = mock(CreateUomConversionUseCase.class);
        updateUseCase = mock(UpdateUomConversionUseCase.class);
        deleteUseCase = mock(DeleteUomConversionUseCase.class);
        findUseCase = mock(FindUomConversionsUseCase.class);
        editViewUseCase = mock(GetUomConversionEditViewUseCase.class);
        getUomLookupUseCase = mock(GetUomLookupUseCase.class);
        webMapper = mock(UomConversionWebMapper.class);
        productLookupProvider = mock(ProductLookupProvider.class);
        messageSource = mock(MessageSource.class);

        controller = new UomConversionController(
            createUseCase, updateUseCase, deleteUseCase,
            findUseCase, editViewUseCase, getUomLookupUseCase,
            webMapper, productLookupProvider, messageSource
        );
    }

    private UomConversion makeDomain() {
        return new UomConversion(
            new AuditMetadata(1L, 1L, null, null, null, null),
            10L, "PRD-001", "Product 1",
            20L, "Box", 30L, "Pieces", new BigDecimal("12.00"));
    }

    @Test
    void list_shouldReturnListViewAndPage() {
        UomConversion domain = makeDomain();
        com.solusi.erp.core.domain.model.Page<UomConversion> domainPage =
            new com.solusi.erp.core.domain.model.Page<>(List.of(domain), 0, 20, 1L);
        when(findUseCase.execute(any(), any())).thenReturn(domainPage);

        UomConversionSummaryResponse summary = new UomConversionSummaryResponse();
        summary.setId(1L);
        summary.setProductName("Product 1");
        when(webMapper.toSummaryResponse(any())).thenReturn(summary);

        org.springframework.data.domain.Pageable springPageable =
            org.springframework.data.domain.PageRequest.of(0, 20);
        Model model = new ExtendedModelMap();

        String view = controller.list(null, springPageable, model);

        assertEquals("inventory/uom-conversions/list", view);
        assertThat(model.getAttribute("page")).isInstanceOf(org.springframework.data.domain.Page.class);
        org.springframework.data.domain.Page<?> page =
            (org.springframework.data.domain.Page<?>) model.getAttribute("page");
        assertThat(page.getTotalElements()).isEqualTo(1L);
    }

    @Test
    void showCreateForm_shouldReturnFormWithEmptyRequest() {
        when(getUomLookupUseCase.findByType(UomType.UNIT)).thenReturn(List.of());
        Model model = new ExtendedModelMap();

        String view = controller.showCreateForm(model);

        assertEquals("inventory/uom-conversions/form", view);
        assertThat(model.getAttribute("uomConversionRequest")).isInstanceOf(UomConversionSaveRequest.class);
        assertThat(((UomConversionSaveRequest) model.getAttribute("uomConversionRequest")).getId()).isNull();
    }

    @Test
    void showEditForm_shouldPopulateModel() {
        UomConversion domain = makeDomain();
        when(editViewUseCase.execute(1L)).thenReturn(Optional.of(domain));
        when(getUomLookupUseCase.findByType(UomType.UNIT)).thenReturn(List.of());

        UomConversionSaveRequest req = new UomConversionSaveRequest();
        req.setId(1L);
        req.setFromUomId(20L);
        when(webMapper.toSaveRequest(domain)).thenReturn(req);

        UomConversionDetailResponse detail = new UomConversionDetailResponse();
        detail.setId(1L);
        when(webMapper.toDetailResponse(domain)).thenReturn(detail);

        Model model = new ExtendedModelMap();
        String view = controller.showEditForm(1L, model);

        assertEquals("inventory/uom-conversions/form", view);
        assertThat(model.getAttribute("uomConversionRequest")).isNotNull();
        assertThat(model.getAttribute("uomUIForm")).isInstanceOf(UomConversionUIInfo.class);
        assertThat(model.getAttribute("auditInfo")).isNotNull();

        UomConversionUIInfo uiInfo = (UomConversionUIInfo) model.getAttribute("uomUIForm");
        assertThat(uiInfo.getProductName()).isEqualTo("Product 1");
        assertThat(uiInfo.getProductCode()).isEqualTo("PRD-001");
        assertThat(uiInfo.getToUomName()).isEqualTo("Pieces");
    }

    @Test
    void create_shouldReturn201WithDetailResponse() {
        UomConversion domain = makeDomain();
        when(createUseCase.execute(anyLong(), anyLong(), any())).thenReturn(domain);

        UomConversionDetailResponse detail = new UomConversionDetailResponse();
        detail.setId(1L);
        when(webMapper.toDetailResponse(domain)).thenReturn(detail);
        when(messageSource.getMessage(eq("msg.success.create"), any(), any())).thenReturn("Created");

        UomConversionSaveRequest request = new UomConversionSaveRequest();
        request.setProductId(10L);
        request.setFromUomId(20L);
        request.setConversionFactor(new BigDecimal("12.00"));

        var response = controller.create(request);

        assertThat(response.getStatusCode().value()).isEqualTo(201);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getData().getId()).isEqualTo(1L);
    }

    @Test
    void update_shouldReturn200WithDetailResponse() {
        UomConversion domain = makeDomain();
        when(updateUseCase.execute(anyLong(), anyLong(), any())).thenReturn(domain);

        UomConversionDetailResponse detail = new UomConversionDetailResponse();
        detail.setId(1L);
        when(webMapper.toDetailResponse(domain)).thenReturn(detail);
        when(messageSource.getMessage(eq("msg.success.update"), any(), any())).thenReturn("Updated");

        UomConversionSaveRequest request = new UomConversionSaveRequest();
        request.setFromUomId(40L);
        request.setConversionFactor(new BigDecimal("24.00"));

        var response = controller.update(1L, request);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody().isSuccess()).isTrue();
    }

    @Test
    void delete_shouldReturn200WithHtmxTrigger() {
        doNothing().when(deleteUseCase).execute(1L);
        when(messageSource.getMessage(eq("msg.success.delete"), any(), any())).thenReturn("Deleted");

        var response = controller.delete(1L);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getHeaders().getFirst("HX-Trigger")).contains("refresh-table");
        verify(deleteUseCase).execute(1L);
    }
}
