package com.solusi.erp.purchasing.purchaseorder.application.usecase.query;

import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.inventory.facility.domain.port.FacilityLookupProvider;
import com.solusi.erp.master.currency.domain.port.CurrencyLookupProvider;
import com.solusi.erp.master.party.domain.port.PartyLookupProvider;
import com.solusi.erp.purchasing.purchaserequisition.infrastructure.persistence.PurchaseRequisitionEntity;
import com.solusi.erp.purchasing.purchaserequisition.infrastructure.persistence.PurchaseRequisitionJpaRepository;
import com.solusi.erp.purchasing.purchaserequisition.infrastructure.persistence.PurchaseRequisitionLineEntity;
import com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrderStatus;
import com.solusi.erp.purchasing.purchaseorder.infrastructure.persistence.PrLineConsumptionRow;
import com.solusi.erp.purchasing.purchaseorder.infrastructure.persistence.PurchaseOrderJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FindPurchaseOrderPrSelectorUseCaseImpl implements FindPurchaseOrderPrSelectorUseCase {

    public static final Set<PurchaseOrderStatus> CONSUMING_STATUSES = EnumSet.of(
            PurchaseOrderStatus.SUBMITTED,
            PurchaseOrderStatus.APPROVED,
            PurchaseOrderStatus.SENT,
            PurchaseOrderStatus.PARTIALLY_RECEIVED,
            PurchaseOrderStatus.FULLY_RECEIVED,
            PurchaseOrderStatus.BILLED,
            PurchaseOrderStatus.CLOSED
    );

    private final PurchaseRequisitionJpaRepository purchaseRequisitionJpaRepository;
    private final PurchaseOrderJpaRepository purchaseOrderJpaRepository;
    private final PartyLookupProvider partyLookupProvider;
    private final FacilityLookupProvider facilityLookupProvider;
    private final CurrencyLookupProvider currencyLookupProvider;

    public FindPurchaseOrderPrSelectorUseCaseImpl(PurchaseRequisitionJpaRepository purchaseRequisitionJpaRepository,
                                                  PurchaseOrderJpaRepository purchaseOrderJpaRepository,
                                                  PartyLookupProvider partyLookupProvider,
                                                  FacilityLookupProvider facilityLookupProvider,
                                                  CurrencyLookupProvider currencyLookupProvider) {
        this.purchaseRequisitionJpaRepository = purchaseRequisitionJpaRepository;
        this.purchaseOrderJpaRepository = purchaseOrderJpaRepository;
        this.partyLookupProvider = partyLookupProvider;
        this.facilityLookupProvider = facilityLookupProvider;
        this.currencyLookupProvider = currencyLookupProvider;
    }

    @Override
    public Page<PurchaseOrderPrSelectorRow> execute(String keyword, Long supplierId, Pageable pageable) {
        String normalizedKeyword = normalizeKeyword(keyword);
        List<PurchaseRequisitionEntity> requisitions =
                purchaseRequisitionJpaRepository.findApprovedForPoSelector(supplierId, normalizedKeyword);

        Map<Long, BigDecimal> consumedByLineId = loadConsumedByLineId(requisitions);
        List<PurchaseOrderPrSelectorRow> rows = new ArrayList<>();

        for (PurchaseRequisitionEntity requisition : requisitions) {
            long remainingLineCount = requisition.getLines().stream()
                    .filter(line -> remainingQuantity(line, consumedByLineId).compareTo(BigDecimal.ZERO) > 0)
                    .count();
            if (remainingLineCount <= 0) {
                continue;
            }

            LookupDto supplier = requisition.getSuggestedSupplierId() != null
                    ? partyLookupProvider.resolve(requisition.getSuggestedSupplierId())
                    : null;
            LookupDto facility = requisition.getFacilityId() != null
                    ? facilityLookupProvider.resolve(requisition.getFacilityId())
                    : null;
            LookupDto currency = requisition.getCurrencyId() != null
                    ? currencyLookupProvider.resolve(requisition.getCurrencyId())
                    : null;

            rows.add(new PurchaseOrderPrSelectorRow(
                    requisition.getId(),
                    requisition.getCode(),
                    requisition.getRequestDate(),
                    requisition.getSuggestedSupplierId(),
                    supplier != null ? supplier.name() : null,
                    supplier != null ? supplier.subText() : null,
                    requisition.getFacilityId(),
                    facility != null ? facility.name() : null,
                    facility != null ? facility.subText() : null,
                    requisition.getCurrencyId(),
                    currency != null ? currency.name() : null,
                    currency != null ? currency.subText() : null,
                    remainingLineCount
            ));
        }

        return toPage(rows, pageable);
    }

    private Map<Long, BigDecimal> loadConsumedByLineId(List<PurchaseRequisitionEntity> requisitions) {
        Set<Long> prLineIds = requisitions.stream()
                .flatMap(requisition -> requisition.getLines().stream())
                .map(PurchaseRequisitionLineEntity::getId)
                .filter(id -> id != null)
                .collect(java.util.stream.Collectors.toSet());

        if (prLineIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, BigDecimal> consumedByLineId = new HashMap<>();
        for (PrLineConsumptionRow row : purchaseOrderJpaRepository.sumConsumedByPrLineIds(prLineIds, CONSUMING_STATUSES)) {
            consumedByLineId.put(row.prLineId(), row.consumedQuantity());
        }
        return consumedByLineId;
    }

    private BigDecimal remainingQuantity(PurchaseRequisitionLineEntity line, Map<Long, BigDecimal> consumedByLineId) {
        BigDecimal consumed = consumedByLineId.getOrDefault(line.getId(), BigDecimal.ZERO);
        return line.getQuantity().subtract(consumed);
    }

    private String normalizeKeyword(String keyword) {
        return keyword == null || keyword.isBlank() ? null : keyword.trim();
    }

    private <T> Page<T> toPage(List<T> items, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), items.size());
        List<T> content = start >= items.size() ? List.of() : items.subList(start, end);
        return new PageImpl<>(content, pageable, items.size());
    }
}
