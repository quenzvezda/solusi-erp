package com.solusi.erp.accountspayable.vendorbill.web.controller;

import com.solusi.erp.accountspayable.debitmemoallocation.application.usecase.query.DebitMemoAllocationSelectorUseCase;
import com.solusi.erp.accountspayable.debitmemoallocation.application.usecase.query.FindDebitMemoAllocationHistoryUseCase;
import com.solusi.erp.accountspayable.vendorbill.application.usecase.command.*;
import com.solusi.erp.accountspayable.vendorbill.application.usecase.query.*;
import com.solusi.erp.accountspayable.vendorbill.domain.model.VendorBillDocumentStatus;
import com.solusi.erp.accountspayable.vendorbill.domain.model.VendorBillSettlementStatus;
import com.solusi.erp.accountspayable.vendorbill.domain.port.BillableApReference;
import com.solusi.erp.accountspayable.vendorbill.domain.port.BillableGrLineView;
import com.solusi.erp.accountspayable.vendorbill.web.dto.VendorBillDetailResponse;
import com.solusi.erp.accountspayable.vendorbill.web.dto.VendorBillFormView;
import com.solusi.erp.accountspayable.vendorbill.web.dto.VendorBillSaveCommand;
import com.solusi.erp.accountspayable.vendorbill.web.dto.VendorBillSaveRequest;
import com.solusi.erp.accountspayable.vendorbill.web.dto.VendorBillSummaryResponse;
import com.solusi.erp.accountspayable.vendorbill.web.mapper.VendorBillWebMapper;
import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.dto.ApiResponse;
import com.solusi.erp.core.dto.LookupDto;
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
import java.util.Arrays;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class VendorBillControllerTest {

    private CreateVendorBillUseCase createUseCase;
    private UpdateVendorBillUseCase updateUseCase;
    private DeleteVendorBillUseCase deleteUseCase;
    private CancelVendorBillUseCase cancelUseCase;
    private ConfirmVendorBillUseCase confirmUseCase;
    private FindVendorBillsUseCase findUseCase;
    private GetVendorBillDetailUseCase detailUseCase;
    private GetVendorBillCreateViewUseCase createViewUseCase;
    private FindBillableGrLinesUseCase findBillableGrLinesUseCase;
    private FindBillableReferencesUseCase findBillableReferencesUseCase;
    private FindDebitMemoAllocationHistoryUseCase allocationHistoryUseCase;
    private DebitMemoAllocationSelectorUseCase allocationSelectorUseCase;
    private VendorBillWebMapper webMapper;
    private PartyLookupProvider partyLookupProvider;
    private MessageSource messageSource;
    private VendorBillController controller;

    @BeforeEach
    void setUp() {
        createUseCase = mock(CreateVendorBillUseCase.class);
        updateUseCase = mock(UpdateVendorBillUseCase.class);
        deleteUseCase = mock(DeleteVendorBillUseCase.class);
        cancelUseCase = mock(CancelVendorBillUseCase.class);
        confirmUseCase = mock(ConfirmVendorBillUseCase.class);
        findUseCase = mock(FindVendorBillsUseCase.class);
        detailUseCase = mock(GetVendorBillDetailUseCase.class);
        createViewUseCase = mock(GetVendorBillCreateViewUseCase.class);
        findBillableGrLinesUseCase = mock(FindBillableGrLinesUseCase.class);
        findBillableReferencesUseCase = mock(FindBillableReferencesUseCase.class);
        allocationHistoryUseCase = mock(FindDebitMemoAllocationHistoryUseCase.class);
        allocationSelectorUseCase = mock(DebitMemoAllocationSelectorUseCase.class);
        webMapper = mock(VendorBillWebMapper.class);
        partyLookupProvider = mock(PartyLookupProvider.class);
        messageSource = mock(MessageSource.class);
        controller = new VendorBillController(
                createUseCase,
                updateUseCase,
                deleteUseCase,
                cancelUseCase,
                confirmUseCase,
                findUseCase,
                detailUseCase,
                createViewUseCase,
                findBillableGrLinesUseCase,
                findBillableReferencesUseCase,
                allocationHistoryUseCase,
                allocationSelectorUseCase,
                webMapper,
                partyLookupProvider,
                messageSource
        );
    }

    @Test
    void list_should_render_vendor_bill_list_template() {
        VendorBillSummaryView summary = new VendorBillSummaryView(
                1L,
                "VB-202605-00001",
                10L,
                "INV-001",
                LocalDate.of(2026, 5, 10),
                LocalDate.of(2026, 5, 20),
                VendorBillDocumentStatus.DRAFT,
                null,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO
        );
        when(findUseCase.execute(any(), any(), any(), any(), any()))
                .thenReturn(new Page<>(List.of(summary), 0, 20, 1));
        VendorBillSummaryResponse response = new VendorBillSummaryResponse();
        response.setId(1L);
        response.setCode("VB-202605-00001");
        when(webMapper.toSummaryResponse(summary)).thenReturn(response);

        Model model = new ExtendedModelMap();
        String view = controller.list("INV", 10L, VendorBillDocumentStatus.DRAFT, null, PageRequest.of(0, 20), model);

        assertThat(view).isEqualTo("accountspayable/vendor-bills/list");
        assertThat(model.getAttribute("page")).isInstanceOf(org.springframework.data.domain.Page.class);
        assertThat(model.getAttribute("keyword")).isEqualTo("INV");
        assertThat(model.getAttribute("vendorId")).isEqualTo(10L);
        assertThat(model.getAttribute("documentStatus")).isEqualTo(VendorBillDocumentStatus.DRAFT);
        assertThat(model.getAttribute("documentStatuses")).isNotNull();
        assertThat(model.getAttribute("settlementStatuses")).isNotNull();
    }

    @Test
    void confirm_should_return_success_api_response() {
        VendorBillDetailView detail = new VendorBillDetailView(
                1L,
                "VB-202605-00001",
                10L,
                "INV-001",
                LocalDate.of(2026, 5, 10),
                LocalDate.of(2026, 5, 20),
                1L,
                BigDecimal.ONE,
                VendorBillDocumentStatus.CONFIRMED,
                VendorBillSettlementStatus.PARTIALLY_SETTLED,
                new BigDecimal("100.0000"),
                BigDecimal.ZERO,
                new BigDecimal("100.0000"),
                new BigDecimal("50.0000"),
                BigDecimal.ZERO,
                new BigDecimal("50.0000"),
                "notes",
                List.of(88L),
                List.of()
        );
        VendorBillDetailResponse response = new VendorBillDetailResponse();
        response.setId(1L);
        response.setCode("VB-202605-00001");
        response.setDocumentStatus("CONFIRMED");
        when(detailUseCase.execute(1L)).thenReturn(detail);
        when(webMapper.toDetailResponse(detail)).thenReturn(response);
        when(messageSource.getMessage(anyString(), any(), any())).thenReturn("Confirmed");

        ResponseEntity<ApiResponse<VendorBillDetailResponse>> result = controller.confirm(1L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().isSuccess()).isTrue();
        assertThat(result.getBody().getData().getDocumentStatus()).isEqualTo("CONFIRMED");
        verify(confirmUseCase).execute(1L);
    }

    @Test
    void detail_should_enrich_vendor_display_from_party_lookup_provider() {
        VendorBillDetailView detail = new VendorBillDetailView(
                1L,
                "VB-202605-00001",
                10L,
                "INV-001",
                LocalDate.of(2026, 5, 10),
                LocalDate.of(2026, 5, 20),
                1L,
                BigDecimal.ONE,
                VendorBillDocumentStatus.CONFIRMED,
                VendorBillSettlementStatus.OPEN,
                new BigDecimal("100.0000"),
                BigDecimal.ZERO,
                new BigDecimal("100.0000"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                new BigDecimal("100.0000"),
                "notes",
                List.of(88L),
                List.of()
        );
        VendorBillDetailResponse response = new VendorBillDetailResponse();
        response.setId(1L);
        response.setVendorId(10L);
        response.setDocumentStatus("CONFIRMED");
        response.setSettlementStatus("OPEN");
        response.setOutstandingAmount(new BigDecimal("100.0000"));
        when(detailUseCase.execute(1L)).thenReturn(detail);
        when(webMapper.toDetailResponse(detail)).thenReturn(response);
        when(partyLookupProvider.resolve(10L)).thenReturn(new LookupDto(10L, "PT Solusi Supplier", "SUP-001"));
        when(allocationSelectorUseCase.eligibleDebitMemos(eq(1L), isNull(), any()))
                .thenReturn(new Page<>(List.of(), 0, 1, 0));

        Model model = new ExtendedModelMap();
        String view = controller.detail(1L, model);

        assertThat(view).isEqualTo("accountspayable/vendor-bills/detail");
        VendorBillDetailResponse bill = (VendorBillDetailResponse) model.getAttribute("bill");
        assertThat(bill).isNotNull();
        assertThat(bill.getVendorName()).isEqualTo("PT Solusi Supplier");
        assertThat(bill.getVendorCode()).isEqualTo("SUP-001");
        verify(partyLookupProvider).resolve(10L);
    }

    @Test
    void create_should_call_create_use_case() {
        VendorBillSaveRequest request = new VendorBillSaveRequest();
        VendorBillSaveCommand command = new VendorBillSaveCommand(
                null,
                10L,
                "INV-001",
                LocalDate.of(2026, 5, 10),
                LocalDate.of(2026, 5, 20),
                1L,
                BigDecimal.ONE,
                "notes",
                List.of(88L),
                List.of()
        );
        when(webMapper.toCreateCommand(request)).thenReturn(command);
        when(messageSource.getMessage(anyString(), any(), any())).thenReturn("Created");

        ResponseEntity<ApiResponse<Void>> result = controller.create(request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        verify(createUseCase).execute(
                10L,
                "INV-001",
                LocalDate.of(2026, 5, 10),
                LocalDate.of(2026, 5, 20),
                1L,
                BigDecimal.ONE,
                "notes",
                List.of(88L),
                List.of()
        );
    }

    @Test
    void createForm_should_prefill_reference_metadata_and_today_dates() {
        BillableApReference reference = new BillableApReference(
                "GOODS_RECEIPT",
                88L,
                "GR-001",
                LocalDate.of(2026, 5, 12),
                10L,
                "PT Vendor",
                1L,
                "IDR",
                new BigDecimal("1.000000"),
                new BigDecimal("100.0000"),
                1,
                "COMPLETED"
        );
        BillableGrLineView line = new BillableGrLineView(
                1001L,
                88L,
                2001L,
                "Product A",
                "PRD-A",
                new BigDecimal("10.0000"),
                1L,
                "PCS",
                new BigDecimal("1.0000"),
                new BigDecimal("10.0000"),
                BigDecimal.ZERO,
                new BigDecimal("10.0000"),
                new BigDecimal("1.0000")
        );
        VendorBillSaveRequest request = new VendorBillSaveRequest();
        VendorBillCreateView view = new VendorBillCreateView(10L, 1L, BigDecimal.ONE, List.of());
        when(findBillableReferencesUseCase.execute(null, null)).thenReturn(List.of(reference));
        when(createViewUseCase.execute(10L, 1L)).thenReturn(view);
        when(webMapper.toFormView(view)).thenReturn(new VendorBillFormView(request, null, null, false, List.of()));
        when(findBillableGrLinesUseCase.execute(88L)).thenReturn(List.of(line));

        Model model = new ExtendedModelMap();
        String result = controller.createForm(null, null, "88", model);

        assertThat(result).isEqualTo("accountspayable/vendor-bills/form");
        VendorBillFormView form = (VendorBillFormView) model.getAttribute("form");
        assertThat(form).isNotNull();
        assertThat(form.vendorName()).isEqualTo("PT Vendor");
        assertThat(form.currencyCode()).isEqualTo("IDR");
        assertThat(form.request().getVendorId()).isEqualTo(10L);
        assertThat(form.request().getCurrencyId()).isEqualTo(1L);
        assertThat(form.request().getExchangeRate()).isEqualByComparingTo("1.000000");
        assertThat(form.request().getBillDate()).isEqualTo(LocalDate.now());
        assertThat(form.request().getDueDate()).isEqualTo(LocalDate.now());
        assertThat(form.request().getLines()).hasSize(1);
    }

    @Test
    void cancel_should_require_cancel_authority() throws Exception {
        Method method = VendorBillController.class.getMethod("cancel", Long.class);

        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);

        assertThat(annotation).isNotNull();
        assertThat(annotation.value()).contains("VENDOR-BILL_CANCEL");
    }

    @Test
    void controller_should_expose_billable_gr_lines_selector_endpoint() {
        boolean exists = Arrays.stream(VendorBillController.class.getDeclaredMethods())
                .anyMatch(method -> method.getName().equals("billableGrLines"));

        assertThat(exists).isTrue();
    }

    @Test
    void billableGrLines_should_return_selector_line_data() {
        BillableGrLineView line = new BillableGrLineView(
                1001L,
                88L,
                2001L,
                "Product A",
                "PRD-A",
                new BigDecimal("10.0000"),
                1L,
                "PCS",
                new BigDecimal("10.0000"),
                new BigDecimal("100.0000"),
                BigDecimal.ZERO,
                new BigDecimal("100.0000"),
                new BigDecimal("4.0000")
        );
        when(findBillableGrLinesUseCase.execute(88L)).thenReturn(List.of(line));

        ResponseEntity<ApiResponse<List<BillableGrLineView>>> result = controller.billableGrLines(88L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getData()).containsExactly(line);
    }
}
