package com.solusi.erp.inventory.goodsissue.application.usecase.query;

import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.inventory.container.domain.port.ContainerLookupProvider;
import com.solusi.erp.inventory.facility.domain.port.FacilityLookupProvider;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssue;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssueReferenceType;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssueStatus;
import com.solusi.erp.inventory.goodsissue.domain.repository.GoodsIssueRepository;
import com.solusi.erp.inventory.product.domain.port.ProductLookupProvider;
import com.solusi.erp.inventory.stock.domain.model.MovementType;
import com.solusi.erp.inventory.stock.domain.model.ReferenceType;
import com.solusi.erp.inventory.stock.infrastructure.persistence.InventoryMovementEntity;
import com.solusi.erp.inventory.stock.infrastructure.persistence.InventoryMovementJpaRepository;
import com.solusi.erp.inventory.uom.domain.port.UomLookupProvider;

import java.util.List;

public class GetGoodsIssueCancelViewUseCaseImpl implements GetGoodsIssueCancelViewUseCase {

    private final GoodsIssueRepository goodsIssueRepository;
    private final InventoryMovementJpaRepository movementRepository;
    private final ProductLookupProvider productLookupProvider;
    private final UomLookupProvider uomLookupProvider;
    private final ContainerLookupProvider containerLookupProvider;
    private final FacilityLookupProvider facilityLookupProvider;

    public GetGoodsIssueCancelViewUseCaseImpl(GoodsIssueRepository goodsIssueRepository,
                                              InventoryMovementJpaRepository movementRepository,
                                              ProductLookupProvider productLookupProvider,
                                              UomLookupProvider uomLookupProvider,
                                              ContainerLookupProvider containerLookupProvider,
                                              FacilityLookupProvider facilityLookupProvider) {
        this.goodsIssueRepository = goodsIssueRepository;
        this.movementRepository = movementRepository;
        this.productLookupProvider = productLookupProvider;
        this.uomLookupProvider = uomLookupProvider;
        this.containerLookupProvider = containerLookupProvider;
        this.facilityLookupProvider = facilityLookupProvider;
    }

    @Override
    public GoodsIssueCancelView execute(Long id) {
        GoodsIssue issue = goodsIssueRepository.findById(id)
                .orElseThrow(() -> new DomainException("msg.error.gi.notfound"));
        if (issue.getStatus() != GoodsIssueStatus.COMPLETED) {
            throw new DomainException("msg.error.gi.cancel.only.completed");
        }
        if (issue.getReferenceType() != GoodsIssueReferenceType.MANUAL) {
            throw new DomainException("msg.error.gi.cancel.source.owned");
        }
        List<GoodsIssueCancelLineView> lines = movementRepository
                .findByReferenceTypeAndReferenceIdOrderByIdAsc(ReferenceType.GOODS_ISSUE, issue.getId())
                .stream()
                .filter(this::isOutboundMovement)
                .map(this::toLineView)
                .toList();
        if (lines.isEmpty()) {
            throw new DomainException("msg.error.gi.cancel.movements.notfound");
        }
        LookupDto facility = facilityLookupProvider.resolve(issue.getFacilityId());
        return new GoodsIssueCancelView(
                issue.getId(),
                issue.getCode(),
                issue.getIssueDate(),
                issue.getReferenceType(),
                issue.getReferenceCode(),
                issue.getFacilityId(),
                facility != null ? facility.name() : null,
                lines
        );
    }

    private GoodsIssueCancelLineView toLineView(InventoryMovementEntity movement) {
        LookupDto product = productLookupProvider.resolve(movement.getProductId());
        LookupDto container = containerLookupProvider.resolve(movement.getContainerId());
        return new GoodsIssueCancelLineView(
                movement.getId(),
                movement.getProductId(),
                product != null ? product.name() : null,
                product != null ? product.subText() : null,
                movement.getQuantity().abs(),
                null,
                null,
                movement.getContainerId(),
                container != null ? container.name() : null,
                container != null ? container.subText() : null,
                movement.getSerialNumber()
        );
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
