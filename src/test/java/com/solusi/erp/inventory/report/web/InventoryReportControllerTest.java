package com.solusi.erp.inventory.report.web;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.inventory.container.domain.port.ContainerLookupProvider;
import com.solusi.erp.inventory.product.domain.port.ProductLookupProvider;
import com.solusi.erp.inventory.report.web.controller.InventoryReportController;
import com.solusi.erp.inventory.report.web.dto.InventoryMovementResponse;
import com.solusi.erp.inventory.report.web.dto.LocationStockDetailResponse;
import com.solusi.erp.inventory.report.web.dto.ProductStockSummaryResponse;
import com.solusi.erp.inventory.report.web.dto.StockCardFilter;
import com.solusi.erp.inventory.product.application.usecase.query.GetProductUseCase;
import com.solusi.erp.inventory.product.domain.model.Product;
import com.solusi.erp.inventory.product.web.dto.ProductDetailResponse;
import com.solusi.erp.inventory.product.web.mapper.ProductWebMapper;
import com.solusi.erp.inventory.report.application.usecase.query.GetOnHandDetailUseCase;
import com.solusi.erp.inventory.report.application.usecase.query.GetOnHandSummaryUseCase;
import com.solusi.erp.inventory.report.application.usecase.query.GetStockCardUseCase;
import com.solusi.erp.inventory.stock.domain.model.MovementType;
import com.solusi.erp.inventory.stock.domain.model.ReferenceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("InventoryReportController Tests")
class InventoryReportControllerTest {

    @Mock private GetOnHandSummaryUseCase getOnHandSummaryUseCase;
    @Mock private GetOnHandDetailUseCase getOnHandDetailUseCase;
    @Mock private GetStockCardUseCase getStockCardUseCase;
    @Mock private GetProductUseCase getProductUseCase;
    @Mock private ProductWebMapper webMapper;
    @Mock private ProductLookupProvider productLookupProvider;
    @Mock private ContainerLookupProvider containerLookupProvider;

    private InventoryReportController controller;

    @BeforeEach
    void setUp() {
        controller = new InventoryReportController(
                getOnHandSummaryUseCase,
                getOnHandDetailUseCase,
                getStockCardUseCase,
                getProductUseCase,
                webMapper,
                productLookupProvider,
                containerLookupProvider);
    }

    @Test
    @DisplayName("onHandList returns list view with page and search attributes")
    void onHandList_returnsViewWithModel() {
        Pageable pageable = PageRequest.of(0, 10);
        ProductStockSummaryResponse summary = ProductStockSummaryResponse.builder()
                .productId(1L).productCode("P001").build();
        org.springframework.data.domain.Page<ProductStockSummaryResponse> page =
                new PageImpl<>(List.of(summary), pageable, 1);

        when(getOnHandSummaryUseCase.execute("widget", pageable)).thenReturn(page);

        Model model = new ExtendedModelMap();
        String view = controller.onHandList("widget", pageable, model);

        assertEquals("inventory/reports/on-hand/list", view);
        assertThat(model.getAttribute("page")).isEqualTo(page);
        assertThat(model.getAttribute("search")).isEqualTo("widget");
        verify(getOnHandSummaryUseCase).execute("widget", pageable);
    }

    @Test
    @DisplayName("onHandDetail returns detail view with product and details")
    void onHandDetail_returnsViewWithModel() {
        Product product = mock(Product.class);
        ProductDetailResponse detailResponse = new ProductDetailResponse();
        detailResponse.setCode("P001");
        LocationStockDetailResponse locationDetail = LocationStockDetailResponse.builder()
                .facilityName("Main WH").build();

        when(getProductUseCase.execute(1L)).thenReturn(Optional.of(product));
        when(webMapper.toDetailResponse(product)).thenReturn(detailResponse);
        when(getOnHandDetailUseCase.execute(1L)).thenReturn(List.of(locationDetail));

        Model model = new ExtendedModelMap();
        String view = controller.onHandDetail(1L, model);

        assertEquals("inventory/reports/on-hand/detail", view);
        assertThat(model.getAttribute("product")).isEqualTo(detailResponse);
        assertThat(model.getAttribute("details")).isEqualTo(List.of(locationDetail));
        verify(getOnHandDetailUseCase).execute(1L);
    }

    @Test
    @DisplayName("stockCard returns list view with page, enum dropdowns, and autocomplete prefill")
    void stockCard_returnsViewWithModel() {
        Pageable pageable = PageRequest.of(0, 20);
        StockCardFilter filter = new StockCardFilter();
        filter.setProductId(10L);
        filter.setContainerId(20L);
        InventoryMovementResponse movResponse = InventoryMovementResponse.builder().build();
        org.springframework.data.domain.Page<InventoryMovementResponse> page =
                new PageImpl<>(List.of(movResponse), pageable, 1);

        LookupDto productLookup = new LookupDto(10L, "Widget", "P001");
        LookupDto containerLookup = new LookupDto(20L, "BIN-A1", "Rack A");

        when(getStockCardUseCase.execute(filter, pageable)).thenReturn(page);
        when(productLookupProvider.resolve(10L)).thenReturn(productLookup);
        when(containerLookupProvider.resolve(20L)).thenReturn(containerLookup);

        Model model = new ExtendedModelMap();
        String view = controller.stockCard(filter, pageable, model);

        assertEquals("inventory/reports/stock-card/list", view);
        assertThat(model.getAttribute("page")).isEqualTo(page);
        assertThat(model.getAttribute("movementTypes")).isEqualTo(MovementType.values());
        assertThat(model.getAttribute("referenceTypes")).isEqualTo(ReferenceType.values());
        assertThat((Map<String, Object>) model.getAttribute("filterUI"))
                .containsEntry("productText", "Widget")
                .containsEntry("productSubtext", "P001")
                .containsEntry("containerText", "BIN-A1")
                .containsEntry("containerSubtext", "Rack A");
        assertThat(model.containsAttribute("products")).isFalse();
        assertThat(model.containsAttribute("containers")).isFalse();
        verify(getStockCardUseCase).execute(filter, pageable);
        verify(productLookupProvider).resolve(10L);
        verify(containerLookupProvider).resolve(20L);
    }

    @Test
    @DisplayName("stockCard requires STOCK-CARD_READ authority")
    void stockCard_requiresStockCardReadAuthority() throws NoSuchMethodException {
        PreAuthorize annotation = InventoryReportController.class
                .getMethod("stockCard", StockCardFilter.class, Pageable.class, Model.class)
                .getAnnotation(PreAuthorize.class);

        assertThat(annotation).isNotNull();
        assertThat(annotation.value()).isEqualTo("hasAuthority('STOCK-CARD_READ')");
    }
}
