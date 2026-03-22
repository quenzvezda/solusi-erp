package com.solusi.erp.inventory.controller;

import com.solusi.erp.inventory.dto.UomConversionLookupDto;
import com.solusi.erp.inventory.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class InventoryLookupControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ProductService productService;

    @Mock
    private FacilityService facilityService;

    @Mock
    private GridService gridService;

    @Mock
    private ContainerService containerService;

    @Mock
    private ProductCategoryService categoryService;

    @Mock
    private BrandService brandService;

    @Mock
    private ProductUomConversionService uomConversionService;

    @InjectMocks
    private InventoryLookupController controller;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void testLookupUomConversions() throws Exception {
        Long productId = 1L;
        List<UomConversionLookupDto> mockConversions = List.of(
                new UomConversionLookupDto(10L, "Pieces", "PCS", new BigDecimal("1.0000"), true),
                new UomConversionLookupDto(20L, "Box", "BOX", new BigDecimal("10.0000"), false)
        );

        when(uomConversionService.getConversions(productId)).thenReturn(mockConversions);

        mockMvc.perform(get("/api/lookup/inventory/uom-conversions")
                        .param("productId", productId.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].uomId").value(10))
                .andExpect(jsonPath("$[0].uomName").value("Pieces"))
                .andExpect(jsonPath("$[0].conversionFactor").value(1.0))
                .andExpect(jsonPath("$[0].isBase").value(true))
                .andExpect(jsonPath("$[1].uomId").value(20))
                .andExpect(jsonPath("$[1].uomName").value("Box"))
                .andExpect(jsonPath("$[1].conversionFactor").value(10.0))
                .andExpect(jsonPath("$[1].isBase").value(false));
    }
}
