package com.solusi.erp.accounting.schema.web.controller;

import com.solusi.erp.accounting.coa.application.usecase.query.CoaSelectorRow;
import com.solusi.erp.accounting.coa.application.usecase.query.FindCoaSelectorUseCase;
import com.solusi.erp.accounting.schema.application.usecase.command.CreateSchemaUseCase;
import com.solusi.erp.accounting.schema.application.usecase.command.DeleteSchemaUseCase;
import com.solusi.erp.accounting.schema.application.usecase.command.UpdateSchemaUseCase;
import com.solusi.erp.accounting.schema.application.usecase.query.FindSchemasUseCase;
import com.solusi.erp.accounting.schema.application.usecase.query.GetSchemaEditViewUseCase;
import com.solusi.erp.accounting.coa.domain.model.AccountType;
import com.solusi.erp.accounting.schema.domain.model.AccountingSchema;
import com.solusi.erp.accounting.schema.domain.model.SchemaEventType;
import com.solusi.erp.accounting.schema.web.dto.SchemaDetailResponse;
import com.solusi.erp.accounting.schema.web.dto.SchemaSaveRequest;
import com.solusi.erp.accounting.schema.web.dto.SchemaSummaryResponse;
import com.solusi.erp.accounting.schema.web.mapper.SchemaWebMapper;
import com.solusi.erp.core.domain.model.DeleteResult;
import com.solusi.erp.core.dto.ApiResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@DisplayName("SchemaController — Unit Test")
public class SchemaControllerTest {

    private final CreateSchemaUseCase createSchemaUseCase = mock(CreateSchemaUseCase.class);
    private final UpdateSchemaUseCase updateSchemaUseCase = mock(UpdateSchemaUseCase.class);
    private final DeleteSchemaUseCase deleteSchemaUseCase = mock(DeleteSchemaUseCase.class);
    private final FindSchemasUseCase findSchemasUseCase = mock(FindSchemasUseCase.class);
    private final GetSchemaEditViewUseCase getSchemaEditViewUseCase = mock(GetSchemaEditViewUseCase.class);
    private final FindCoaSelectorUseCase findCoaSelectorUseCase = mock(FindCoaSelectorUseCase.class);
    private final SchemaWebMapper webMapper = mock(SchemaWebMapper.class);
    private final MessageSource messageSource = mock(MessageSource.class);

    private final SchemaController controller = new SchemaController(
            createSchemaUseCase, updateSchemaUseCase, deleteSchemaUseCase,
            findSchemasUseCase, getSchemaEditViewUseCase, findCoaSelectorUseCase, webMapper, messageSource
    );

    // ── helpers ──────────────────────────────────────────────────────────────

    private AccountingSchema sampleDomain() {
        return AccountingSchema.createNew(SchemaEventType.GOODS_RECEIPT,
                "Goods receipt schema", 1L, 2L, true);
    }

    private SchemaSummaryResponse sampleSummary() {
        SchemaSummaryResponse dto = new SchemaSummaryResponse();
        dto.setId(1L);
        dto.setEventType("GOODS_RECEIPT");
        dto.setDescription("Goods receipt schema");
        dto.setDebitAccountId(1L);
        dto.setDebitAccountName("1000 - Cash");
        dto.setCreditAccountId(2L);
        dto.setCreditAccountName("2000 - Accounts Payable");
        dto.setIsActive(true);
        return dto;
    }

    private SchemaDetailResponse sampleDetail() {
        SchemaDetailResponse dto = new SchemaDetailResponse();
        dto.setId(1L);
        dto.setEventType("GOODS_RECEIPT");
        dto.setDescription("Goods receipt schema");
        dto.setDebitAccountId(1L);
        dto.setDebitAccountName("1000 - Cash");
        dto.setCreditAccountId(2L);
        dto.setCreditAccountName("2000 - Accounts Payable");
        dto.setIsActive(true);
        return dto;
    }

    private SchemaSaveRequest validRequest() {
        SchemaSaveRequest req = new SchemaSaveRequest();
        req.setEventType("GOODS_RECEIPT");
        req.setDescription("Goods receipt schema");
        req.setDebitAccountId(1L);
        req.setCreditAccountId(2L);
        req.setIsActive(true);
        return req;
    }

    // ── list ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("list — returns correct view and model attributes")
    void list_returnsCorrectViewAndModel() {
        AccountingSchema domain = sampleDomain();
        com.solusi.erp.core.domain.model.Page<AccountingSchema> domainPage =
                new com.solusi.erp.core.domain.model.Page<>(List.of(domain), 0, 1, 1L);
        when(findSchemasUseCase.execute(any(), any())).thenReturn(domainPage);
        when(webMapper.toSummaryResponse(any(AccountingSchema.class))).thenReturn(sampleSummary());

        org.springframework.data.domain.Pageable springPageable =
                org.springframework.data.domain.PageRequest.of(0, 20);
        Model model = new ExtendedModelMap();

        String view = controller.list(null, springPageable, model);

        assertEquals("accounting/schema/list", view);
        Object pageObj = model.getAttribute("page");
        assertThat(pageObj).isInstanceOf(org.springframework.data.domain.Page.class);
        org.springframework.data.domain.Page<?> springPage = (org.springframework.data.domain.Page<?>) pageObj;
        assertEquals(1, springPage.getTotalElements());
        assertThat(springPage.getContent().get(0)).isInstanceOf(SchemaSummaryResponse.class);
        assertThat(model.getAttribute("keyword")).isNull();
        assertThat(model.getAttribute("eventTypes")).isEqualTo(SchemaEventType.values());
    }

    // ── showCreateForm ───────────────────────────────────────────────────────

    @Test
    @DisplayName("showCreateForm — returns form view with correct defaults")
    void showCreateForm_returnsFormViewWithDefaults() {
        Model model = new ExtendedModelMap();

        String view = controller.showCreateForm(model);

        assertEquals("accounting/schema/form", view);
        SchemaSaveRequest req = (SchemaSaveRequest) model.getAttribute("schemaRequest");
        assertThat(req).isNotNull();
        assertThat(req.getIsActive()).isTrue();
        assertThat(model.getAttribute("eventTypes")).isEqualTo(SchemaEventType.values());
    }

    // ── selectors ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("showAccountSelector — returns selector fragment with paged rows")
    void showAccountSelector_returnsSelectorFragmentWithPagedRows() {
        CoaSelectorRow row = new CoaSelectorRow(
                1L, "1000", "Cash", AccountType.ASSET.name(), 1, false, null, null, null
        );
        com.solusi.erp.core.domain.model.Page<CoaSelectorRow> domainPage =
                new com.solusi.erp.core.domain.model.Page<>(List.of(row), 0, 10, 1L);
        when(findCoaSelectorUseCase.execute(eq("cash"), eq("ASSET"), any())).thenReturn(domainPage);

        org.springframework.data.domain.Pageable springPageable =
                org.springframework.data.domain.PageRequest.of(0, 10);
        Model model = new ExtendedModelMap();

        String view = controller.showAccountSelector("cash", "ASSET", springPageable, model);

        assertEquals("accounting/schema/fragments/account-selector-modal", view);
        assertThat(model.getAttribute("page")).isInstanceOf(org.springframework.data.domain.Page.class);
        assertThat(model.getAttribute("keyword")).isEqualTo("cash");
        assertThat(model.getAttribute("accountType")).isEqualTo("ASSET");
        assertThat(model.getAttribute("accountTypes")).isEqualTo(AccountType.values());
    }

    // ── create ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("create — valid request returns CREATED with data")
    void create_returnsCreatedResponse() {
        AccountingSchema domain = sampleDomain();
        SchemaDetailResponse detail = sampleDetail();
        when(createSchemaUseCase.execute(any(), any(), any(), any(), any()))
                .thenReturn(domain);
        when(webMapper.toDetailResponse(any(AccountingSchema.class))).thenReturn(detail);
        when(messageSource.getMessage(eq("msg.success.create"), any(), any())).thenReturn("Created");

        ResponseEntity<ApiResponse<SchemaDetailResponse>> response = controller.create(validRequest());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getData().getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("create — invalid event type returns BAD_REQUEST")
    void create_returnsBadRequestForInvalidEventType() {
        SchemaSaveRequest req = validRequest();
        req.setEventType("INVALID");

        ResponseEntity<ApiResponse<SchemaDetailResponse>> response = controller.create(req);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
    }

    // ── showEditForm ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("showEditForm — populates model with request and audit info")
    void showEditForm_populatesModel() {
        AccountingSchema domain = sampleDomain();
        when(getSchemaEditViewUseCase.execute(1L)).thenReturn(Optional.of(domain));
        SchemaSaveRequest saveReq = validRequest();
        saveReq.setId(1L);
        when(webMapper.toSaveRequest(any(AccountingSchema.class))).thenReturn(saveReq);
        when(webMapper.toDetailResponse(any(AccountingSchema.class))).thenReturn(sampleDetail());

        Model model = new ExtendedModelMap();
        String view = controller.showEditForm(1L, model);

        assertEquals("accounting/schema/form", view);
        assertThat(model.getAttribute("schemaRequest")).isNotNull();
        assertThat(model.getAttribute("auditInfo")).isNotNull();
        assertThat(model.getAttribute("eventTypes")).isEqualTo(SchemaEventType.values());
    }

    @Test
    @DisplayName("showEditForm — throws when not found")
    void showEditForm_throwsWhenNotFound() {
        when(getSchemaEditViewUseCase.execute(999L)).thenReturn(Optional.empty());
        Model model = new ExtendedModelMap();

        assertThatThrownBy(() -> controller.showEditForm(999L, model))
                .isInstanceOf(RuntimeException.class);
    }

    // ── update ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("update — valid request returns OK")
    void update_returnsOkResponse() {
        AccountingSchema domain = sampleDomain();
        SchemaDetailResponse detail = sampleDetail();
        when(updateSchemaUseCase.execute(any(), any(), any(), any(), any()))
                .thenReturn(domain);
        when(webMapper.toDetailResponse(any(AccountingSchema.class))).thenReturn(detail);
        when(messageSource.getMessage(eq("msg.success.update"), any(), any())).thenReturn("Updated");

        ResponseEntity<ApiResponse<SchemaDetailResponse>> response = controller.update(1L, validRequest());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
    }

    // ── delete ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("delete — hard delete returns OK")
    void delete_returnsOkWithHardDelete() {
        when(deleteSchemaUseCase.execute(1L)).thenReturn(DeleteResult.HARD_DELETED);
        when(messageSource.getMessage(eq("msg.success.delete"), any(), any())).thenReturn("Deleted");

        ResponseEntity<Void> response = controller.delete(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("delete — soft delete returns OK with warning")
    void delete_returnsOkWithSoftDelete() {
        when(deleteSchemaUseCase.execute(1L)).thenReturn(DeleteResult.SOFT_DELETED);
        when(messageSource.getMessage(eq("msg.success.deactivated"), any(), any())).thenReturn("Deactivated");

        ResponseEntity<Void> response = controller.delete(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
