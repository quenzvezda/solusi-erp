package com.solusi.erp.inventory.goodsreceipt.web.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.inventory.container.domain.port.ContainerLookupProvider;
import com.solusi.erp.inventory.facility.domain.port.FacilityLookupProvider;
import com.solusi.erp.inventory.goodsreceipt.application.usecase.command.GoodsReceiptLineCommand;
import com.solusi.erp.inventory.goodsreceipt.domain.model.GoodsReceipt;
import com.solusi.erp.inventory.goodsreceipt.domain.model.GoodsReceiptLine;
import com.solusi.erp.inventory.goodsreceipt.domain.model.GoodsReceiptReferenceType;
import com.solusi.erp.inventory.goodsreceipt.domain.port.GoodsReceiptReferenceLookupProvider;
import com.solusi.erp.inventory.goodsreceipt.web.dto.GoodsReceiptDetailResponse;
import com.solusi.erp.inventory.goodsreceipt.web.dto.GoodsReceiptLineDetailResponse;
import com.solusi.erp.inventory.goodsreceipt.web.dto.GoodsReceiptSaveLineRequest;
import com.solusi.erp.inventory.product.domain.port.ProductLookupProvider;
import com.solusi.erp.inventory.uom.domain.port.UomLookupProvider;
import com.solusi.erp.master.currency.domain.port.CurrencyLookupProvider;
import com.solusi.erp.master.party.domain.port.PartyLookupProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GoodsReceiptWebMapperTest {

    @Mock
    private ProductLookupProvider productLookupProvider;

    @Mock
    private UomLookupProvider uomLookupProvider;

    @Mock
    private ContainerLookupProvider containerLookupProvider;

    @Mock
    private GoodsReceiptReferenceLookupProvider referenceLookupProvider;

    @Mock
    private PartyLookupProvider partyLookupProvider;

    @Mock
    private FacilityLookupProvider facilityLookupProvider;

    @Mock
    private CurrencyLookupProvider currencyLookupProvider;

    private GoodsReceiptWebMapper mapper;

    @BeforeEach
    void setUp() {
        GoodsReceiptWebMapperImpl actualMapper = new GoodsReceiptWebMapperImpl();
        actualMapper.productLookupProvider = productLookupProvider;
        actualMapper.uomLookupProvider = uomLookupProvider;
        actualMapper.containerLookupProvider = containerLookupProvider;
        actualMapper.referenceLookupProvider = referenceLookupProvider;
        actualMapper.partyLookupProvider = partyLookupProvider;
        actualMapper.facilityLookupProvider = facilityLookupProvider;
        actualMapper.currencyLookupProvider = currencyLookupProvider;
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
        when(containerLookupProvider.resolve(55L)).thenReturn(new LookupDto(55L, "Container 55", "BIN-DEMO-A01-01", null));

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
        assertThat(response.getContainerCode()).isEqualTo("BIN-DEMO-A01-01");
    }

    @Test
    @DisplayName("toDetailResponse resolves referenceCode via reference lookup provider")
    void toDetailResponseResolvesReferenceCodeViaReferenceLookupProvider() {
        GoodsReceipt domain = mock(GoodsReceipt.class);
        when(domain.getReferenceType()).thenReturn(GoodsReceiptReferenceType.PURCHASE_ORDER);
        when(domain.getReferenceId()).thenReturn(5L);
        when(referenceLookupProvider.resolveReferenceCode(GoodsReceiptReferenceType.PURCHASE_ORDER, 5L))
                .thenReturn("PO-202604-00005");

        GoodsReceiptDetailResponse response = mapper.toDetailResponse(domain);

        assertThat(response.getReferenceCode()).isEqualTo("PO-202604-00005");
        verify(referenceLookupProvider).resolveReferenceCode(GoodsReceiptReferenceType.PURCHASE_ORDER, 5L);
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
