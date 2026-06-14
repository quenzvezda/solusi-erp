package com.solusi.erp.purchasing.purchasereturn.infrastructure.adapter;

import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.inventory.container.domain.port.ContainerLookupProvider;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssue;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssueLine;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssueReferenceType;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssueStatus;
import com.solusi.erp.inventory.goodsissue.domain.repository.GoodsIssueRepository;
import com.solusi.erp.inventory.product.domain.port.ProductLookupProvider;
import com.solusi.erp.inventory.stock.domain.model.MovementType;
import com.solusi.erp.inventory.stock.domain.model.ReferenceType;
import com.solusi.erp.inventory.stock.domain.model.StockMovementReversalRequest;
import com.solusi.erp.inventory.stock.domain.port.StockMovementReversalService;
import com.solusi.erp.inventory.stock.infrastructure.persistence.InventoryMovementEntity;
import com.solusi.erp.inventory.stock.infrastructure.persistence.InventoryMovementJpaRepository;
import com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturn;
import com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturnReversalLine;
import com.solusi.erp.purchasing.purchasereturn.domain.port.PurchaseReturnInventoryReversalPort;
import com.solusi.erp.purchasing.purchasereturn.domain.port.PurchaseReturnReversibleMovement;
import com.solusi.erp.purchasing.purchasereturn.domain.port.PurchaseReturnStockReversalTarget;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PurchaseReturnInventoryReversalAdapter implements PurchaseReturnInventoryReversalPort {

    private final GoodsIssueRepository goodsIssueRepository;
    private final InventoryMovementJpaRepository movementRepository;
    private final StockMovementReversalService stockMovementReversalService;
    private final ProductLookupProvider productLookupProvider;
    private final ContainerLookupProvider containerLookupProvider;

    public PurchaseReturnInventoryReversalAdapter(GoodsIssueRepository goodsIssueRepository,
                                                  InventoryMovementJpaRepository movementRepository,
                                                  StockMovementReversalService stockMovementReversalService,
                                                  ProductLookupProvider productLookupProvider,
                                                  ContainerLookupProvider containerLookupProvider) {
        this.goodsIssueRepository = goodsIssueRepository;
        this.movementRepository = movementRepository;
        this.stockMovementReversalService = stockMovementReversalService;
        this.productLookupProvider = productLookupProvider;
        this.containerLookupProvider = containerLookupProvider;
    }

    @Override
    public List<PurchaseReturnReversibleMovement> findReversibleMovements(PurchaseReturn purchaseReturn) {
        GoodsIssue goodsIssue = loadGeneratedGoodsIssue(purchaseReturn);
        return outboundMovements(goodsIssue).stream()
                .map(movement -> toReversibleMovement(goodsIssue, movement))
                .toList();
    }

    @Override
    public List<PurchaseReturnReversalLine> reverseStockMovements(PurchaseReturn purchaseReturn,
                                                                  LocalDate reversalDate,
                                                                  String reversalReason,
                                                                  List<PurchaseReturnStockReversalTarget> targets) {
        GoodsIssue goodsIssue = loadGeneratedGoodsIssue(purchaseReturn);
        List<InventoryMovementEntity> movements = outboundMovements(goodsIssue);
        Map<Long, PurchaseReturnStockReversalTarget> targetByMovementId = targets.stream()
                .filter(target -> target.originalMovementId() != null)
                .collect(Collectors.toMap(
                        PurchaseReturnStockReversalTarget::originalMovementId,
                        Function.identity(),
                        (left, right) -> right
                ));
        List<StockMovementReversalRequest> reversalRequests = movements.stream()
                .map(movement -> toReversalRequest(movement, targetByMovementId, reversalDate, reversalReason))
                .toList();
        stockMovementReversalService.reverse(reversalRequests);
        return movements.stream()
                .map(movement -> toSnapshot(goodsIssue, movement, targetByMovementId.get(movement.getId())))
                .toList();
    }

    @Override
    public void cancelGeneratedGoodsIssue(PurchaseReturn purchaseReturn,
                                          LocalDate reversalDate,
                                          String reversalReason) {
        GoodsIssue goodsIssue = loadGeneratedGoodsIssue(purchaseReturn);
        goodsIssue.cancel(reversalDate, reversalReason);
        goodsIssueRepository.save(goodsIssue);
    }

    private GoodsIssue loadGeneratedGoodsIssue(PurchaseReturn purchaseReturn) {
        if (purchaseReturn.getGeneratedGoodsIssueId() == null) {
            throw new DomainException("msg.error.purchase-return.reverse.gi-required");
        }
        GoodsIssue goodsIssue = goodsIssueRepository.findById(purchaseReturn.getGeneratedGoodsIssueId())
                .orElseThrow(() -> new DomainException("msg.error.purchase-return.reverse.gi-not-found"));
        if (goodsIssue.getReferenceType() != GoodsIssueReferenceType.PURCHASE_RETURN
                || !purchaseReturn.getId().equals(goodsIssue.getReferenceId())) {
            throw new DomainException("msg.error.purchase-return.reverse.gi-source-mismatch");
        }
        if (goodsIssue.getStatus() != GoodsIssueStatus.COMPLETED) {
            throw new DomainException("msg.error.purchase-return.reverse.gi-not-completed");
        }
        return goodsIssue;
    }

    private List<InventoryMovementEntity> outboundMovements(GoodsIssue goodsIssue) {
        List<InventoryMovementEntity> movements = movementRepository
                .findByReferenceTypeAndReferenceIdOrderByIdAsc(ReferenceType.GOODS_ISSUE, goodsIssue.getId())
                .stream()
                .filter(this::isOutboundMovement)
                .toList();
        if (movements.isEmpty()) {
            throw new DomainException("msg.error.purchase-return.reverse.movements-not-found");
        }
        return movements;
    }

    private PurchaseReturnReversibleMovement toReversibleMovement(GoodsIssue goodsIssue,
                                                                  InventoryMovementEntity movement) {
        LookupDto product = productLookupProvider.resolve(movement.getProductId());
        LookupDto container = containerLookupProvider.resolve(movement.getContainerId());
        return new PurchaseReturnReversibleMovement(
                movement.getId(),
                resolvePurchaseReturnLineId(goodsIssue, movement),
                movement.getProductId(),
                product != null ? product.name() : null,
                product != null ? product.subText() : null,
                movement.getQuantity().abs(),
                movement.getContainerId(),
                container != null ? container.name() : null,
                container != null ? container.subText() : null,
                movement.getSerialNumber()
        );
    }

    private StockMovementReversalRequest toReversalRequest(
            InventoryMovementEntity movement,
            Map<Long, PurchaseReturnStockReversalTarget> targetByMovementId,
            LocalDate reversalDate,
            String reversalReason) {
        PurchaseReturnStockReversalTarget target = targetByMovementId.get(movement.getId());
        if (target == null || target.targetContainerId() == null) {
            throw new DomainException("msg.error.purchase-return.reverse.target-container-required");
        }
        return new StockMovementReversalRequest(
                movement.getId(),
                target.targetContainerId(),
                reversalDate,
                reversalReason
        );
    }

    private PurchaseReturnReversalLine toSnapshot(GoodsIssue goodsIssue,
                                                  InventoryMovementEntity movement,
                                                  PurchaseReturnStockReversalTarget target) {
        return PurchaseReturnReversalLine.create(
                resolvePurchaseReturnLineId(goodsIssue, movement),
                movement.getId(),
                target.targetContainerId(),
                movement.getProductId(),
                movement.getSerialNumber(),
                movement.getQuantity().abs()
        );
    }

    private Long resolvePurchaseReturnLineId(GoodsIssue goodsIssue, InventoryMovementEntity movement) {
        return goodsIssue.getLines().stream()
                .filter(line -> matches(line, movement))
                .map(GoodsIssueLine::getReferenceLineId)
                .filter(id -> id != null)
                .findFirst()
                .orElse(null);
    }

    private boolean matches(GoodsIssueLine line, InventoryMovementEntity movement) {
        if (!line.getProductId().equals(movement.getProductId())
                || !line.getContainerId().equals(movement.getContainerId())) {
            return false;
        }
        if (movement.getSerialNumber() == null || movement.getSerialNumber().isBlank()) {
            BigDecimal baseQuantity = line.getBaseQuantity() != null ? line.getBaseQuantity() : line.getQuantityIssued();
            return baseQuantity != null && baseQuantity.compareTo(movement.getQuantity().abs()) == 0;
        }
        return line.getSerialNumber() != null
                && List.of(line.getSerialNumber().split(",")).stream()
                .map(String::trim)
                .anyMatch(movement.getSerialNumber()::equals);
    }

    private boolean isOutboundMovement(InventoryMovementEntity movement) {
        if (movement.getMovementType() == MovementType.ADJUSTMENT) {
            return movement.getQuantity() != null && movement.getQuantity().signum() < 0;
        }
        return movement.getMovementType() == MovementType.ISSUE
                || movement.getMovementType() == MovementType.ISSUE_RESERVED
                || movement.getMovementType() == MovementType.TRANSFER_OUT;
    }
}
