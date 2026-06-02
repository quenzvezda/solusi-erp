package com.solusi.erp.purchasing.purchasereturn.web.mapper;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.core.mapper.AuditMapperHelper;
import com.solusi.erp.inventory.container.domain.port.ContainerLookupProvider;
import com.solusi.erp.inventory.facility.domain.port.FacilityLookupProvider;
import com.solusi.erp.inventory.grid.domain.port.GridLookupProvider;
import com.solusi.erp.inventory.product.domain.port.ProductLookupProvider;
import com.solusi.erp.inventory.uom.domain.port.UomLookupProvider;
import com.solusi.erp.master.currency.domain.port.CurrencyLookupProvider;
import com.solusi.erp.master.party.domain.port.PartyLookupProvider;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.command.PurchaseReturnLineCommand;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.query.EligibleGoodsReceiptRow;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.query.GetPurchaseReturnCreateViewUseCase.PurchaseReturnCreateView;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.query.ReturnableGrLineSlice;
import com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturn;
import com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturnLine;
import com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturnReason;
import com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturnStatus;
import com.solusi.erp.purchasing.purchasereturn.web.dto.PurchaseReturnDetailResponse;
import com.solusi.erp.purchasing.purchasereturn.web.dto.PurchaseReturnSaveLineRequest;
import com.solusi.erp.purchasing.purchasereturn.web.dto.PurchaseReturnSaveRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PurchaseReturnWebMapperTest {

    @Mock private AuditMapperHelper auditMapperHelper;
    @Mock private PartyLookupProvider partyLookupProvider;
    @Mock private FacilityLookupProvider facilityLookupProvider;
    @Mock private CurrencyLookupProvider currencyLookupProvider;
    @Mock private ProductLookupProvider productLookupProvider;
    @Mock private UomLookupProvider uomLookupProvider;
    @Mock private GridLookupProvider gridLookupProvider;
    @Mock private ContainerLookupProvider containerLookupProvider;

    private PurchaseReturnWebMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new PurchaseReturnWebMapper(
                auditMapperHelper, partyLookupProvider, facilityLookupProvider, currencyLookupProvider,
                productLookupProvider, uomLookupProvider, gridLookupProvider, containerLookupProvider);
    }

    @Test
    void toCreateRequest_prefillsCurrentContainerSliceWithZeroQuantity() {
        PurchaseReturnSaveRequest result = mapper.toCreateRequest(new PurchaseReturnCreateView(source(), List.of(slice())));

        assertThat(result.getGoodsReceiptId()).isEqualTo(1L);
        assertThat(result.getLines()).hasSize(1);
        PurchaseReturnSaveLineRequest line = result.getLines().get(0);
        assertThat(line.getSelectionKey()).isEqualTo("11:40");
        assertThat(line.getContainerCode()).isEqualTo("BIN");
        assertThat(line.getOutstandingQuantity()).isEqualByComparingTo("10");
        assertThat(line.getQuantity()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void toDetailResponse_mapsReasonSerialCurrentContainerTotalsAndGeneratedGi() {
        stubLookups();

        PurchaseReturnDetailResponse result = mapper.toDetailResponse(purchaseReturn());

        assertThat(result.getReason()).isEqualTo(PurchaseReturnReason.DAMAGED);
        assertThat(result.getGeneratedGoodsIssueId()).isEqualTo(500L);
        assertThat(result.getTotalQuantity()).isEqualByComparingTo("2");
        assertThat(result.getTotalAmount()).isEqualByComparingTo("200");
        assertThat(result.getLines().get(0).getContainerCode()).isEqualTo("BIN");
        assertThat(result.getLines().get(0).getSerialNumbers()).isEqualTo("SER-001,SER-002");
        assertThat(result.getLines().get(0).getValuationReferenceLineId()).isEqualTo(11L);
    }

    @Test
    void toLineCommands_persistsOnlyPositiveRows() {
        PurchaseReturnSaveLineRequest zero = new PurchaseReturnSaveLineRequest();
        zero.setQuantity(BigDecimal.ZERO);
        PurchaseReturnSaveLineRequest positive = new PurchaseReturnSaveLineRequest();
        positive.setGoodsReceiptLineId(11L);
        positive.setSerialized(false);
        positive.setQuantity(new BigDecimal("2"));
        positive.setBaseQuantity(new BigDecimal("2"));
        positive.setContainerId(40L);
        positive.setReason(PurchaseReturnReason.DAMAGED);

        List<PurchaseReturnLineCommand> result = mapper.toLineCommands(List.of(zero, positive));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).containerId()).isEqualTo(40L);
        assertThat(result.get(0).quantity()).isEqualByComparingTo("2");
    }

    private void stubLookups() {
        when(partyLookupProvider.resolve(3L)).thenReturn(new LookupDto(3L, "Supplier", "SUP"));
        when(facilityLookupProvider.resolve(30L)).thenReturn(new LookupDto(30L, "Main", "MAIN"));
        when(currencyLookupProvider.resolve(5L)).thenReturn(new LookupDto(5L, "Rupiah", "IDR"));
        when(productLookupProvider.resolve(10L)).thenReturn(new LookupDto(10L, "Product", "P-001"));
        when(uomLookupProvider.resolve(20L)).thenReturn(new LookupDto(20L, "Piece", "PCS"));
        when(gridLookupProvider.resolve(35L)).thenReturn(new LookupDto(35L, "A", "A"));
        when(containerLookupProvider.resolve(40L)).thenReturn(new LookupDto(40L, "Bin", "BIN"));
    }

    private EligibleGoodsReceiptRow source() {
        return new EligibleGoodsReceiptRow(
                1L, "GR-001", 2L, "PO-001", 3L, "Supplier", LocalDate.of(2026, 6, 1),
                30L, "Main", 5L, "IDR", BigDecimal.ONE, 1, BigDecimal.TEN);
    }

    private ReturnableGrLineSlice slice() {
        return new ReturnableGrLineSlice(
                "11:40", 1L, 11L, 10L, "Product", "P-001", false, 20L, "Piece", "PCS",
                30L, "Main", 35L, "Grid A", "A", 40L, "Bin", "BIN", BigDecimal.TEN, "GOODS_RECEIPT",
                1L, 11L, new BigDecimal("100"), new BigDecimal("1000"), BigDecimal.ZERO, BigDecimal.ZERO);
    }

    private PurchaseReturn purchaseReturn() {
        return PurchaseReturn.reconstitute(
                new AuditMetadata(1L, 0L, null, null, null, null),
                "PRT-001", LocalDate.of(2026, 6, 1), "GOODS_RECEIPT", 1L, "GR-001",
                2L, "PO-001", 3L, 30L, 5L, BigDecimal.ONE, PurchaseReturnStatus.CONFIRMED,
                PurchaseReturnReason.DAMAGED, null, 99L, 500L, List.of(line()));
    }

    private PurchaseReturnLine line() {
        return PurchaseReturnLine.reconstitute(
                new AuditMetadata(100L, 0L, null, null, null, null),
                11L, 10L, true, new BigDecimal("2"), 20L, new BigDecimal("2"), 30L, 35L, 40L,
                "SER-001,SER-002", PurchaseReturnReason.DAMAGED, null, "GOODS_RECEIPT", 1L, 11L,
                new BigDecimal("100"), new BigDecimal("200"), BigDecimal.ZERO, BigDecimal.ZERO);
    }
}
