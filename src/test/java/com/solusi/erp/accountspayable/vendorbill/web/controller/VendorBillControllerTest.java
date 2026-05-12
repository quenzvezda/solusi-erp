package com.solusi.erp.accountspayable.vendorbill.web.controller;

import com.solusi.erp.accountspayable.vendorbill.application.usecase.command.*;
import com.solusi.erp.accountspayable.vendorbill.application.usecase.query.*;
import com.solusi.erp.accountspayable.vendorbill.domain.model.VendorBillStatus;
import com.solusi.erp.accountspayable.vendorbill.domain.port.BillableGrLineView;
import com.solusi.erp.accountspayable.vendorbill.web.dto.VendorBillDetailResponse;
import com.solusi.erp.accountspayable.vendorbill.web.dto.VendorBillSaveCommand;
import com.solusi.erp.accountspayable.vendorbill.web.dto.VendorBillSaveRequest;
import com.solusi.erp.accountspayable.vendorbill.web.dto.VendorBillSummaryResponse;
import com.solusi.erp.accountspayable.vendorbill.web.mapper.VendorBillWebMapper;
import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.dto.ApiResponse;
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
    private VendorBillWebMapper webMapper;
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
        webMapper = mock(VendorBillWebMapper.class);
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
                webMapper,
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
                VendorBillStatus.DRAFT,
                BigDecimal.ZERO
        );
        when(findUseCase.execute(any(), any(), any(), any()))
                .thenReturn(new Page<>(List.of(summary), 0, 20, 1));
        VendorBillSummaryResponse response = new VendorBillSummaryResponse();
        response.setId(1L);
        response.setCode("VB-202605-00001");
        when(webMapper.toSummaryResponse(summary)).thenReturn(response);

        Model model = new ExtendedModelMap();
        String view = controller.list("INV", 10L, VendorBillStatus.DRAFT, PageRequest.of(0, 20), model);

        assertThat(view).isEqualTo("accountspayable/vendor-bills/list");
        assertThat(model.getAttribute("page")).isInstanceOf(org.springframework.data.domain.Page.class);
        assertThat(model.getAttribute("keyword")).isEqualTo("INV");
        assertThat(model.getAttribute("vendorId")).isEqualTo(10L);
        assertThat(model.getAttribute("status")).isEqualTo(VendorBillStatus.DRAFT);
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
                VendorBillStatus.CONFIRMED,
                new BigDecimal("100.0000"),
                BigDecimal.ZERO,
                new BigDecimal("100.0000"),
                "notes",
                List.of(88L),
                List.of()
        );
        VendorBillDetailResponse response = new VendorBillDetailResponse();
        response.setId(1L);
        response.setCode("VB-202605-00001");
        response.setStatus("CONFIRMED");
        when(detailUseCase.execute(1L)).thenReturn(detail);
        when(webMapper.toDetailResponse(detail)).thenReturn(response);
        when(messageSource.getMessage(anyString(), any(), any())).thenReturn("Confirmed");

        ResponseEntity<ApiResponse<VendorBillDetailResponse>> result = controller.confirm(1L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().isSuccess()).isTrue();
        assertThat(result.getBody().getData().getStatus()).isEqualTo("CONFIRMED");
        verify(confirmUseCase).execute(1L);
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
