package com.solusi.erp.purchasing.purchaseorder.application.usecase.query;

import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.inventory.product.domain.port.ProductLookupProvider;
import com.solusi.erp.inventory.uom.domain.port.UomLookupProvider;
import com.solusi.erp.purchasing.purchaserequisition.infrastructure.persistence.PurchaseRequisitionLineEntity;
import com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrderStatus;
import com.solusi.erp.purchasing.purchaseorder.infrastructure.persistence.PrLineConsumptionRow;
import com.solusi.erp.purchasing.purchaseorder.infrastructure.persistence.PurchaseOrderJpaRepository;
import com.solusi.erp.purchasing.purchaserequisition.infrastructure.persistence.PurchaseRequisitionJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class FindPurchaseOrderPrLineSelectorUseCaseImpl implements FindPurchaseOrderPrLineSelectorUseCase {

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
    private final ProductLookupProvider productLookupProvider;
    private final UomLookupProvider uomLookupProvider;

    public FindPurchaseOrderPrLineSelectorUseCaseImpl(PurchaseRequisitionJpaRepository purchaseRequisitionJpaRepository,
                                                      PurchaseOrderJpaRepository purchaseOrderJpaRepository,
                                                      ProductLookupProvider productLookupProvider,
                                                      UomLookupProvider uomLookupProvider) {
        this.purchaseRequisitionJpaRepository = purchaseRequisitionJpaRepository;
        this.purchaseOrderJpaRepository = purchaseOrderJpaRepository;
        this.productLookupProvider = productLookupProvider;
        this.uomLookupProvider = uomLookupProvider;
    }

    @Override
    public Page<PurchaseOrderPrLineSelectorRow> execute(Long prId, String keyword, List<Long> excludePrLineIds, Pageable pageable) {
        String normalizedKeyword = normalizeKeyword(keyword);
        Set<Long> excludedIds = excludePrLineIds == null ? Set.of() : new HashSet<>(excludePrLineIds);

        List<PurchaseRequisitionLineEntity> lines =
                purchaseRequisitionJpaRepository.findApprovedLinesForPoSelector(prId, normalizedKeyword);
        Map<Long, BigDecimal> consumedByLineId = loadConsumedByLineId(lines);

        List<PurchaseOrderPrLineSelectorRow> rows = new ArrayList<>();
        for (PurchaseRequisitionLineEntity line : lines) {
            if (line.getId() == null || excludedIds.contains(line.getId())) {
                continue;
            }

            BigDecimal remainingQuantity = line.getQuantity()
                    .subtract(consumedByLineId.getOrDefault(line.getId(), BigDecimal.ZERO));
            if (remainingQuantity.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            LookupDto product = line.getProductId() != null ? productLookupProvider.resolve(line.getProductId()) : null;
            LookupDto uom = line.getUomId() != null ? uomLookupProvider.resolve(line.getUomId()) : null;

            PurchaseOrderPrLineSelectorRow row = new PurchaseOrderPrLineSelectorRow(
                    line.getId(),
                    line.getHeader() != null ? line.getHeader().getId() : null,
                    line.getHeader() != null ? line.getHeader().getCode() : null,
                    line.getProductId(),
                    product != null ? product.name() : null,
                    product != null ? product.subText() : null,
                    line.getQuantity(),
                    remainingQuantity,
                    line.getUomId(),
                    uom != null ? uom.name() : null,
                    uom != null ? uom.subText() : null,
                    line.getEstimatedUnitPrice(),
                    line.getRequiredDate(),
                    line.getNote()
            );

            if (!matchesKeyword(row, normalizedKeyword)) {
                continue;
            }

            rows.add(row);
        }

        return toPage(rows, pageable);
    }

    private Map<Long, BigDecimal> loadConsumedByLineId(List<PurchaseRequisitionLineEntity> lines) {
        Set<Long> prLineIds = lines.stream()
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

    private boolean matchesKeyword(PurchaseOrderPrLineSelectorRow row, String keyword) {
        if (keyword == null) {
            return true;
        }

        String haystack = String.join(" ",
                value(row.prCode()),
                value(row.productName()),
                value(row.productSubtext()),
                value(row.note())
        ).toLowerCase(Locale.ROOT);

        return haystack.contains(keyword.toLowerCase(Locale.ROOT));
    }

    private String value(String input) {
        return input == null ? "" : input;
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
