package com.solusi.erp.inventory.goodsissue.web.mapper;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.core.mapper.AuditMapperHelper;
import com.solusi.erp.inventory.container.domain.port.ContainerLookupProvider;
import com.solusi.erp.inventory.facility.domain.port.FacilityLookupProvider;
import com.solusi.erp.inventory.goodsissue.application.usecase.command.GoodsIssueLineCommand;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssue;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssueLine;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssuePartyType;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssueReferenceType;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssueStatus;
import com.solusi.erp.inventory.goodsissue.domain.port.GoodsIssueReferenceLookupProvider;
import com.solusi.erp.inventory.goodsissue.web.dto.GoodsIssueDetailResponse;
import com.solusi.erp.inventory.goodsissue.web.dto.GoodsIssueLineDetailResponse;
import com.solusi.erp.inventory.goodsissue.web.dto.GoodsIssueSaveLineRequest;
import com.solusi.erp.inventory.grid.domain.port.GridLookupProvider;
import com.solusi.erp.inventory.product.domain.port.ProductLookupProvider;
import com.solusi.erp.inventory.uom.domain.port.UomLookupProvider;
import com.solusi.erp.master.currency.domain.port.CurrencyLookupProvider;
import com.solusi.erp.master.party.domain.port.PartyLookupProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GoodsIssueWebMapperTest {

    @Mock private AuditMapperHelper auditMapperHelper;
    @Mock private PartyLookupProvider partyLookupProvider;
    @Mock private FacilityLookupProvider facilityLookupProvider;
    @Mock private GridLookupProvider gridLookupProvider;
    @Mock private ContainerLookupProvider containerLookupProvider;
    @Mock private CurrencyLookupProvider currencyLookupProvider;
    @Mock private ProductLookupProvider productLookupProvider;
    @Mock private UomLookupProvider uomLookupProvider;
    @Mock private GoodsIssueReferenceLookupProvider referenceLookupProvider;

    private GoodsIssueWebMapper mapper;

    @BeforeEach
    void setUp() {
        GoodsIssueWebMapperImpl actualMapper = new GoodsIssueWebMapperImpl();
        actualMapper.auditMapperHelper = auditMapperHelper;
        actualMapper.partyLookupProvider = partyLookupProvider;
        actualMapper.facilityLookupProvider = facilityLookupProvider;
        actualMapper.gridLookupProvider = gridLookupProvider;
        actualMapper.containerLookupProvider = containerLookupProvider;
        actualMapper.currencyLookupProvider = currencyLookupProvider;
        actualMapper.productLookupProvider = productLookupProvider;
        actualMapper.uomLookupProvider = uomLookupProvider;
        actualMapper.referenceLookupProvider = referenceLookupProvider;
        mapper = actualMapper;
    }

    @Test
    void toLineCommand_mapsSnapshotAndValuationFields() {
        GoodsIssueSaveLineRequest request = new GoodsIssueSaveLineRequest();
        request.setId(11L);
        request.setReferenceLineId(22L);
        request.setProductId(33L);
        request.setQuantityIssued(new BigDecimal("2.0000"));
        request.setBaseQuantity(new BigDecimal("2.0000"));
        request.setFacilityId(3L);
        request.setGridId(4L);
        request.setContainerId(5L);
        request.setUnitCost(new BigDecimal("150.000000"));
        request.setValuationRefType("GOODS_RECEIPT");
        request.setValuationRefId(301L);
        request.setValuationRefLineId(401L);

        GoodsIssueLineCommand command = mapper.toLineCommand(request);

        assertThat(command.referenceLineId()).isEqualTo(22L);
        assertThat(command.gridId()).isEqualTo(4L);
        assertThat(command.valuationRefType()).isEqualTo("GOODS_RECEIPT");
        assertThat(command.valuationRefId()).isEqualTo(301L);
        assertThat(command.valuationRefLineId()).isEqualTo(401L);
    }

    @Test
    void toDetailResponse_resolvesHeaderAndReferenceLookups() {
        GoodsIssue issue = issue();
        when(partyLookupProvider.resolve(11L)).thenReturn(new LookupDto(11L, "Supplier A", "SUP-A", null));
        when(facilityLookupProvider.resolve(3L)).thenReturn(new LookupDto(3L, "Main WH", "MAIN", null));
        when(currencyLookupProvider.resolve(1L)).thenReturn(new LookupDto(1L, "Rupiah", "IDR", null));
        when(referenceLookupProvider.resolveReferenceCode(GoodsIssueReferenceType.PURCHASE_RETURN, 70L))
                .thenReturn("PRTN-0070");

        GoodsIssueDetailResponse response = mapper.toDetailResponse(issue);

        assertThat(response.getPartyName()).isEqualTo("Supplier A");
        assertThat(response.getFacilityName()).isEqualTo("Main WH");
        assertThat(response.getCurrencyCode()).isEqualTo("IDR");
        assertThat(response.getReferenceCode()).isEqualTo("PRTN-0070");
        verify(referenceLookupProvider).resolveReferenceCode(GoodsIssueReferenceType.PURCHASE_RETURN, 70L);
    }

    @Test
    void toLineDetailResponse_resolvesTrinityLookupData() {
        GoodsIssueLine line = line();
        when(productLookupProvider.resolve(201L)).thenReturn(new LookupDto(201L, "Product A", "SKU-A", null));
        when(uomLookupProvider.resolve(1L)).thenReturn(new LookupDto(1L, "Pieces", "PCS", null));
        when(facilityLookupProvider.resolve(3L)).thenReturn(new LookupDto(3L, "Main WH", "MAIN", null));
        when(gridLookupProvider.resolve(4L)).thenReturn(new LookupDto(4L, "Grid A", "A01", null));
        when(containerLookupProvider.resolve(5L)).thenReturn(new LookupDto(5L, "Bin A", "BIN-A", null));

        GoodsIssueLineDetailResponse response = mapper.toLineDetailResponse(line);

        assertThat(response.getProductName()).isEqualTo("Product A");
        assertThat(response.getProductCode()).isEqualTo("SKU-A");
        assertThat(response.getUomCode()).isEqualTo("PCS");
        assertThat(response.getFacilityCode()).isEqualTo("MAIN");
        assertThat(response.getGridCode()).isEqualTo("A01");
        assertThat(response.getContainerCode()).isEqualTo("BIN-A");
    }

    private static GoodsIssue issue() {
        return new GoodsIssue(
                new AuditMetadata(7L, 1L, null, null, null, null),
                "GI-202606-00001",
                LocalDate.of(2026, 6, 1),
                GoodsIssueReferenceType.PURCHASE_RETURN,
                70L,
                null,
                11L,
                GoodsIssuePartyType.SUPPLIER,
                3L,
                1L,
                BigDecimal.ONE,
                GoodsIssueStatus.DRAFT,
                "note",
                List.of(line())
        );
    }

    private static GoodsIssueLine line() {
        return GoodsIssueLine.prefill(
                101L, 201L, false,
                new BigDecimal("2.0000"), 1L, new BigDecimal("2.0000"),
                3L, 4L, 5L, "SN-001",
                new BigDecimal("150.000000"), new BigDecimal("300.0000"),
                BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("300.0000"),
                "GOODS_RECEIPT", 301L, 401L
        );
    }
}
