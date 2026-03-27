package com.solusi.erp.inventory.product.web.controller;

import com.solusi.erp.inventory.product.web.controller.ProductController;
import com.solusi.erp.inventory.product.application.usecase.command.CreateProductUseCase;
import com.solusi.erp.inventory.product.application.usecase.command.UpdateProductUseCase;
import com.solusi.erp.inventory.product.application.usecase.command.DeleteProductUseCase;
import com.solusi.erp.inventory.product.application.usecase.query.FindProductsUseCase;
import com.solusi.erp.inventory.product.application.usecase.query.GetProductEditViewUseCase;
import com.solusi.erp.inventory.product.application.usecase.query.GetProductLookupUseCase;
import com.solusi.erp.inventory.product.application.usecase.query.GetProductUseCase;
import com.solusi.erp.inventory.product.domain.model.Product;
import com.solusi.erp.inventory.product.web.dto.ProductSummaryResponse;
import com.solusi.erp.inventory.product.web.mapper.ProductWebMapper;
import com.solusi.erp.inventory.uom.application.usecase.query.GetUomLookupUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ProductControllerTest {

    @Test
    public void listShouldReturnListViewAndModel() {
        CreateProductUseCase createProductUseCase = mock(CreateProductUseCase.class);
        UpdateProductUseCase updateProductUseCase = mock(UpdateProductUseCase.class);
        DeleteProductUseCase deleteProductUseCase = mock(DeleteProductUseCase.class);
        FindProductsUseCase findProductsUseCase = mock(FindProductsUseCase.class);
        GetProductEditViewUseCase getProductEditViewUseCase = mock(GetProductEditViewUseCase.class);
        GetProductLookupUseCase getProductLookupUseCase = mock(GetProductLookupUseCase.class);
        GetProductUseCase getProductUseCase = mock(GetProductUseCase.class);
        GetUomLookupUseCase getUomLookupUseCase = mock(GetUomLookupUseCase.class);
        ProductWebMapper webMapper = mock(ProductWebMapper.class);
        MessageSource messageSource = mock(MessageSource.class);

        ProductController controller = new ProductController(
            findProductsUseCase, getProductUseCase, createProductUseCase,
            updateProductUseCase, deleteProductUseCase, getProductEditViewUseCase,
            getProductLookupUseCase, getUomLookupUseCase, webMapper, messageSource
        );

        Product domain = Product.createNew(
            "PR1", "Product 1", "12345", "Note 1",
            1L, 1L, 1L, "", true, false,
            BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
            1L, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 1L
        );
        com.solusi.erp.core.domain.model.Page<Product> domainPage = new com.solusi.erp.core.domain.model.Page<>(List.of(domain), 0, 1, 1L);
        when(findProductsUseCase.execute(any(), any())).thenReturn(domainPage);

        ProductSummaryResponse summary = new ProductSummaryResponse();
        summary.setId(1L);
        summary.setCode("PR1");
        summary.setName("Product 1");
        summary.setBarcode("12345");
        summary.setNote("Note 1");
        summary.setCategoryId(1L);
        summary.setCategoryName("Category 1");
        summary.setCategoryType("STOCK");
        summary.setUomCode("PCS");
        summary.setBrandName("Brand 1");
        summary.setIsActive(Boolean.TRUE);
        summary.setIsSerialized(Boolean.FALSE);
        when(webMapper.toSummaryResponse(any(Product.class))).thenReturn(summary);

        org.springframework.data.domain.Pageable springPageable = org.springframework.data.domain.PageRequest.of(0, 20);
        Model model = new ExtendedModelMap();

        String view = controller.list(null, springPageable, model);

        assertEquals("inventory/products/list", view);
        Object pageObj = model.getAttribute("page");
        assertThat(pageObj).isInstanceOf(org.springframework.data.domain.Page.class);
        org.springframework.data.domain.Page<?> springPage = (org.springframework.data.domain.Page<?>) pageObj;
        assertEquals(1, springPage.getTotalElements());
        Object first = springPage.getContent().get(0);
        assertThat(first).isInstanceOf(ProductSummaryResponse.class);
        assertEquals("PR1", ((ProductSummaryResponse) first).getCode());
    }
}
