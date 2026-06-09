package com.solusi.erp.accountspayable.debitmemoallocation.web.controller;

import com.solusi.erp.accountspayable.debitmemoallocation.application.usecase.command.*;
import com.solusi.erp.accountspayable.debitmemoallocation.application.usecase.query.*;
import com.solusi.erp.accountspayable.debitmemoallocation.domain.model.DebitMemoAllocationStatus;
import com.solusi.erp.accountspayable.debitmemoallocation.domain.port.DebitMemoAllocationSourcePort;
import com.solusi.erp.accountspayable.debitmemoallocation.web.dto.DebitMemoAllocationDetailResponse;
import com.solusi.erp.accountspayable.debitmemoallocation.web.dto.DebitMemoAllocationReverseRequest;
import com.solusi.erp.accountspayable.debitmemoallocation.web.dto.DebitMemoAllocationSaveRequest;
import com.solusi.erp.accountspayable.debitmemoallocation.web.mapper.DebitMemoAllocationWebMapper;
import com.solusi.erp.accountspayable.debitmemoallocation.web.mapper.DebitMemoAllocationWebMapperTest;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DebitMemoAllocationControllerTest {

    private FindDebitMemoAllocationsUseCase findUseCase;
    private GetDebitMemoAllocationDetailUseCase detailUseCase;
    private CreateDebitMemoAllocationUseCase createUseCase;
    private UpdateDebitMemoAllocationUseCase updateUseCase;
    private CancelDebitMemoAllocationUseCase cancelUseCase;
    private ConfirmDebitMemoAllocationUseCase confirmUseCase;
    private ReverseDebitMemoAllocationUseCase reverseUseCase;
    private DebitMemoAllocationSelectorUseCase selectorUseCase;
    private PartyLookupProvider partyLookupProvider;
    private CurrencyLookupProvider currencyLookupProvider;
    private DebitMemoAllocationController controller;

    @BeforeEach
    void setUp() {
        findUseCase = mock(FindDebitMemoAllocationsUseCase.class);
        detailUseCase = mock(GetDebitMemoAllocationDetailUseCase.class);
        createUseCase = mock(CreateDebitMemoAllocationUseCase.class);
        updateUseCase = mock(UpdateDebitMemoAllocationUseCase.class);
        cancelUseCase = mock(CancelDebitMemoAllocationUseCase.class);
        confirmUseCase = mock(ConfirmDebitMemoAllocationUseCase.class);
        reverseUseCase = mock(ReverseDebitMemoAllocationUseCase.class);
        selectorUseCase = mock(DebitMemoAllocationSelectorUseCase.class);
        partyLookupProvider = mock(PartyLookupProvider.class);
        currencyLookupProvider = mock(CurrencyLookupProvider.class);
        MessageSource messageSource = mock(MessageSource.class);
        when(messageSource.getMessage(anyString(), any(), any())).thenReturn("ok");
        controller = new DebitMemoAllocationController(findUseCase, detailUseCase, createUseCase, updateUseCase,
                cancelUseCase, confirmUseCase, reverseUseCase, selectorUseCase,
                new DebitMemoAllocationWebMapper(), partyLookupProvider, currencyLookupProvider, messageSource);
    }

    @Test
    void list_should_render_list_view_and_model() {
        DebitMemoAllocationSummaryView summary = new DebitMemoAllocationSummaryView(
                1L, "DMA-001", 10L, "DM-001", 22L, 1L, LocalDate.of(2026, 6, 5),
                DebitMemoAllocationStatus.DRAFT, bd("40.0000"), bd("50.0000"), BigDecimal.ZERO, BigDecimal.ZERO);
        when(findUseCase.execute(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new Page<>(List.of(summary), 0, 20, 1));
        when(partyLookupProvider.resolve(22L)).thenReturn(new LookupDto(22L, "Vendor A", "VEN-001"));
        when(currencyLookupProvider.resolve(1L)).thenReturn(new LookupDto(1L, "US Dollar", "$ - USD"));

        Model model = new ExtendedModelMap();
        String view = controller.list("DMA", 10L, 22L, DebitMemoAllocationStatus.DRAFT,
                null, null, PageRequest.of(0, 20), model);

        assertThat(view).isEqualTo("accountspayable/debit-memo-allocations/list");
        assertThat(model.getAttribute("page")).isInstanceOf(org.springframework.data.domain.Page.class);
        assertThat(model.getAttribute("vendorId")).isEqualTo(22L);
        assertThat(model.getAttribute("vendorText")).isEqualTo("Vendor A");
        assertThat(model.getAttribute("statuses")).isNotNull();
    }

    @Test
    void detail_should_render_detail_view() {
        when(detailUseCase.execute(1L)).thenReturn(DebitMemoAllocationWebMapperTest.detailView());
        Model model = new ExtendedModelMap();

        String view = controller.detail(1L, model);

        assertThat(view).isEqualTo("accountspayable/debit-memo-allocations/detail");
        assertThat(model.getAttribute("allocation")).isInstanceOf(DebitMemoAllocationDetailResponse.class);
        assertThat(model.getAttribute("reverseRequest")).isInstanceOf(DebitMemoAllocationReverseRequest.class);
    }

    @Test
    void create_should_call_use_case_and_return_created_response() {
        when(createUseCase.execute(any())).thenReturn(DebitMemoAllocationWebMapperTest.detailView());
        DebitMemoAllocationSaveRequest request = saveRequest();

        ResponseEntity<ApiResponse<DebitMemoAllocationDetailResponse>> result = controller.create(request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getData().getCode()).isEqualTo("DMA-001");
        verify(createUseCase).execute(any(CreateDebitMemoAllocationCommand.class));
    }

    @Test
    void confirm_and_reverse_should_return_success_response() {
        when(detailUseCase.execute(1L)).thenReturn(DebitMemoAllocationWebMapperTest.detailView());
        assertThat(controller.confirm(1L).getStatusCode()).isEqualTo(HttpStatus.OK);

        DebitMemoAllocationReverseRequest reverseRequest = new DebitMemoAllocationReverseRequest();
        reverseRequest.setReversalDate(LocalDate.of(2026, 6, 6));
        reverseRequest.setReversalReason("wrong allocation");
        assertThat(controller.reverse(1L, reverseRequest).getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(reverseUseCase).execute(any(ReverseDebitMemoAllocationCommand.class));
    }

    @Test
    void selector_fragments_should_populate_model() {
        when(selectorUseCase.eligibleVendorBills(eq(10L), any(), any())).thenReturn(new Page<>(List.of(
                new DebitMemoAllocationSourcePort.EligibleVendorBill(20L, "VB-001", bd("100.0000"), bd("80.0000"), bd("1.000000"))
        ), 0, 20, 1));
        when(selectorUseCase.eligibleDebitMemos(eq(20L), any(), any())).thenReturn(new Page<>(List.of(
                new DebitMemoAllocationSourcePort.EligibleDebitMemo(10L, "DM-001", bd("100.0000"), bd("80.0000"))
        ), 0, 20, 1));
        Model model = new ExtendedModelMap();

        String view = controller.eligibleVendorBills(10L, null, PageRequest.of(0, 20), model);

        assertThat(view).isEqualTo("accountspayable/debit-memo-allocations/fragments/vendor-bill-selector");
        assertThat(model.getAttribute("page")).isInstanceOf(org.springframework.data.domain.Page.class);

        Model debitMemoModel = new ExtendedModelMap();
        String debitMemoView = controller.eligibleDebitMemos(20L, "DM", PageRequest.of(0, 20), debitMemoModel);

        assertThat(debitMemoView).isEqualTo("accountspayable/debit-memo-allocations/fragments/debit-memo-selector");
        assertThat(debitMemoModel.getAttribute("page")).isInstanceOf(org.springframework.data.domain.Page.class);
        assertThat(debitMemoModel.getAttribute("vendorBillId")).isEqualTo(20L);
        assertThat(debitMemoModel.getAttribute("q")).isEqualTo("DM");
    }

    @Test
    void routes_should_have_expected_authorities() throws Exception {
        assertAuth("list", "DEBIT-MEMO-ALLOCATION_READ", String.class, Long.class, Long.class,
                DebitMemoAllocationStatus.class, LocalDate.class, LocalDate.class,
                org.springframework.data.domain.Pageable.class, Model.class);
        assertAuth("createForm", "DEBIT-MEMO-ALLOCATION_CREATE", Long.class, String.class, Long.class, Model.class);
        assertAuth("editForm", "DEBIT-MEMO-ALLOCATION_UPDATE", Long.class, Model.class);
        assertAuth("detail", "DEBIT-MEMO-ALLOCATION_READ", Long.class, Model.class);
        assertAuth("create", "DEBIT-MEMO-ALLOCATION_CREATE", DebitMemoAllocationSaveRequest.class);
        assertAuth("update", "DEBIT-MEMO-ALLOCATION_UPDATE", Long.class, DebitMemoAllocationSaveRequest.class);
        assertAuth("confirm", "DEBIT-MEMO-ALLOCATION_CONFIRM", Long.class);
        assertAuth("cancel", "DEBIT-MEMO-ALLOCATION_CANCEL", Long.class);
        assertAuth("reverse", "DEBIT-MEMO-ALLOCATION_REVERSE", Long.class, DebitMemoAllocationReverseRequest.class);
        assertAuth("eligibleVendorBills", "DEBIT-MEMO-ALLOCATION_CREATE", Long.class, String.class,
                org.springframework.data.domain.Pageable.class, Model.class);
        assertAuth("eligibleDebitMemos", "DEBIT-MEMO-ALLOCATION_CREATE", Long.class, String.class,
                org.springframework.data.domain.Pageable.class, Model.class);
    }

    private void assertAuth(String method, String authority, Class<?>... parameterTypes) throws Exception {
        Method reflected = DebitMemoAllocationController.class.getMethod(method, parameterTypes);
        PreAuthorize annotation = reflected.getAnnotation(PreAuthorize.class);
        assertThat(annotation).isNotNull();
        assertThat(annotation.value()).contains(authority);
    }

    private DebitMemoAllocationSaveRequest saveRequest() {
        DebitMemoAllocationSaveRequest request = new DebitMemoAllocationSaveRequest();
        request.setDebitMemoId(10L);
        request.setAllocationDate(LocalDate.of(2026, 6, 5));
        com.solusi.erp.accountspayable.debitmemoallocation.web.dto.DebitMemoAllocationLineRequest line =
                new com.solusi.erp.accountspayable.debitmemoallocation.web.dto.DebitMemoAllocationLineRequest();
        line.setVendorBillId(20L);
        line.setAppliedGrossOriginal(bd("40.0000"));
        request.setLines(List.of(line));
        return request;
    }

    private static BigDecimal bd(String value) {
        return new BigDecimal(value);
    }
}
