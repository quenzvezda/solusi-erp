package com.solusi.erp.inventory.goodsissue.web.controller;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.inventory.goodsissue.application.usecase.command.CancelGoodsIssueUseCase;
import com.solusi.erp.inventory.goodsissue.application.usecase.command.CompleteGoodsIssueUseCase;
import com.solusi.erp.inventory.goodsissue.application.usecase.command.CreateGoodsIssueUseCase;
import com.solusi.erp.inventory.goodsissue.application.usecase.command.DeleteGoodsIssueUseCase;
import com.solusi.erp.inventory.goodsissue.application.usecase.command.UpdateGoodsIssueUseCase;
import com.solusi.erp.inventory.goodsissue.application.usecase.query.FindGoodsIssuesUseCase;
import com.solusi.erp.inventory.goodsissue.application.usecase.query.GetGoodsIssueCreateViewUseCase;
import com.solusi.erp.inventory.goodsissue.application.usecase.query.GetGoodsIssueEditViewUseCase;
import com.solusi.erp.inventory.goodsissue.application.usecase.query.GetGoodsIssueUseCase;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssue;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssuePartyType;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssueReferenceType;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssueStatus;
import com.solusi.erp.inventory.goodsissue.web.dto.GoodsIssueDetailResponse;
import com.solusi.erp.inventory.goodsissue.web.dto.GoodsIssueSaveRequest;
import com.solusi.erp.inventory.goodsissue.web.dto.GoodsIssueSummaryResponse;
import com.solusi.erp.inventory.goodsissue.web.mapper.GoodsIssueWebMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

class GoodsIssueControllerTest {

    private CreateGoodsIssueUseCase createUseCase;
    private UpdateGoodsIssueUseCase updateUseCase;
    private DeleteGoodsIssueUseCase deleteUseCase;
    private CompleteGoodsIssueUseCase completeUseCase;
    private CancelGoodsIssueUseCase cancelUseCase;
    private FindGoodsIssuesUseCase findUseCase;
    private GetGoodsIssueUseCase getUseCase;
    private GetGoodsIssueEditViewUseCase editViewUseCase;
    private GetGoodsIssueCreateViewUseCase createViewUseCase;
    private GoodsIssueWebMapper webMapper;
    private MessageSource messageSource;
    private GoodsIssueController controller;

    @BeforeEach
    void setUp() {
        createUseCase = mock(CreateGoodsIssueUseCase.class);
        updateUseCase = mock(UpdateGoodsIssueUseCase.class);
        deleteUseCase = mock(DeleteGoodsIssueUseCase.class);
        completeUseCase = mock(CompleteGoodsIssueUseCase.class);
        cancelUseCase = mock(CancelGoodsIssueUseCase.class);
        findUseCase = mock(FindGoodsIssuesUseCase.class);
        getUseCase = mock(GetGoodsIssueUseCase.class);
        editViewUseCase = mock(GetGoodsIssueEditViewUseCase.class);
        createViewUseCase = mock(GetGoodsIssueCreateViewUseCase.class);
        webMapper = mock(GoodsIssueWebMapper.class);
        messageSource = mock(MessageSource.class);
        controller = new GoodsIssueController(
                createUseCase, updateUseCase, deleteUseCase, completeUseCase, cancelUseCase,
                findUseCase, getUseCase, editViewUseCase, createViewUseCase, webMapper, messageSource);
    }

    @Test
    void list_returnsListViewAndReferenceFilterModel() throws Exception {
        GoodsIssue issue = issue();
        when(findUseCase.execute(any(), any(), any(), any())).thenReturn(new Page<>(List.of(issue), 0, 20, 1));
        GoodsIssueSummaryResponse summary = new GoodsIssueSummaryResponse();
        summary.setId(7L);
        summary.setCode("GI-202606-00001");
        summary.setReferenceCode("PRTN-0070");
        when(webMapper.toSummaryResponse(issue)).thenReturn(summary);

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();

        mockMvc.perform(get("/inventory/goods-issues")
                        .param("referenceType", "PURCHASE_RETURN")
                        .param("referenceId", "70"))
                .andExpect(status().isOk())
                .andExpect(view().name("inventory/goods-issues/list"))
                .andExpect(model().attribute("activeReferenceType", "PURCHASE_RETURN"))
                .andExpect(model().attribute("activeReferenceId", 70L))
                .andExpect(model().attribute("activeReferenceCode", "PRTN-0070"));
    }

    @Test
    void createAndEditAndView_returnExpectedTemplates() {
        GoodsIssue issue = issue();
        GoodsIssueSaveRequest saveRequest = new GoodsIssueSaveRequest();
        GoodsIssueDetailResponse detail = new GoodsIssueDetailResponse();
        when(createViewUseCase.execute(GoodsIssueReferenceType.PURCHASE_RETURN, 70L)).thenReturn(issue);
        when(editViewUseCase.execute(7L)).thenReturn(Optional.of(issue));
        when(getUseCase.execute(7L)).thenReturn(Optional.of(issue));
        when(webMapper.toSaveRequest(issue)).thenReturn(saveRequest);
        when(webMapper.toDetailResponse(issue)).thenReturn(detail);

        Model model = new ExtendedModelMap();
        assertThat(controller.createForm(GoodsIssueReferenceType.PURCHASE_RETURN, 70L, model))
                .isEqualTo("inventory/goods-issues/form");
        assertThat(model.getAttribute("giRequest")).isSameAs(saveRequest);

        model = new ExtendedModelMap();
        assertThat(controller.editForm(7L, model)).isEqualTo("inventory/goods-issues/form");
        assertThat(model.getAttribute("giRequest")).isSameAs(saveRequest);

        model = new ExtendedModelMap();
        assertThat(controller.view(7L, model)).isEqualTo("inventory/goods-issues/view");
        assertThat(model.getAttribute("gi")).isSameAs(detail);
    }

    @Test
    void saveCreateAndActions_returnJsonResponses() {
        GoodsIssue issue = issue();
        GoodsIssueSaveRequest request = new GoodsIssueSaveRequest();
        request.setIssueDate(LocalDate.of(2026, 6, 1));
        GoodsIssueDetailResponse detail = new GoodsIssueDetailResponse();
        when(createUseCase.execute(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(issue);
        when(getUseCase.execute(7L)).thenReturn(Optional.of(issue));
        when(webMapper.toLineCommands(any())).thenReturn(List.of());
        when(webMapper.toDetailResponse(issue)).thenReturn(detail);
        when(messageSource.getMessage(any(), any(), any(Locale.class))).thenReturn("ok");

        ResponseEntity<?> createResponse = controller.save(request);
        assertThat(createResponse.getStatusCode().value()).isEqualTo(201);

        ResponseEntity<?> completeResponse = controller.complete(7L);
        assertThat(completeResponse.getStatusCode().is2xxSuccessful()).isTrue();
        verify(completeUseCase).execute(7L);

        ResponseEntity<?> cancelResponse = controller.cancel(7L, "reason");
        assertThat(cancelResponse.getStatusCode().is2xxSuccessful()).isTrue();
        verify(cancelUseCase).execute(7L, "reason");
    }

    @Test
    void preAuthorizeValues_areExactAuthorities() throws Exception {
        assertPreAuthorize("list", "hasAuthority('GOODS-ISSUE_READ')", String.class,
                GoodsIssueReferenceType.class, Long.class, org.springframework.data.domain.Pageable.class, Model.class);
        assertPreAuthorize("createForm", "hasAuthority('GOODS-ISSUE_CREATE')",
                GoodsIssueReferenceType.class, Long.class, Model.class);
        assertPreAuthorize("save", "hasAnyAuthority('GOODS-ISSUE_CREATE', 'GOODS-ISSUE_UPDATE')",
                GoodsIssueSaveRequest.class);
        assertPreAuthorize("complete", "hasAuthority('GOODS-ISSUE_COMPLETE')", Long.class);
        assertPreAuthorize("cancel", "hasAuthority('GOODS-ISSUE_CANCEL')", Long.class, String.class);
        assertPreAuthorize("delete", "hasAuthority('GOODS-ISSUE_DELETE')", Long.class);
    }

    private void assertPreAuthorize(String methodName, String expected, Class<?>... parameterTypes) throws Exception {
        Method method = GoodsIssueController.class.getMethod(methodName, parameterTypes);
        org.springframework.security.access.prepost.PreAuthorize annotation =
                method.getAnnotation(org.springframework.security.access.prepost.PreAuthorize.class);
        assertThat(annotation.value()).isEqualTo(expected);
    }

    private static GoodsIssue issue() {
        return new GoodsIssue(
                new AuditMetadata(7L, 1L, null, null, null, null),
                "GI-202606-00001",
                LocalDate.of(2026, 6, 1),
                GoodsIssueReferenceType.PURCHASE_RETURN,
                70L,
                "PRTN-0070",
                11L,
                GoodsIssuePartyType.SUPPLIER,
                3L,
                1L,
                BigDecimal.ONE,
                GoodsIssueStatus.DRAFT,
                "note",
                List.of()
        );
    }
}
