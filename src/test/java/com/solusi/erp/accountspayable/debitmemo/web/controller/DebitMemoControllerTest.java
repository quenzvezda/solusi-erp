package com.solusi.erp.accountspayable.debitmemo.web.controller;

import com.solusi.erp.accountspayable.debitmemo.application.usecase.command.CancelDebitMemoUseCase;
import com.solusi.erp.accountspayable.debitmemo.application.usecase.command.UpdateDebitMemoMetadataUseCase;
import com.solusi.erp.accountspayable.debitmemo.application.usecase.query.DebitMemoDetailView;
import com.solusi.erp.accountspayable.debitmemo.application.usecase.query.DebitMemoSummaryView;
import com.solusi.erp.accountspayable.debitmemo.application.usecase.query.FindDebitMemosUseCase;
import com.solusi.erp.accountspayable.debitmemo.application.usecase.query.GetDebitMemoDetailUseCase;
import com.solusi.erp.accountspayable.debitmemo.domain.model.DebitMemoSettlementStatus;
import com.solusi.erp.accountspayable.debitmemo.web.dto.DebitMemoDetailResponse;
import com.solusi.erp.accountspayable.debitmemo.web.dto.DebitMemoMetadataRequest;
import com.solusi.erp.accountspayable.debitmemo.web.dto.DebitMemoSummaryResponse;
import com.solusi.erp.accountspayable.debitmemo.web.mapper.DebitMemoWebMapper;
import com.solusi.erp.accountspayable.debitmemoallocation.application.usecase.query.FindDebitMemoAllocationHistoryUseCase;
import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.dto.ApiResponse;
import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.master.currency.domain.port.CurrencyLookupProvider;
import com.solusi.erp.master.party.domain.port.PartyLookupProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DebitMemoControllerTest {

    private FindDebitMemosUseCase findUseCase;
    private GetDebitMemoDetailUseCase detailUseCase;
    private UpdateDebitMemoMetadataUseCase updateMetadataUseCase;
    private CancelDebitMemoUseCase cancelUseCase;
    private FindDebitMemoAllocationHistoryUseCase historyUseCase;
    private DebitMemoWebMapper webMapper;
    private PartyLookupProvider partyLookupProvider;
    private CurrencyLookupProvider currencyLookupProvider;
    private MessageSource messageSource;
    private DebitMemoController controller;

    @BeforeEach
    void setUp() {
        findUseCase = mock(FindDebitMemosUseCase.class);
        detailUseCase = mock(GetDebitMemoDetailUseCase.class);
        updateMetadataUseCase = mock(UpdateDebitMemoMetadataUseCase.class);
        cancelUseCase = mock(CancelDebitMemoUseCase.class);
        historyUseCase = mock(FindDebitMemoAllocationHistoryUseCase.class);
        webMapper = mock(DebitMemoWebMapper.class);
        partyLookupProvider = mock(PartyLookupProvider.class);
        currencyLookupProvider = mock(CurrencyLookupProvider.class);
        messageSource = mock(MessageSource.class);
        controller = new DebitMemoController(
                findUseCase, detailUseCase, updateMetadataUseCase, cancelUseCase, historyUseCase, webMapper,
                partyLookupProvider, currencyLookupProvider, messageSource);
    }

    @Test
    void list_should_render_debit_memo_list_template() {
        DebitMemoSummaryView summary = new DebitMemoSummaryView(
                10L,
                "DM-202606-00001",
                LocalDate.of(2026, 6, 2),
                22L,
                1L,
                100L,
                "PRT-202606-00001",
                new BigDecimal("111.0000"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                new BigDecimal("111.0000"),
                DebitMemoSettlementStatus.OPEN
        );
        DebitMemoSummaryResponse response = new DebitMemoSummaryResponse();
        response.setId(10L);
        response.setCode("DM-202606-00001");
        response.setVendorId(22L);
        response.setCurrencyId(1L);
        when(findUseCase.execute(any(), any(), any(), any(), any(), any()))
                .thenReturn(new Page<>(List.of(summary), 0, 20, 1));
        when(webMapper.toSummaryResponse(summary)).thenReturn(response);
        when(partyLookupProvider.resolve(22L)).thenReturn(new LookupDto(22L, "Vendor A", "VEN-001"));
        when(currencyLookupProvider.resolve(1L)).thenReturn(new LookupDto(1L, "US Dollar", "$ - USD"));

        Model model = new ExtendedModelMap();
        String view = controller.list("DM", 22L, DebitMemoSettlementStatus.OPEN,
                LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30), PageRequest.of(0, 20), model);

        assertThat(view).isEqualTo("accountspayable/debit-memos/list");
        assertThat(model.getAttribute("page")).isInstanceOf(org.springframework.data.domain.Page.class);
        assertThat(model.getAttribute("keyword")).isEqualTo("DM");
        assertThat(model.getAttribute("vendorId")).isEqualTo(22L);
        assertThat(model.getAttribute("vendorText")).isEqualTo("Vendor A");
        assertThat(model.getAttribute("settlementStatus")).isEqualTo(DebitMemoSettlementStatus.OPEN);
        assertThat(model.getAttribute("settlementStatuses")).isNotNull();
    }

    @Test
    void detail_should_render_debit_memo_detail_template_with_metadata_request() {
        DebitMemoDetailView view = detailView();
        DebitMemoDetailResponse response = new DebitMemoDetailResponse();
        response.setId(10L);
        response.setCode("DM-202606-00001");
        response.setSupplierMemoNumber("SUP-DM-001");
        response.setSupplierMemoDate(LocalDate.of(2026, 6, 3));
        when(detailUseCase.execute(10L)).thenReturn(view);
        when(webMapper.toDetailResponse(view)).thenReturn(response);

        Model model = new ExtendedModelMap();
        String result = controller.detail(10L, model);

        assertThat(result).isEqualTo("accountspayable/debit-memos/detail");
        assertThat(model.getAttribute("debitMemo")).isSameAs(response);
        assertThat(model.getAttribute("metadataRequest")).isInstanceOf(DebitMemoMetadataRequest.class);
    }

    @Test
    void updateMetadata_should_call_usecase_and_return_detail() {
        DebitMemoMetadataRequest request = new DebitMemoMetadataRequest();
        request.setSupplierMemoNumber("SUP-DM-001");
        request.setSupplierMemoDate(LocalDate.of(2026, 6, 3));
        DebitMemoDetailView detail = detailView();
        DebitMemoDetailResponse response = new DebitMemoDetailResponse();
        response.setId(10L);
        when(detailUseCase.execute(10L)).thenReturn(detail);
        when(webMapper.toDetailResponse(detail)).thenReturn(response);
        when(messageSource.getMessage(anyString(), any(), any())).thenReturn("Updated");

        ResponseEntity<ApiResponse<DebitMemoDetailResponse>> result = controller.updateMetadata(10L, request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getData()).isSameAs(response);
        verify(updateMetadataUseCase).execute(10L, "SUP-DM-001", LocalDate.of(2026, 6, 3), null, null, null);
    }

    @Test
    void routes_should_have_expected_authorities() throws Exception {
        assertAuth("list", "DEBIT-MEMO_READ", String.class, Long.class,
                DebitMemoSettlementStatus.class, LocalDate.class, LocalDate.class,
                org.springframework.data.domain.Pageable.class, Model.class);
        assertAuth("detail", "DEBIT-MEMO_READ", Long.class, Model.class);
        assertAuth("updateMetadata", "DEBIT-MEMO_UPDATE-METADATA", Long.class, DebitMemoMetadataRequest.class);
        assertAuth("cancel", "DEBIT-MEMO_CANCEL", Long.class);
    }

    @Test
    void cancel_should_require_cancel_authority() throws Exception {
        Method method = DebitMemoController.class.getMethod("cancel", Long.class);

        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);

        assertThat(annotation).isNotNull();
        assertThat(annotation.value()).contains("DEBIT-MEMO_CANCEL");
    }

    @Test
    void updateMetadata_should_require_metadata_authority() throws Exception {
        Method method = DebitMemoController.class.getMethod("updateMetadata", Long.class, DebitMemoMetadataRequest.class);

        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);

        assertThat(annotation).isNotNull();
        assertThat(annotation.value()).contains("DEBIT-MEMO_UPDATE-METADATA");
    }

    private void assertAuth(String method, String authority, Class<?>... parameterTypes) throws Exception {
        Method reflected = DebitMemoController.class.getMethod(method, parameterTypes);
        PreAuthorize annotation = reflected.getAnnotation(PreAuthorize.class);
        assertThat(annotation).isNotNull();
        assertThat(annotation.value()).contains(authority);
    }

    private DebitMemoDetailView detailView() {
        return new DebitMemoDetailView(
                10L,
                "DM-202606-00001",
                100L,
                "PRT-202606-00001",
                900L,
                22L,
                1L,
                LocalDate.of(2026, 6, 2),
                new BigDecimal("111.0000"),
                new BigDecimal("100.0000"),
                new BigDecimal("11.0000"),
                new BigDecimal("111.0000"),
                new BigDecimal("100.0000"),
                new BigDecimal("11.0000"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                new BigDecimal("111.0000"),
                DebitMemoSettlementStatus.OPEN,
                null,
                null,
                null,
                null,
                null,
                List.of()
        );
    }
}
