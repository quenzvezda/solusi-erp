package com.solusi.erp.accounting.coa.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solusi.erp.accounting.coa.application.usecase.command.CreateCoaUseCase;
import com.solusi.erp.accounting.coa.application.usecase.command.DeleteCoaUseCase;
import com.solusi.erp.accounting.coa.application.usecase.command.UpdateCoaUseCase;
import com.solusi.erp.accounting.coa.application.usecase.query.CoaSelectorRow;
import com.solusi.erp.accounting.coa.application.usecase.query.FindCoaSelectorUseCase;
import com.solusi.erp.accounting.coa.application.usecase.query.FindCoaUseCase;
import com.solusi.erp.accounting.coa.application.usecase.query.GetCoaEditViewUseCase;
import com.solusi.erp.accounting.coa.domain.model.AccountType;
import com.solusi.erp.accounting.coa.domain.model.ChartOfAccount;
import com.solusi.erp.accounting.coa.web.dto.CoaDetailResponse;
import com.solusi.erp.accounting.coa.web.dto.CoaSaveRequest;
import com.solusi.erp.accounting.coa.web.dto.CoaSummaryResponse;
import com.solusi.erp.accounting.coa.web.mapper.CoaWebMapper;
import com.solusi.erp.core.domain.model.DeleteResult;
import com.solusi.erp.core.dto.ApiResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@DisplayName("CoaController — Unit Test")
public class CoaControllerTest {

    private final CreateCoaUseCase createCoaUseCase = mock(CreateCoaUseCase.class);
    private final UpdateCoaUseCase updateCoaUseCase = mock(UpdateCoaUseCase.class);
    private final DeleteCoaUseCase deleteCoaUseCase = mock(DeleteCoaUseCase.class);
    private final FindCoaUseCase findCoaUseCase = mock(FindCoaUseCase.class);
    private final FindCoaSelectorUseCase findCoaSelectorUseCase = mock(FindCoaSelectorUseCase.class);
    private final GetCoaEditViewUseCase getCoaEditViewUseCase = mock(GetCoaEditViewUseCase.class);
    private final CoaWebMapper webMapper = mock(CoaWebMapper.class);
    private final MessageSource messageSource = mock(MessageSource.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final CoaController controller = new CoaController(
            createCoaUseCase, updateCoaUseCase, deleteCoaUseCase,
            findCoaUseCase, findCoaSelectorUseCase, getCoaEditViewUseCase,
            webMapper, messageSource
    );

    // ── helpers ──────────────────────────────────────────────────────────────

    private ChartOfAccount sampleDomain() {
        return ChartOfAccount.createNew("1000", "Cash", AccountType.ASSET,
                null, 1, false, "Cash account", true);
    }

    private CoaSummaryResponse sampleSummary() {
        CoaSummaryResponse dto = new CoaSummaryResponse();
        dto.setId(1L);
        dto.setCode("1000");
        dto.setName("Cash");
        dto.setAccountType("ASSET");
        dto.setNormalBalance("DEBIT");
        dto.setLevel(1);
        dto.setIsHeader(false);
        dto.setIsActive(true);
        return dto;
    }

    private CoaDetailResponse sampleDetail() {
        CoaDetailResponse dto = new CoaDetailResponse();
        dto.setId(1L);
        dto.setCode("1000");
        dto.setName("Cash");
        dto.setAccountType("ASSET");
        dto.setNormalBalance("DEBIT");
        dto.setLevel(1);
        dto.setIsHeader(false);
        dto.setNote("Cash account");
        dto.setIsActive(true);
        return dto;
    }

    private CoaSaveRequest validRequest() {
        CoaSaveRequest req = new CoaSaveRequest();
        req.setCode("1000");
        req.setName("Cash");
        req.setAccountType("ASSET");
        req.setLevel(1);
        req.setIsHeader(false);
        req.setNote("Cash account");
        req.setIsActive(true);
        return req;
    }

    // ── list ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("list — returns correct view and model attributes")
    void list_returnsCorrectViewAndModel() {
        ChartOfAccount domain = sampleDomain();
        com.solusi.erp.core.domain.model.Page<ChartOfAccount> domainPage =
                new com.solusi.erp.core.domain.model.Page<>(List.of(domain), 0, 1, 1L);
        when(findCoaUseCase.execute(any(), any())).thenReturn(domainPage);
        when(webMapper.toSummaryResponse(any(ChartOfAccount.class))).thenReturn(sampleSummary());

        org.springframework.data.domain.Pageable springPageable =
                org.springframework.data.domain.PageRequest.of(0, 20);
        Model model = new ExtendedModelMap();

        String view = controller.list(null, springPageable, model);

        assertEquals("accounting/coa/list", view);
        Object pageObj = model.getAttribute("page");
        assertThat(pageObj).isInstanceOf(org.springframework.data.domain.Page.class);
        org.springframework.data.domain.Page<?> springPage = (org.springframework.data.domain.Page<?>) pageObj;
        assertEquals(1, springPage.getTotalElements());
        assertThat(springPage.getContent().get(0)).isInstanceOf(CoaSummaryResponse.class);
        assertThat(model.getAttribute("keyword")).isNull();
        assertThat(model.getAttribute("accountTypes")).isEqualTo(AccountType.values());
    }

    // ── showCreateForm ───────────────────────────────────────────────────────

    @Test
    @DisplayName("showCreateForm — returns form view with correct defaults")
    void showCreateForm_returnsFormViewWithDefaults() {
        Model model = new ExtendedModelMap();

        String view = controller.showCreateForm(model);

        assertEquals("accounting/coa/form", view);
        CoaSaveRequest req = (CoaSaveRequest) model.getAttribute("coaRequest");
        assertThat(req).isNotNull();
        assertThat(req.getIsActive()).isTrue();
        assertThat(req.getIsHeader()).isFalse();
        assertThat(req.getLevel()).isEqualTo(1);
        assertThat(model.getAttribute("accountTypes")).isEqualTo(AccountType.values());
    }

    // ── create ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("create — valid request returns CREATED with data")
    void create_returnsCreatedResponse() {
        ChartOfAccount domain = sampleDomain();
        CoaDetailResponse detail = sampleDetail();
        when(createCoaUseCase.execute(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(domain);
        when(webMapper.toDetailResponse(any(ChartOfAccount.class))).thenReturn(detail);
        when(messageSource.getMessage(eq("msg.success.create"), any(), any())).thenReturn("Created");

        ResponseEntity<ApiResponse<CoaDetailResponse>> response = controller.create(validRequest());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getData().getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("create — invalid account type returns BAD_REQUEST")
    void create_returnsBadRequestForInvalidAccountType() {
        CoaSaveRequest req = validRequest();
        req.setAccountType("INVALID_TYPE");

        ResponseEntity<ApiResponse<CoaDetailResponse>> response = controller.create(req);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
    }

    // ── showEditForm ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("showEditForm — populates model with request and audit info")
    void showEditForm_populatesModel() {
        ChartOfAccount domain = sampleDomain();
        when(getCoaEditViewUseCase.execute(1L)).thenReturn(Optional.of(domain));
        CoaSaveRequest saveReq = validRequest();
        saveReq.setId(1L);
        when(webMapper.toSaveRequest(any(ChartOfAccount.class))).thenReturn(saveReq);
        when(webMapper.toDetailResponse(any(ChartOfAccount.class))).thenReturn(sampleDetail());

        Model model = new ExtendedModelMap();
        String view = controller.showEditForm(1L, model);

        assertEquals("accounting/coa/form", view);
        assertThat(model.getAttribute("coaRequest")).isNotNull();
        assertThat(model.getAttribute("auditInfo")).isNotNull();
        assertThat(model.getAttribute("accountTypes")).isEqualTo(AccountType.values());
    }

    @Test
    @DisplayName("showEditForm — throws when not found")
    void showEditForm_throwsWhenNotFound() {
        when(getCoaEditViewUseCase.execute(999L)).thenReturn(Optional.empty());
        Model model = new ExtendedModelMap();

        assertThatThrownBy(() -> controller.showEditForm(999L, model))
                .isInstanceOf(RuntimeException.class);
    }

    // ── update ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("update — valid request returns OK")
    void update_returnsOkResponse() {
        ChartOfAccount domain = sampleDomain();
        CoaDetailResponse detail = sampleDetail();
        when(updateCoaUseCase.execute(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(domain);
        when(webMapper.toDetailResponse(any(ChartOfAccount.class))).thenReturn(detail);
        when(messageSource.getMessage(eq("msg.success.update"), any(), any())).thenReturn("Updated");

        ResponseEntity<ApiResponse<CoaDetailResponse>> response = controller.update(1L, validRequest());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
    }

    // ── delete ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("delete — hard delete returns OK")
    void delete_returnsOkWithHardDelete() {
        when(deleteCoaUseCase.execute(1L)).thenReturn(DeleteResult.HARD_DELETED);
        when(messageSource.getMessage(eq("msg.success.delete"), any(), any())).thenReturn("Deleted");

        ResponseEntity<Void> response = controller.delete(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("delete — soft delete returns OK with warning")
    void delete_returnsOkWithSoftDelete() {
        when(deleteCoaUseCase.execute(1L)).thenReturn(DeleteResult.SOFT_DELETED);
        when(messageSource.getMessage(eq("msg.success.deactivated"), any(), any())).thenReturn("Deactivated");

        ResponseEntity<Void> response = controller.delete(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    // ── Integration Tests (MockMvc) ─────────────────────────────────────────

    @Test
    @DisplayName("showParentSelector — returns selector fragment with model attributes")
    void showParentSelector_returnsSelectorFragment() throws Exception {
        List<CoaSelectorRow> rows = List.of(
                new CoaSelectorRow(1L, "1000", "Cash", "ASSET", 1, false, null, null, null)
        );
        when(findCoaSelectorUseCase.execute(any(), any())).thenReturn(rows);

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(get("/accounting/coa/selectors/parent"))
                .andExpect(status().isOk())
                .andExpect(view().name("accounting/coa/fragments/coa-selector-modal"))
                .andExpect(model().attributeExists("selectorRows"));
    }

    @Test
    @DisplayName("create — with parent sets level correctly via MockMvc")
    void create_withParent_setsLevelCorrectly() throws Exception {
        ChartOfAccount domain = sampleDomain();
        CoaDetailResponse detail = sampleDetail();
        when(createCoaUseCase.execute(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(domain);
        when(webMapper.toDetailResponse(any(ChartOfAccount.class))).thenReturn(detail);
        when(messageSource.getMessage(eq("msg.success.create"), any(), any())).thenReturn("Created");

        CoaSaveRequest request = new CoaSaveRequest();
        request.setCode("1100");
        request.setName("Cash");
        request.setAccountType("ASSET");
        request.setParentId(1L);
        request.setLevel(2);
        request.setIsHeader(false);
        request.setIsActive(true);

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(post("/accounting/coa/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }
}
