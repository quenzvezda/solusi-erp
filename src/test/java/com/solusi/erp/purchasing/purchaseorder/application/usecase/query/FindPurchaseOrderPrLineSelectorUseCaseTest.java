package com.solusi.erp.purchasing.purchaseorder.application.usecase.query;

import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.inventory.product.domain.port.ProductLookupProvider;
import com.solusi.erp.inventory.uom.domain.port.UomLookupProvider;
import com.solusi.erp.purchasing.purchaserequisition.domain.model.PurchaseRequisitionPriority;
import com.solusi.erp.purchasing.purchaserequisition.domain.model.PurchaseRequisitionStatus;
import com.solusi.erp.purchasing.purchaserequisition.infrastructure.persistence.PurchaseRequisitionEntity;
import com.solusi.erp.purchasing.purchaserequisition.infrastructure.persistence.PurchaseRequisitionJpaRepository;
import com.solusi.erp.purchasing.purchaserequisition.infrastructure.persistence.PurchaseRequisitionLineEntity;
import com.solusi.erp.purchasing.purchaseorder.infrastructure.persistence.PrLineConsumptionRow;
import com.solusi.erp.purchasing.purchaseorder.infrastructure.persistence.PurchaseOrderJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FindPurchaseOrderPrLineSelectorUseCase Tests")
class FindPurchaseOrderPrLineSelectorUseCaseTest {

    @Mock
    private PurchaseRequisitionJpaRepository purchaseRequisitionJpaRepository;

    @Mock
    private PurchaseOrderJpaRepository purchaseOrderJpaRepository;

    @Mock
    private ProductLookupProvider productLookupProvider;

    @Mock
    private UomLookupProvider uomLookupProvider;

    private FindPurchaseOrderPrLineSelectorUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new FindPurchaseOrderPrLineSelectorUseCaseImpl(
                purchaseRequisitionJpaRepository,
                purchaseOrderJpaRepository,
                productLookupProvider,
                uomLookupProvider
        );
    }

    @Test
    @DisplayName("execute skips lines without ids and does not query consumption when all ids are missing")
    void execute_skipsLinesWithoutIdsAndConsumptionQuery() {
        PurchaseRequisitionLineEntity lineWithoutId = line(null, 10L, 1L, "10.0000", "100.00");

        when(purchaseRequisitionJpaRepository.findApprovedLinesForPoSelector(10L, null))
                .thenReturn(List.of(lineWithoutId));

        Page<PurchaseOrderPrLineSelectorRow> result = useCase.execute(10L, null, null, PageRequest.of(0, 10));

        assertThat(result.getContent()).isEmpty();
        verify(purchaseOrderJpaRepository, never()).sumConsumedByPrLineIds(anySet(), anySet());
    }

    @Test
    @DisplayName("execute excludes already selected PR line ids")
    void execute_excludesAlreadySelectedPrLines() {
        PurchaseRequisitionLineEntity line100 = line(100L, 10L, 1L, "10.0000", "100.00");
        PurchaseRequisitionLineEntity line101 = line(101L, 11L, 1L, "2.0000", "50.00");

        when(purchaseRequisitionJpaRepository.findApprovedLinesForPoSelector(10L, null))
                .thenReturn(List.of(line100, line101));
        when(purchaseOrderJpaRepository.sumConsumedByPrLineIds(Set.of(100L, 101L), FindPurchaseOrderPrLineSelectorUseCaseImpl.CONSUMING_STATUSES))
                .thenReturn(List.of());
        when(productLookupProvider.resolve(11L)).thenReturn(new LookupDto(11L, "Product B", "PROD-B"));
        when(uomLookupProvider.resolve(1L)).thenReturn(new LookupDto(1L, "PCS", "PCS"));

        Page<PurchaseOrderPrLineSelectorRow> result = useCase.execute(10L, null, List.of(100L), PageRequest.of(0, 10));

        assertThat(result.getContent()).extracting(PurchaseOrderPrLineSelectorRow::prLineId)
                .containsExactly(101L);
    }

    @Test
    @DisplayName("execute skips fully consumed lines")
    void execute_skipsFullyConsumedLines() {
        PurchaseRequisitionLineEntity line100 = line(100L, 10L, 1L, "10.0000", "100.00");

        when(purchaseRequisitionJpaRepository.findApprovedLinesForPoSelector(10L, null))
                .thenReturn(List.of(line100));
        when(purchaseOrderJpaRepository.sumConsumedByPrLineIds(Set.of(100L), FindPurchaseOrderPrLineSelectorUseCaseImpl.CONSUMING_STATUSES))
                .thenReturn(List.of(new PrLineConsumptionRow(100L, new BigDecimal("10.0000"))));

        Page<PurchaseOrderPrLineSelectorRow> result = useCase.execute(10L, null, List.of(), PageRequest.of(0, 10));

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("execute keeps rows when lookup providers return null")
    void execute_keepsRowsWhenLookupProvidersReturnNull() {
        PurchaseRequisitionLineEntity line100 = line(100L, 10L, 1L, "10.0000", "100.00");

        when(purchaseRequisitionJpaRepository.findApprovedLinesForPoSelector(10L, null))
                .thenReturn(List.of(line100));
        when(purchaseOrderJpaRepository.sumConsumedByPrLineIds(Set.of(100L), FindPurchaseOrderPrLineSelectorUseCaseImpl.CONSUMING_STATUSES))
                .thenReturn(List.of());
        when(productLookupProvider.resolve(10L)).thenReturn(null);
        when(uomLookupProvider.resolve(1L)).thenReturn(null);

        Page<PurchaseOrderPrLineSelectorRow> result = useCase.execute(10L, null, List.of(), PageRequest.of(0, 10));

        assertThat(result.getContent()).singleElement().satisfies(row -> {
            assertThat(row.productName()).isNull();
            assertThat(row.productSubtext()).isNull();
            assertThat(row.uomName()).isNull();
            assertThat(row.uomSubtext()).isNull();
        });
    }

    @Test
    @DisplayName("execute filters rows that do not match normalized keyword after lookup resolution")
    void execute_filtersRowsThatDoNotMatchKeyword() {
        PurchaseRequisitionLineEntity line100 = line(100L, 10L, 1L, "10.0000", "100.00");

        when(purchaseRequisitionJpaRepository.findApprovedLinesForPoSelector(10L, "missing"))
                .thenReturn(List.of(line100));
        when(purchaseOrderJpaRepository.sumConsumedByPrLineIds(Set.of(100L), FindPurchaseOrderPrLineSelectorUseCaseImpl.CONSUMING_STATUSES))
                .thenReturn(List.of());
        when(productLookupProvider.resolve(10L)).thenReturn(new LookupDto(10L, "Bearing 6204", "BRG-6204"));
        when(uomLookupProvider.resolve(1L)).thenReturn(new LookupDto(1L, "PCS", "PCS"));

        Page<PurchaseOrderPrLineSelectorRow> result = useCase.execute(10L, "  missing  ", List.of(), PageRequest.of(0, 10));

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("execute returns empty page when requested offset is beyond available rows")
    void execute_returnsEmptyPageWhenOffsetBeyondRows() {
        PurchaseRequisitionLineEntity line100 = line(100L, 10L, 1L, "10.0000", "100.00");

        when(purchaseRequisitionJpaRepository.findApprovedLinesForPoSelector(10L, null))
                .thenReturn(List.of(line100));
        when(purchaseOrderJpaRepository.sumConsumedByPrLineIds(Set.of(100L), FindPurchaseOrderPrLineSelectorUseCaseImpl.CONSUMING_STATUSES))
                .thenReturn(List.of());
        when(productLookupProvider.resolve(10L)).thenReturn(new LookupDto(10L, "Bearing 6204", "BRG-6204"));
        when(uomLookupProvider.resolve(1L)).thenReturn(new LookupDto(1L, "PCS", "PCS"));

        Page<PurchaseOrderPrLineSelectorRow> result = useCase.execute(10L, null, List.of(), PageRequest.of(1, 10));

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(1L);
    }

    @Test
    @DisplayName("execute returns remaining quantity and supports keyword filter on resolved product text")
    void execute_returnsRemainingQuantityAndKeywordMatchesResolvedProduct() {
        PurchaseRequisitionLineEntity line100 = line(100L, 10L, 1L, "10.0000", "100.00");

        when(purchaseRequisitionJpaRepository.findApprovedLinesForPoSelector(10L, "bearing"))
                .thenReturn(List.of(line100));
        when(purchaseOrderJpaRepository.sumConsumedByPrLineIds(Set.of(100L), FindPurchaseOrderPrLineSelectorUseCaseImpl.CONSUMING_STATUSES))
                .thenReturn(List.of(new PrLineConsumptionRow(100L, new BigDecimal("4.0000"))));
        when(productLookupProvider.resolve(10L)).thenReturn(new LookupDto(10L, "Bearing 6204", "BRG-6204"));
        when(uomLookupProvider.resolve(1L)).thenReturn(new LookupDto(1L, "PCS", "PCS"));

        Page<PurchaseOrderPrLineSelectorRow> result = useCase.execute(10L, "bearing", List.of(), PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        PurchaseOrderPrLineSelectorRow row = result.getContent().getFirst();
        assertThat(row.prLineId()).isEqualTo(100L);
        assertThat(row.productName()).isEqualTo("Bearing 6204");
        assertThat(row.productSubtext()).isEqualTo("BRG-6204");
        assertThat(row.remainingQuantity()).isEqualByComparingTo("6.0000");
        assertThat(row.estimatedUnitPrice()).isEqualByComparingTo("100.00");
    }

    private PurchaseRequisitionLineEntity line(Long id,
                                               Long productId,
                                               Long uomId,
                                               String quantity,
                                               String estimatedUnitPrice) {
        PurchaseRequisitionEntity header = new PurchaseRequisitionEntity();
        header.setId(10L);
        header.setCode("PR-001");
        header.setRequestDate(LocalDate.of(2026, 7, 1));
        header.setRequesterId(500L);
        header.setPriority(PurchaseRequisitionPriority.NORMAL);
        header.setStatus(PurchaseRequisitionStatus.APPROVED);
        header.setActive(true);
        header.setCurrencyId(1L);

        PurchaseRequisitionLineEntity entity = new PurchaseRequisitionLineEntity();
        entity.setId(id);
        entity.setHeader(header);
        entity.setProductId(productId);
        entity.setQuantity(new BigDecimal(quantity));
        entity.setUomId(uomId);
        entity.setEstimatedUnitPrice(new BigDecimal(estimatedUnitPrice));
        entity.setRequiredDate(LocalDate.of(2026, 7, 10));
        entity.setNote("Line note");
        return entity;
    }
}
