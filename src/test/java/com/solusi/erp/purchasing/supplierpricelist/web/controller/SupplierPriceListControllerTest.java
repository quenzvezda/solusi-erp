package com.solusi.erp.purchasing.supplierpricelist.web.controller;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.inventory.product.domain.port.ProductLookupProvider;
import com.solusi.erp.inventory.uom.domain.port.UomLookupProvider;
import com.solusi.erp.master.currency.domain.port.CurrencyLookupProvider;
import com.solusi.erp.master.party.domain.port.PartyLookupProvider;
import com.solusi.erp.purchasing.supplierpricelist.application.usecase.command.*;
import com.solusi.erp.purchasing.supplierpricelist.application.usecase.query.*;
import com.solusi.erp.purchasing.supplierpricelist.domain.model.SupplierPriceList;
import com.solusi.erp.purchasing.supplierpricelist.web.dto.SupplierPriceListDetailResponse;
import com.solusi.erp.purchasing.supplierpricelist.web.dto.SupplierPriceListSaveRequest;
import com.solusi.erp.purchasing.supplierpricelist.web.dto.SupplierPriceListSummaryResponse;
import com.solusi.erp.purchasing.supplierpricelist.web.mapper.SupplierPriceListWebMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("SupplierPriceListController Tests")
public class SupplierPriceListControllerTest {

    private SupplierPriceListController buildController(
            FindSupplierPriceListsUseCase findUc,
            GetSupplierPriceListEditViewUseCase editViewUc,
            SupplierPriceListWebMapper webMapper,
            PartyLookupProvider partyLookup,
            ProductLookupProvider productLookup,
            UomLookupProvider uomLookup,
            CurrencyLookupProvider currencyLookup) {
        return new SupplierPriceListController(
            mock(CreateSupplierPriceListUseCase.class),
            mock(UpdateSupplierPriceListUseCase.class),
            mock(DeleteSupplierPriceListUseCase.class),
            findUc,
            editViewUc,
            webMapper,
            mock(MessageSource.class),
            partyLookup,
            productLookup,
            uomLookup,
            currencyLookup
        );
    }

    @Test
    @DisplayName("list returns list view with page model")
    public void listShouldReturnListViewAndModel() {
        FindSupplierPriceListsUseCase findUc = mock(FindSupplierPriceListsUseCase.class);
        SupplierPriceListWebMapper webMapper = mock(SupplierPriceListWebMapper.class);

        SupplierPriceListController controller = buildController(
            findUc, mock(GetSupplierPriceListEditViewUseCase.class), webMapper,
            mock(PartyLookupProvider.class), mock(ProductLookupProvider.class),
            mock(UomLookupProvider.class), mock(CurrencyLookupProvider.class));

        AuditMetadata metadata = new AuditMetadata(1L, 1L, null, null, null, null);
        SupplierPriceList domainSpl = new SupplierPriceList(metadata, "SPL-001",
            1L, 2L, 3L, 4L,
            new BigDecimal("100.0000"), new BigDecimal("1.0000"),
            LocalDate.of(2026, 7, 1), null, null, true);

        com.solusi.erp.core.domain.model.Page<SupplierPriceList> domainPage =
            new com.solusi.erp.core.domain.model.Page<>(List.of(domainSpl), 0, 20, 1L);
        when(findUc.execute(any(), any())).thenReturn(domainPage);

        SupplierPriceListSummaryResponse summary = new SupplierPriceListSummaryResponse();
        summary.setId(1L);
        summary.setCode("SPL-001");
        when(webMapper.toSummaryResponse(any(SupplierPriceList.class))).thenReturn(summary);

        org.springframework.data.domain.Pageable springPageable =
            org.springframework.data.domain.PageRequest.of(0, 20);
        Model model = new ExtendedModelMap();

        String view = controller.list(null, springPageable, model);

        assertEquals("purchasing/supplier-price-lists/list", view);
        Object pageObj = model.getAttribute("page");
        assertThat(pageObj).isInstanceOf(org.springframework.data.domain.Page.class);
        org.springframework.data.domain.Page<?> springPage = (org.springframework.data.domain.Page<?>) pageObj;
        assertEquals(1, springPage.getTotalElements());
        Object first = springPage.getContent().get(0);
        assertThat(first).isInstanceOf(SupplierPriceListSummaryResponse.class);
        assertEquals("SPL-001", ((SupplierPriceListSummaryResponse) first).getCode());
    }

    @Test
    @DisplayName("showCreateForm returns form view with empty request")
    public void showCreateFormShouldReturnFormView() {
        SupplierPriceListController controller = buildController(
            mock(FindSupplierPriceListsUseCase.class), mock(GetSupplierPriceListEditViewUseCase.class),
            mock(SupplierPriceListWebMapper.class),
            mock(PartyLookupProvider.class), mock(ProductLookupProvider.class),
            mock(UomLookupProvider.class), mock(CurrencyLookupProvider.class));

        Model model = new ExtendedModelMap();
        String view = controller.showCreateForm(model);

        assertEquals("purchasing/supplier-price-lists/form", view);
        assertThat(model.getAttribute("splRequest")).isNotNull();
    }

    @Test
    @DisplayName("showEditForm returns form view with splUI containing Trinity Data")
    public void showEditFormShouldReturnFormViewWithSplUI() {
        GetSupplierPriceListEditViewUseCase editViewUc = mock(GetSupplierPriceListEditViewUseCase.class);
        SupplierPriceListWebMapper webMapper = mock(SupplierPriceListWebMapper.class);
        PartyLookupProvider partyLookup = mock(PartyLookupProvider.class);
        ProductLookupProvider productLookup = mock(ProductLookupProvider.class);
        UomLookupProvider uomLookup = mock(UomLookupProvider.class);
        CurrencyLookupProvider currencyLookup = mock(CurrencyLookupProvider.class);

        SupplierPriceListController controller = buildController(
            mock(FindSupplierPriceListsUseCase.class), editViewUc, webMapper,
            partyLookup, productLookup, uomLookup, currencyLookup);

        AuditMetadata metadata = new AuditMetadata(1L, 1L, null, null, null, null);
        SupplierPriceList domain = new SupplierPriceList(metadata, "SPL-001",
            1L, 2L, 3L, 4L,
            new BigDecimal("100.0000"), new BigDecimal("1.0000"),
            LocalDate.of(2026, 7, 1), null, null, true);

        when(editViewUc.execute(1L)).thenReturn(Optional.of(domain));
        when(webMapper.toSaveRequest(domain)).thenReturn(new SupplierPriceListSaveRequest());
        when(webMapper.toDetailResponse(domain)).thenReturn(new SupplierPriceListDetailResponse());

        when(partyLookup.resolve(1L)).thenReturn(new LookupDto(1L, "PT Supplier", "SUP-001", null));
        when(productLookup.resolve(2L)).thenReturn(new LookupDto(2L, "Widget A", "PRD-001", null));
        when(uomLookup.resolve(3L)).thenReturn(new LookupDto(3L, "Kilogram", "KG", null));
        when(currencyLookup.resolve(4L)).thenReturn(new LookupDto(4L, "US Dollar", "$ - USD", Map.of("symbol", "$")));

        Model model = new ExtendedModelMap();
        String view = controller.showEditForm(1L, model);

        assertEquals("purchasing/supplier-price-lists/form", view);

        @SuppressWarnings("unchecked")
        Map<String, Object> splUI = (Map<String, Object>) model.getAttribute("splUI");
        assertThat(splUI).isNotNull();
        assertEquals("PT Supplier", splUI.get("supplierText"));
        assertEquals("SUP-001", splUI.get("supplierSubtext"));
        assertEquals("Widget A", splUI.get("productText"));
        assertEquals("PRD-001", splUI.get("productSubtext"));
        assertEquals("Kilogram", splUI.get("uomText"));
        assertEquals("KG", splUI.get("uomSubtext"));
        assertEquals("US Dollar", splUI.get("currencyText"));
        assertEquals("$ - USD", splUI.get("currencySubtext"));
    }

    @Test
    @DisplayName("update forwards supplierId from request to use case")
    public void updateShouldForwardSupplierIdToUseCase() {
        UpdateSupplierPriceListUseCase updateUc = mock(UpdateSupplierPriceListUseCase.class);
        SupplierPriceListWebMapper webMapper = mock(SupplierPriceListWebMapper.class);
        MessageSource messageSource = mock(MessageSource.class);

        SupplierPriceListController controller = new SupplierPriceListController(
            mock(CreateSupplierPriceListUseCase.class),
            updateUc,
            mock(DeleteSupplierPriceListUseCase.class),
            mock(FindSupplierPriceListsUseCase.class),
            mock(GetSupplierPriceListEditViewUseCase.class),
            webMapper,
            messageSource,
            mock(PartyLookupProvider.class),
            mock(ProductLookupProvider.class),
            mock(UomLookupProvider.class),
            mock(CurrencyLookupProvider.class)
        );

        SupplierPriceListSaveRequest request = new SupplierPriceListSaveRequest();
        request.setSupplierId(99L);
        request.setProductId(2L);
        request.setUomId(3L);
        request.setCurrencyId(4L);
        request.setUnitPrice(new BigDecimal("100.0000"));
        request.setMinQuantity(new BigDecimal("1.0000"));
        request.setEffectiveFrom(LocalDate.of(2026, 7, 1));
        request.setEffectiveTo(null);
        request.setNote("Updated");
        request.setActive(true);

        AuditMetadata metadata = new AuditMetadata(1L, 1L, null, null, null, null);
        SupplierPriceList domain = new SupplierPriceList(metadata, "SPL-001",
            99L, 2L, 3L, 4L,
            new BigDecimal("100.0000"), new BigDecimal("1.0000"),
            LocalDate.of(2026, 7, 1), null, "Updated", true);

        when(updateUc.execute(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), anyBoolean()))
            .thenReturn(domain);
        when(webMapper.toDetailResponse(domain)).thenReturn(new SupplierPriceListDetailResponse());

        controller.update(1L, request);

        verify(updateUc).execute(1L, 99L, 2L, 3L, 4L,
            new BigDecimal("100.0000"), new BigDecimal("1.0000"),
            LocalDate.of(2026, 7, 1), null, "Updated", true);
    }
}
