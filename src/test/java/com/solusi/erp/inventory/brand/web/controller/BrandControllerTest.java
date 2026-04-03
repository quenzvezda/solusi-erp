package com.solusi.erp.inventory.brand.web.controller;

import com.solusi.erp.inventory.brand.web.controller.BrandController;
import com.solusi.erp.inventory.brand.application.usecase.command.CreateBrandUseCase;
import com.solusi.erp.inventory.brand.application.usecase.command.UpdateBrandUseCase;
import com.solusi.erp.inventory.brand.application.usecase.command.DeleteBrandUseCase;
import com.solusi.erp.inventory.brand.application.usecase.query.FindBrandsUseCase;
import com.solusi.erp.inventory.brand.application.usecase.query.GetBrandEditViewUseCase;
import com.solusi.erp.inventory.brand.application.usecase.query.GetBrandLookupUseCase;
import com.solusi.erp.inventory.brand.domain.model.Brand;
import com.solusi.erp.inventory.brand.web.dto.BrandSummaryResponse;
import com.solusi.erp.inventory.brand.web.mapper.BrandWebMapper;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class BrandControllerTest {

    @Test
    public void listShouldReturnListViewAndModel() {
        CreateBrandUseCase createBrandUseCase = mock(CreateBrandUseCase.class);
        UpdateBrandUseCase updateBrandUseCase = mock(UpdateBrandUseCase.class);
        DeleteBrandUseCase deleteBrandUseCase = mock(DeleteBrandUseCase.class);
        FindBrandsUseCase findBrandsUseCase = mock(FindBrandsUseCase.class);
        GetBrandEditViewUseCase getBrandEditViewUseCase = mock(GetBrandEditViewUseCase.class);
        GetBrandLookupUseCase getBrandLookupUseCase = mock(GetBrandLookupUseCase.class);
        BrandWebMapper webMapper = mock(BrandWebMapper.class);
        MessageSource messageSource = mock(MessageSource.class);

        BrandController controller = new BrandController(
            createBrandUseCase, updateBrandUseCase, deleteBrandUseCase,
            findBrandsUseCase, getBrandEditViewUseCase, getBrandLookupUseCase,
            webMapper, messageSource
        );

        Brand domainBrand = Brand.createNew("BR1", "Brand 1", "Note 1");
        com.solusi.erp.core.domain.model.Page<Brand> domainPage = new com.solusi.erp.core.domain.model.Page<>(List.of(domainBrand), 0, 1, 1L);
        when(findBrandsUseCase.execute(any(), any())).thenReturn(domainPage);

        BrandSummaryResponse summary = new BrandSummaryResponse();
        summary.setId(1L);
        summary.setCode("BR1");
        summary.setName("Brand 1");
        summary.setNote("Note 1");
        when(webMapper.toSummaryResponse(any(Brand.class))).thenReturn(summary);

        org.springframework.data.domain.Pageable springPageable = org.springframework.data.domain.PageRequest.of(0, 20);
        Model model = new ExtendedModelMap();

        String view = controller.list(null, springPageable, model);

        assertEquals("inventory/brands/list", view);
        Object pageObj = model.getAttribute("page");
        assertThat(pageObj).isInstanceOf(org.springframework.data.domain.Page.class);
        org.springframework.data.domain.Page<?> springPage = (org.springframework.data.domain.Page<?>) pageObj;
        assertEquals(1, springPage.getTotalElements());
        Object first = springPage.getContent().get(0);
        assertThat(first).isInstanceOf(BrandSummaryResponse.class);
        assertEquals("BR1", ((BrandSummaryResponse) first).getCode());
    }
}
