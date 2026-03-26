package com.solusi.erp.inventory.productcategory;

import com.solusi.erp.inventory.productcategory.web.controller.ProductCategoryController;
import com.solusi.erp.inventory.productcategory.application.usecase.command.CreateProductCategoryUseCase;
import com.solusi.erp.inventory.productcategory.application.usecase.command.UpdateProductCategoryUseCase;
import com.solusi.erp.inventory.productcategory.application.usecase.command.DeleteProductCategoryUseCase;
import com.solusi.erp.inventory.productcategory.application.usecase.query.FindProductCategoriesUseCase;
import com.solusi.erp.inventory.productcategory.application.usecase.query.GetProductCategoryEditViewUseCase;
import com.solusi.erp.inventory.productcategory.application.usecase.query.GetProductCategoryLookupUseCase;
import com.solusi.erp.inventory.productcategory.domain.model.ProductCategory;
import com.solusi.erp.inventory.productcategory.web.dto.ProductCategorySummaryResponse;
import com.solusi.erp.inventory.productcategory.web.mapper.ProductCategoryWebMapper;
import com.solusi.erp.inventory.model.ProductCategoryType;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ProductCategoryControllerTest {

    @Test
    public void listShouldReturnListViewAndModel() {
        CreateProductCategoryUseCase createProductCategoryUseCase = mock(CreateProductCategoryUseCase.class);
        UpdateProductCategoryUseCase updateProductCategoryUseCase = mock(UpdateProductCategoryUseCase.class);
        DeleteProductCategoryUseCase deleteProductCategoryUseCase = mock(DeleteProductCategoryUseCase.class);
        FindProductCategoriesUseCase findProductCategoriesUseCase = mock(FindProductCategoriesUseCase.class);
        GetProductCategoryEditViewUseCase getProductCategoryEditViewUseCase = mock(GetProductCategoryEditViewUseCase.class);
        GetProductCategoryLookupUseCase getProductCategoryLookupUseCase = mock(GetProductCategoryLookupUseCase.class);
        ProductCategoryWebMapper webMapper = mock(ProductCategoryWebMapper.class);
        MessageSource messageSource = mock(MessageSource.class);

        ProductCategoryController controller = new ProductCategoryController(
            createProductCategoryUseCase, updateProductCategoryUseCase, deleteProductCategoryUseCase,
            findProductCategoriesUseCase, getProductCategoryEditViewUseCase, getProductCategoryLookupUseCase,
            webMapper, messageSource
        );

        ProductCategory domain = ProductCategory.createNew("CAT1", "Category 1", ProductCategoryType.STOCK, "Note 1");
        com.solusi.erp.core.domain.model.Page<ProductCategory> domainPage = new com.solusi.erp.core.domain.model.Page<>(List.of(domain), 0, 1, 1L);
        when(findProductCategoriesUseCase.execute(any(), any())).thenReturn(domainPage);

        ProductCategorySummaryResponse summary = new ProductCategorySummaryResponse();
        summary.setId(1L);
        summary.setCode("CAT1");
        summary.setName("Category 1");
        summary.setType(ProductCategoryType.STOCK);
        summary.setNote("Note 1");
        when(webMapper.toSummaryResponse(any(ProductCategory.class))).thenReturn(summary);

        org.springframework.data.domain.Pageable springPageable = org.springframework.data.domain.PageRequest.of(0, 20);
        Model model = new ExtendedModelMap();

        String view = controller.list(null, springPageable, model);

        assertEquals("inventory/product-categories/list", view);
        Object pageObj = model.getAttribute("page");
        assertThat(pageObj).isInstanceOf(org.springframework.data.domain.Page.class);
        org.springframework.data.domain.Page<?> springPage = (org.springframework.data.domain.Page<?>) pageObj;
        assertEquals(1, springPage.getTotalElements());
        Object first = springPage.getContent().get(0);
        assertThat(first).isInstanceOf(ProductCategorySummaryResponse.class);
        assertEquals("CAT1", ((ProductCategorySummaryResponse) first).getCode());
    }
}
