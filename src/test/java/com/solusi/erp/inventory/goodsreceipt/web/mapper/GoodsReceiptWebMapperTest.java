package com.solusi.erp.inventory.goodsreceipt.web.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.inventory.goodsreceipt.application.usecase.command.GoodsReceiptLineCommand;
import com.solusi.erp.inventory.goodsreceipt.domain.model.GoodsReceiptLine;
import com.solusi.erp.inventory.goodsreceipt.web.dto.GoodsReceiptLineDetailResponse;
import com.solusi.erp.inventory.goodsreceipt.web.dto.GoodsReceiptSaveLineRequest;
import com.solusi.erp.inventory.product.domain.port.ProductLookupProvider;
import com.solusi.erp.inventory.uom.domain.port.UomLookupProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GoodsReceiptWebMapperTest {

    @Mock
    private ProductLookupProvider productLookupProvider;

    @Mock
    private UomLookupProvider uomLookupProvider;

    private GoodsReceiptWebMapper mapper;

    @BeforeEach
    void setUp() {
        GoodsReceiptWebMapperImpl actualMapper = new GoodsReceiptWebMapperImpl();
        actualMapper.productLookupProvider = productLookupProvider;
        actualMapper.uomLookupProvider = uomLookupProvider;
        mapper = actualMapper;
    }

    @Test
    @DisplayName("toLineCommand maps referenceLineId from save request")
    void toLineCommandMapsReferenceLineId() {
        GoodsReceiptSaveLineRequest request = new GoodsReceiptSaveLineRequest();
        request.setId(11L);
        request.setReferenceLineId(22L);
        request.setProductId(33L);
        request.setSerialized(Boolean.TRUE);
        request.setQuantityReceived(new BigDecimal("4.5000"));
        request.setUomId(44L);
        request.setContainerId(55L);
        request.setSerialNumber("SN-001");

        GoodsReceiptLineCommand command = mapper.toLineCommand(request);

        assertThat(command.id()).isEqualTo(11L);
        assertThat(command.referenceLineId()).isEqualTo(22L);
        assertThat(command.productId()).isEqualTo(33L);
        assertThat(command.serialNumber()).isEqualTo("SN-001");
    }

    @Test
    @DisplayName("toLineDetailResponse preserves referenceLineId from domain line")
    void toLineDetailResponsePreservesReferenceLineId() {
        when(productLookupProvider.resolve(33L)).thenReturn(new LookupDto(33L, "Product 33", "P-33", null));
        when(uomLookupProvider.resolve(44L)).thenReturn(new LookupDto(44L, "Box", "BOX", null));

        GoodsReceiptLine line = GoodsReceiptLine.prefill(
                22L,
                33L,
                66L,
                Boolean.TRUE,
                new BigDecimal("4.5000"),
                44L,
                55L,
                new BigDecimal("10.00"),
                new BigDecimal("4.5000"),
                new BigDecimal("45.00"),
                new BigDecimal("45.00"),
                new BigDecimal("45.00"),
                "SN-001"
        );

        GoodsReceiptLineDetailResponse response = mapper.toLineDetailResponse(line);

        assertThat(response.getReferenceLineId()).isEqualTo(22L);
        assertThat(response.getProductId()).isEqualTo(33L);
        assertThat(response.getProductName()).isEqualTo("Product 33");
        assertThat(response.getUomCode()).isEqualTo("BOX");
    }

    @Test
    @DisplayName("line detail response keeps legacy poLineId JSON field alongside referenceLineId")
    void lineDetailResponseKeepsLegacyPoLineIdJsonField() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        GoodsReceiptLineDetailResponse response = new GoodsReceiptLineDetailResponse();
        response.setReferenceLineId(22L);
        response.setProductId(33L);

        String json = objectMapper.writeValueAsString(response);

        assertThat(json).contains("\"referenceLineId\":22");
        assertThat(json).contains("\"poLineId\":22");
    }
}
