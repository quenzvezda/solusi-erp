package com.solusi.erp.inventory.goodsreceipt.web.controller;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.dto.ApiResponse;
import com.solusi.erp.inventory.goodsreceipt.application.usecase.command.*;
import com.solusi.erp.inventory.goodsreceipt.application.usecase.query.*;
import com.solusi.erp.inventory.goodsreceipt.domain.model.GoodsReceipt;
import com.solusi.erp.inventory.goodsreceipt.domain.model.GoodsReceiptLine;
import com.solusi.erp.inventory.goodsreceipt.domain.model.GoodsReceiptStatus;
import com.solusi.erp.inventory.goodsreceipt.web.dto.*;
import com.solusi.erp.inventory.goodsreceipt.web.mapper.GoodsReceiptWebMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("GoodsReceiptController Tests")
public class GoodsReceiptControllerTest {

    private CreateGoodsReceiptUseCase createUc;
    private UpdateGoodsReceiptUseCase updateUc;
    private DeleteGoodsReceiptUseCase deleteUc;
    private CompleteGoodsReceiptUseCase completeUc;
    private FindGoodsReceiptsUseCase findUc;
    private GetGoodsReceiptUseCase getUc;
    private GetGoodsReceiptEditViewUseCase editViewUc;
    private GetGoodsReceiptCreateViewUseCase createViewUc;
    private GoodsReceiptWebMapper webMapper;
    private MessageSource messageSource;
    private GoodsReceiptController controller;

    @BeforeEach
    void setUp() {
        createUc = mock(CreateGoodsReceiptUseCase.class);
        updateUc = mock(UpdateGoodsReceiptUseCase.class);
        deleteUc = mock(DeleteGoodsReceiptUseCase.class);
        completeUc = mock(CompleteGoodsReceiptUseCase.class);
        findUc = mock(FindGoodsReceiptsUseCase.class);
        getUc = mock(GetGoodsReceiptUseCase.class);
        editViewUc = mock(GetGoodsReceiptEditViewUseCase.class);
        createViewUc = mock(GetGoodsReceiptCreateViewUseCase.class);
        webMapper = mock(GoodsReceiptWebMapper.class);
        messageSource = mock(MessageSource.class);

        controller = new GoodsReceiptController(
            createUc, updateUc, deleteUc, completeUc, findUc, getUc, editViewUc, createViewUc,
            webMapper, messageSource
        );
    }

    private GoodsReceipt buildDraftGr() {
        AuditMetadata metadata = new AuditMetadata(1L, 1L, null, null, null, null);
        GoodsReceiptLine line = GoodsReceiptLine.prefill(
            10L, 100L, 200L, false,
            new BigDecimal("5.0000"), 1L, 1L,
            new BigDecimal("100.00"), new BigDecimal("5.0000"),
            new BigDecimal("500.00"), BigDecimal.ZERO,
            new BigDecimal("500.00"), null
        );
        return new GoodsReceipt(
            metadata, "GR-001", LocalDate.of(2026, 7, 1),
            1L, 100L, 200L, 1L, BigDecimal.ONE,
            GoodsReceiptStatus.DRAFT, "Test note", List.of(line)
        );
    }

    @Test
    @DisplayName("list returns list view with page model")
    void listShouldReturnListViewAndModel() {
        GoodsReceipt gr = buildDraftGr();
        com.solusi.erp.core.domain.model.Page<GoodsReceipt> domainPage =
            new com.solusi.erp.core.domain.model.Page<>(List.of(gr), 0, 20, 1L);
        when(findUc.execute(any(), any())).thenReturn(domainPage);

        GoodsReceiptSummaryResponse summary = new GoodsReceiptSummaryResponse();
        summary.setId(1L);
        summary.setCode("GR-001");
        summary.setStatus("DRAFT");
        summary.setLineCount(1);
        when(webMapper.toSummaryResponse(any(GoodsReceipt.class))).thenReturn(summary);

        org.springframework.data.domain.Pageable springPageable =
            org.springframework.data.domain.PageRequest.of(0, 20);
        Model model = new ExtendedModelMap();

        String view = controller.list(null, springPageable, model);

        assertEquals("inventory/goods-receipts/list", view);
        Object pageObj = model.getAttribute("page");
        assertThat(pageObj).isInstanceOf(org.springframework.data.domain.Page.class);
        org.springframework.data.domain.Page<?> springPage = (org.springframework.data.domain.Page<?>) pageObj;
        assertEquals(1, springPage.getTotalElements());
    }

    @Test
    @DisplayName("createForm loads from use case and returns form view")
    void createFormShouldLoadFromUseCaseAndReturnFormView() {
        GoodsReceipt gr = buildDraftGr();
        when(createViewUc.execute(7L)).thenReturn(gr);

        GoodsReceiptSaveRequest request = new GoodsReceiptSaveRequest();
        request.setReceiptDate(LocalDate.of(2026, 7, 1));
        when(webMapper.toSaveRequest(any(GoodsReceipt.class))).thenReturn(request);

        Model model = new ExtendedModelMap();
        String view = controller.createForm(7L, model);

        assertEquals("inventory/goods-receipts/form", view);
        assertThat(model.getAttribute("grRequest")).isNotNull()
            .isInstanceOf(GoodsReceiptSaveRequest.class);
        verify(createViewUc).execute(7L);
    }

    @Test
    @DisplayName("view loads detail response and returns view template")
    void viewShouldLoadDetailResponseAndReturnViewTemplate() {
        GoodsReceipt gr = buildDraftGr();
        when(getUc.execute(1L)).thenReturn(Optional.of(gr));

        GoodsReceiptDetailResponse detail = new GoodsReceiptDetailResponse();
        detail.setId(1L);
        detail.setCode("GR-001");
        when(webMapper.toDetailResponse(any(GoodsReceipt.class))).thenReturn(detail);

        Model model = new ExtendedModelMap();
        String view = controller.view(1L, model);

        assertEquals("inventory/goods-receipts/view", view);
        assertThat(model.getAttribute("gr")).isNotNull()
            .isInstanceOf(GoodsReceiptDetailResponse.class);
        verify(getUc).execute(1L);
    }

    @Test
    @DisplayName("editForm loads from use case and returns form view")
    void editFormShouldLoadFromUseCaseAndReturnFormView() {
        GoodsReceipt gr = buildDraftGr();
        when(editViewUc.execute(1L)).thenReturn(Optional.of(gr));

        GoodsReceiptSaveRequest request = new GoodsReceiptSaveRequest();
        request.setId(1L);
        request.setReceiptDate(LocalDate.of(2026, 7, 1));
        when(webMapper.toSaveRequest(any(GoodsReceipt.class))).thenReturn(request);

        Model model = new ExtendedModelMap();
        String view = controller.editForm(1L, model);

        assertEquals("inventory/goods-receipts/form", view);
        assertThat(model.getAttribute("grRequest")).isNotNull()
            .isInstanceOf(GoodsReceiptSaveRequest.class);
        verify(editViewUc).execute(1L);
    }

    @Test
    @DisplayName("save creates new GR when id is null")
    void saveShouldCreateNewGrWhenIdIsNull() {
        GoodsReceipt gr = buildDraftGr();
        when(createUc.execute(any(), any(), any(), anyList())).thenReturn(gr);

        GoodsReceiptDetailResponse detail = new GoodsReceiptDetailResponse();
        detail.setId(1L);
        detail.setCode("GR-001");
        when(webMapper.toDetailResponse(any(GoodsReceipt.class))).thenReturn(detail);
        when(webMapper.toLineCommands(anyList())).thenReturn(List.of());
        when(messageSource.getMessage(any(), any(), any())).thenReturn("Created");

        GoodsReceiptSaveRequest request = new GoodsReceiptSaveRequest();
        request.setReceiptDate(LocalDate.of(2026, 7, 1));
        request.setNotes("Test note");
        request.setLines(List.of());

        ResponseEntity<ApiResponse<GoodsReceiptDetailResponse>> response = controller.save(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        verify(createUc).execute(any(), any(), any(), anyList());
    }

    @Test
    @DisplayName("save updates existing GR when id is present")
    void saveShouldUpdateGrWhenIdIsPresent() {
        GoodsReceipt gr = buildDraftGr();
        when(updateUc.execute(any(), any(), any(), anyList())).thenReturn(gr);

        GoodsReceiptDetailResponse detail = new GoodsReceiptDetailResponse();
        detail.setId(1L);
        detail.setCode("GR-001");
        when(webMapper.toDetailResponse(any(GoodsReceipt.class))).thenReturn(detail);
        when(webMapper.toLineCommands(anyList())).thenReturn(List.of());
        when(messageSource.getMessage(any(), any(), any())).thenReturn("Updated");

        GoodsReceiptSaveRequest request = new GoodsReceiptSaveRequest();
        request.setId(1L);
        request.setReceiptDate(LocalDate.of(2026, 7, 2));
        request.setNotes("Updated note");
        request.setLines(List.of());

        ResponseEntity<ApiResponse<GoodsReceiptDetailResponse>> response = controller.save(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        verify(updateUc).execute(eq(1L), any(), any(), anyList());
    }

    @Test
    @DisplayName("complete calls use case and redirects to view")
    void completeShouldCallUseCaseAndRedirect() {
        when(messageSource.getMessage(any(), any(), any())).thenReturn("Completed");

        RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);
        String view = controller.complete(1L, redirectAttributes);

        assertEquals("redirect:/inventory/goods-receipts/1", view);
        verify(completeUc).execute(1L);
    }

    @Test
    @DisplayName("delete calls use case and returns response")
    void deleteShouldCallUseCase() {
        when(messageSource.getMessage(any(), any(), any())).thenReturn("Deleted");

        ResponseEntity<Void> response = controller.delete(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(deleteUc).execute(1L);
    }
}
