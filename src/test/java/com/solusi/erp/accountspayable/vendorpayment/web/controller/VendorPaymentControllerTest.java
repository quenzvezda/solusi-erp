package com.solusi.erp.accountspayable.vendorpayment.web.controller;

import com.solusi.erp.accountspayable.vendorpayment.application.usecase.command.*;
import com.solusi.erp.accountspayable.vendorpayment.application.usecase.query.*;
import com.solusi.erp.accountspayable.vendorpayment.domain.model.VendorPayment;
import com.solusi.erp.accountspayable.vendorpayment.domain.model.VendorPaymentLine;
import com.solusi.erp.accountspayable.vendorpayment.domain.model.VendorPaymentStatus;
import com.solusi.erp.accountspayable.vendorpayment.web.mapper.VendorPaymentWebMapper;
import com.solusi.erp.core.domain.model.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("VendorPaymentController Tests")
class VendorPaymentControllerTest {

    private CreateVendorPaymentUseCase createUseCase;
    private UpdateVendorPaymentUseCase updateUseCase;
    private ConfirmVendorPaymentUseCase confirmUseCase;
    private CancelVendorPaymentUseCase cancelUseCase;
    private DeleteVendorPaymentUseCase deleteUseCase;
    private GetVendorPaymentListUseCase listUseCase;
    private GetVendorPaymentDetailUseCase detailUseCase;
    private GetPayableVendorBillsUseCase payableBillsUseCase;
    private VendorPaymentWebMapper webMapper;
    private MessageSource messageSource;
    private VendorPaymentController controller;

    @BeforeEach
    void setUp() {
        createUseCase = mock(CreateVendorPaymentUseCase.class);
        updateUseCase = mock(UpdateVendorPaymentUseCase.class);
        confirmUseCase = mock(ConfirmVendorPaymentUseCase.class);
        cancelUseCase = mock(CancelVendorPaymentUseCase.class);
        deleteUseCase = mock(DeleteVendorPaymentUseCase.class);
        listUseCase = mock(GetVendorPaymentListUseCase.class);
        detailUseCase = mock(GetVendorPaymentDetailUseCase.class);
        payableBillsUseCase = mock(GetPayableVendorBillsUseCase.class);
        webMapper = new VendorPaymentWebMapper();
        messageSource = mock(MessageSource.class);

        controller = new VendorPaymentController(
                createUseCase, updateUseCase, confirmUseCase, cancelUseCase,
                deleteUseCase, listUseCase, detailUseCase, payableBillsUseCase,
                webMapper, messageSource);
    }

    @Test
    @DisplayName("list returns correct view name and model")
    void list_returnsCorrectViewAndModel() {
        VendorPayment payment = draftPayment();
        Page<VendorPayment> domainPage = new Page<>(List.of(payment), 0, 20, 1L);
        when(listUseCase.execute(any(), any(), any(), any())).thenReturn(domainPage);

        org.springframework.data.domain.Pageable springPageable = org.springframework.data.domain.PageRequest.of(0, 20);
        Model model = new ExtendedModelMap();

        String view = controller.list(null, null, null, springPageable, model);

        assertThat(view).isEqualTo("accountspayable/vendor-payments/list");
        assertThat(model.getAttribute("page")).isNotNull();
    }

    @Test
    @DisplayName("createForm returns form view")
    void createForm_returnsFormView() {
        Model model = new ExtendedModelMap();
        String view = controller.createForm(model);
        assertThat(view).isEqualTo("accountspayable/vendor-payments/form");
        assertThat(model.getAttribute("paymentRequest")).isNotNull();
    }

    @Test
    @DisplayName("detail returns detail view")
    void detail_returnsDetailView() {
        VendorPayment payment = draftPayment();
        when(detailUseCase.execute(1L)).thenReturn(Optional.of(payment));

        Model model = new ExtendedModelMap();
        String view = controller.detail(1L, model);

        assertThat(view).isEqualTo("accountspayable/vendor-payments/detail");
        assertThat(model.getAttribute("payment")).isNotNull();
    }

    @Test
    @DisplayName("list endpoint requires VENDOR-PAYMENT_READ authority")
    void list_requiresReadAuthority() throws NoSuchMethodException {
        Method method = VendorPaymentController.class.getMethod("list", String.class, Long.class,
                VendorPaymentStatus.class, org.springframework.data.domain.Pageable.class, Model.class);
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
        assertThat(annotation).isNotNull();
        assertThat(annotation.value()).contains("VENDOR-PAYMENT_READ");
    }

    @Test
    @DisplayName("confirm endpoint requires VENDOR-PAYMENT_CONFIRM authority")
    void confirm_requiresConfirmAuthority() throws NoSuchMethodException {
        Method method = VendorPaymentController.class.getMethod("confirm", Long.class);
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
        assertThat(annotation).isNotNull();
        assertThat(annotation.value()).contains("VENDOR-PAYMENT_CONFIRM");
    }

    @Test
    @DisplayName("cancel endpoint requires VENDOR-PAYMENT_CANCEL authority")
    void cancel_requiresCancelAuthority() throws NoSuchMethodException {
        Method method = VendorPaymentController.class.getMethod("cancel", Long.class);
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
        assertThat(annotation).isNotNull();
        assertThat(annotation.value()).contains("VENDOR-PAYMENT_CANCEL");
    }

    private VendorPayment draftPayment() {
        return VendorPayment.createNew("VP-001", 1L, 1L, 1L,
                LocalDate.of(2026, 5, 15), BigDecimal.ONE, new BigDecimal("500.00"),
                "REF", "notes", List.of(new VendorPaymentLine(null, 1L, "VB-001",
                        new BigDecimal("500.00"), new BigDecimal("500.00"))));
    }
}
