package com.solusi.erp.inventory.goodsissue.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssue;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssueLine;
import com.solusi.erp.inventory.goodsissue.domain.repository.GoodsIssueRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class UpdateGoodsIssueUseCaseImpl implements UpdateGoodsIssueUseCase {

    private final GoodsIssueRepository goodsIssueRepository;

    public UpdateGoodsIssueUseCaseImpl(GoodsIssueRepository goodsIssueRepository) {
        this.goodsIssueRepository = goodsIssueRepository;
    }

    @Override
    public GoodsIssue execute(Long id, LocalDate issueDate, String note, List<GoodsIssueLineCommand> lines) {
        GoodsIssue goodsIssue = goodsIssueRepository.findById(id)
                .orElseThrow(() -> new DomainException("msg.error.gi.notfound"));
        goodsIssue.update(issueDate, note, buildLines(lines));
        return goodsIssueRepository.save(goodsIssue);
    }

    private List<GoodsIssueLine> buildLines(List<GoodsIssueLineCommand> commands) {
        if (commands == null) {
            return List.of();
        }
        return commands.stream()
                .filter(this::hasPositiveQuantity)
                .map(command -> GoodsIssueLine.prefill(
                        command.referenceLineId(),
                        command.productId(),
                        Boolean.TRUE.equals(command.serialized()),
                        command.quantityIssued(),
                        command.uomId(),
                        command.baseQuantity(),
                        command.facilityId(),
                        command.gridId(),
                        command.containerId(),
                        command.serialNumber(),
                        command.unitCost(),
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        command.valuationRefType(),
                        command.valuationRefId(),
                        command.valuationRefLineId()
                ))
                .toList();
    }

    private boolean hasPositiveQuantity(GoodsIssueLineCommand command) {
        return command.quantityIssued() != null && command.quantityIssued().compareTo(BigDecimal.ZERO) > 0;
    }
}
