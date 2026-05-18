package com.solusi.erp.purchasing.purchaseorder.application.usecase.query;

import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.inventory.facility.domain.port.FacilityLookupProvider;
import com.solusi.erp.master.currency.domain.port.CurrencyLookupProvider;
import com.solusi.erp.master.party.domain.port.PartyLookupProvider;
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
@DisplayName("FindPurchaseOrderPrSelectorUseCase Tests")
class FindPurchaseOrderPrSelectorUseCaseTest {

    @Mock
    private PurchaseRequisitionJpaRepository purchaseRequisitionJpaRepository;

    @Mock
    private PurchaseOrderJpaRepository purchaseOrderJpaRepository;

    @Mock
    private PartyLookupProvider partyLookupProvider;

    @Mock
    private FacilityLookupProvider facilityLookupProvider;

    @Mock
    private CurrencyLookupProvider currencyLookupProvider;

    private FindPurchaseOrderPrSelectorUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new FindPurchaseOrderPrSelectorUseCaseImpl(
                purchaseRequisitionJpaRepository,
                purchaseOrderJpaRepository,
                partyLookupProvider,
                facilityLookupProvider,
                currencyLookupProvider
        );
    }

    @Test
    @DisplayName("execute returns empty page without consumption query when requisition has no lines")
    void execute_skipsConsumptionQueryWhenRequisitionHasNoLines() {
        PurchaseRequisitionEntity pr = approvedPr(10L, "PR-001", null, null, null);

        when(purchaseRequisitionJpaRepository.findApprovedForPoSelector(null, null))
                .thenReturn(List.of(pr));

        Page<PurchaseOrderPrSelectorRow> result = useCase.execute(null, null, PageRequest.of(0, 10));

        assertThat(result.getContent()).isEmpty();
        verify(purchaseOrderJpaRepository, never()).sumConsumedByPrLineIds(anySet(), anySet());
    }

    @Test
    @DisplayName("execute excludes purchase requisitions whose remaining lines are fully consumed")
    void execute_excludesExhaustedPurchaseRequisitions() {
        PurchaseRequisitionEntity pr = approvedPr(10L, "PR-001", 100L, 200L, 1L,
                line(100L, 10L, 1L, "5.0000", "100.00"));

        when(purchaseRequisitionJpaRepository.findApprovedForPoSelector(null, null))
                .thenReturn(List.of(pr));
        when(purchaseOrderJpaRepository.sumConsumedByPrLineIds(Set.of(100L), FindPurchaseOrderPrSelectorUseCaseImpl.CONSUMING_STATUSES))
                .thenReturn(List.of(new PrLineConsumptionRow(100L, new BigDecimal("5.0000"))));

        Page<PurchaseOrderPrSelectorRow> result = useCase.execute(null, null, PageRequest.of(0, 10));

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("execute keeps row when lookup providers return null")
    void execute_keepsRowWhenLookupProvidersReturnNull() {
        PurchaseRequisitionEntity pr = approvedPr(10L, "PR-001", 100L, 200L, 1L,
                line(100L, 10L, 1L, "5.0000", "100.00"));

        when(purchaseRequisitionJpaRepository.findApprovedForPoSelector(null, null))
                .thenReturn(List.of(pr));
        when(purchaseOrderJpaRepository.sumConsumedByPrLineIds(Set.of(100L), FindPurchaseOrderPrSelectorUseCaseImpl.CONSUMING_STATUSES))
                .thenReturn(List.of());
        when(partyLookupProvider.resolve(100L)).thenReturn(null);
        when(facilityLookupProvider.resolve(200L)).thenReturn(null);
        when(currencyLookupProvider.resolve(1L)).thenReturn(null);

        Page<PurchaseOrderPrSelectorRow> result = useCase.execute(null, null, PageRequest.of(0, 10));

        assertThat(result.getContent()).singleElement().satisfies(row -> {
            assertThat(row.supplierName()).isNull();
            assertThat(row.supplierSubtext()).isNull();
            assertThat(row.facilityName()).isNull();
            assertThat(row.facilitySubtext()).isNull();
            assertThat(row.currencyName()).isNull();
            assertThat(row.currencySubtext()).isNull();
        });
    }

    @Test
    @DisplayName("execute returns empty page when requested offset is beyond available rows")
    void execute_returnsEmptyPageWhenOffsetBeyondRows() {
        PurchaseRequisitionEntity pr = approvedPr(10L, "PR-001", 100L, 200L, 1L,
                line(100L, 10L, 1L, "5.0000", "100.00"));

        when(purchaseRequisitionJpaRepository.findApprovedForPoSelector(null, null))
                .thenReturn(List.of(pr));
        when(purchaseOrderJpaRepository.sumConsumedByPrLineIds(Set.of(100L), FindPurchaseOrderPrSelectorUseCaseImpl.CONSUMING_STATUSES))
                .thenReturn(List.of());
        when(partyLookupProvider.resolve(100L)).thenReturn(null);
        when(facilityLookupProvider.resolve(200L)).thenReturn(null);
        when(currencyLookupProvider.resolve(1L)).thenReturn(null);

        Page<PurchaseOrderPrSelectorRow> result = useCase.execute(null, null, PageRequest.of(1, 10));

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(1L);
    }

    @Test
    @DisplayName("execute returns eligible purchase requisition rows with lookup text and remaining line count")
    void execute_returnsEligiblePurchaseRequisitionRows() {
        PurchaseRequisitionEntity pr = approvedPr(10L, "PR-001", 100L, 200L, 1L,
                line(100L, 10L, 1L, "5.0000", "100.00"),
                line(101L, 11L, 1L, "2.0000", "50.00"));

        when(purchaseRequisitionJpaRepository.findApprovedForPoSelector(100L, "alpha"))
                .thenReturn(List.of(pr));
        when(purchaseOrderJpaRepository.sumConsumedByPrLineIds(Set.of(100L, 101L), FindPurchaseOrderPrSelectorUseCaseImpl.CONSUMING_STATUSES))
                .thenReturn(List.of(new PrLineConsumptionRow(100L, new BigDecimal("5.0000"))));
        when(partyLookupProvider.resolve(100L)).thenReturn(new LookupDto(100L, "Alpha Supplier", "SUP-001"));
        when(facilityLookupProvider.resolve(200L)).thenReturn(new LookupDto(200L, "Main Warehouse", "WH-01"));
        when(currencyLookupProvider.resolve(1L)).thenReturn(new LookupDto(1L, "US Dollar", "USD"));

        Page<PurchaseOrderPrSelectorRow> result = useCase.execute("alpha", 100L, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        PurchaseOrderPrSelectorRow row = result.getContent().getFirst();
        assertThat(row.prId()).isEqualTo(10L);
        assertThat(row.prCode()).isEqualTo("PR-001");
        assertThat(row.requestDate()).isEqualTo(LocalDate.of(2026, 7, 1));
        assertThat(row.supplierName()).isEqualTo("Alpha Supplier");
        assertThat(row.supplierSubtext()).isEqualTo("SUP-001");
        assertThat(row.facilityName()).isEqualTo("Main Warehouse");
        assertThat(row.currencySubtext()).isEqualTo("USD");
        assertThat(row.remainingLineCount()).isEqualTo(1L);
    }

    private PurchaseRequisitionEntity approvedPr(Long id,
                                                 String code,
                                                 Long supplierId,
                                                 Long facilityId,
                                                 Long currencyId,
                                                 PurchaseRequisitionLineEntity... lines) {
        PurchaseRequisitionEntity entity = new PurchaseRequisitionEntity();
        entity.setId(id);
        entity.setCode(code);
        entity.setRequestDate(LocalDate.of(2026, 7, 1));
        entity.setRequesterId(500L);
        entity.setFacilityId(facilityId);
        entity.setDepartment("Procurement");
        entity.setPriority(PurchaseRequisitionPriority.NORMAL);
        entity.setStatus(PurchaseRequisitionStatus.APPROVED);
        entity.setActive(true);
        entity.setSuggestedSupplierId(supplierId);
        entity.setCurrencyId(currencyId);
        entity.setLines(List.of(lines));
        for (PurchaseRequisitionLineEntity line : lines) {
            line.setHeader(entity);
        }
        return entity;
    }

    private PurchaseRequisitionLineEntity line(Long id,
                                               Long productId,
                                               Long uomId,
                                               String quantity,
                                               String estimatedUnitPrice) {
        PurchaseRequisitionLineEntity entity = new PurchaseRequisitionLineEntity();
        entity.setId(id);
        entity.setProductId(productId);
        entity.setQuantity(new BigDecimal(quantity));
        entity.setUomId(uomId);
        entity.setEstimatedUnitPrice(new BigDecimal(estimatedUnitPrice));
        entity.setRequiredDate(LocalDate.of(2026, 7, 10));
        entity.setNote("Line note");
        return entity;
    }
}
