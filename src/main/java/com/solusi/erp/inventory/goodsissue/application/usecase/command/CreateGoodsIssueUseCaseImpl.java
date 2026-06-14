package com.solusi.erp.inventory.goodsissue.application.usecase.command;

import com.solusi.erp.core.infrastructure.sequence.SequenceGeneratorService;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssue;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssueLine;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssuePartyType;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssueReferenceType;
import com.solusi.erp.inventory.goodsissue.domain.repository.GoodsIssueRepository;
import com.solusi.erp.inventory.goodsissue.infrastructure.service.GoodsIssueSourceResolverRegistry;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class CreateGoodsIssueUseCaseImpl implements CreateGoodsIssueUseCase {

    private final GoodsIssueRepository goodsIssueRepository;
    private final SequenceGeneratorService sequenceGeneratorService;
    private final GoodsIssueSourceResolverRegistry resolverRegistry;

    public CreateGoodsIssueUseCaseImpl(GoodsIssueRepository goodsIssueRepository,
                                       SequenceGeneratorService sequenceGeneratorService,
                                       GoodsIssueSourceResolverRegistry resolverRegistry) {
        this.goodsIssueRepository = goodsIssueRepository;
        this.sequenceGeneratorService = sequenceGeneratorService;
        this.resolverRegistry = resolverRegistry;
    }

    @Override
    public GoodsIssue execute(LocalDate issueDate,
                              GoodsIssueReferenceType referenceType,
                              Long referenceId,
                              String referenceCode,
                              Long partyId,
                              GoodsIssuePartyType partyType,
                              Long facilityId,
                              Long currencyId,
                              BigDecimal exchangeRate,
                              String note,
                              List<GoodsIssueLineCommand> lines) {
        GoodsIssue draft = resolveDraft(
                referenceType, referenceId, referenceCode, partyId, partyType, facilityId, currencyId, exchangeRate);
        GoodsIssue goodsIssue = GoodsIssue.createNew(
                sequenceGeneratorService.generate("GOODS_ISSUE"),
                issueDate,
                draft.getReferenceType(),
                draft.getReferenceId(),
                draft.getReferenceCode(),
                draft.getPartyId(),
                draft.getPartyType(),
                draft.getFacilityId(),
                draft.getCurrencyId(),
                draft.getExchangeRate(),
                buildLines(lines)
        );
        goodsIssue.update(issueDate, note, goodsIssue.getLines());
        return goodsIssueRepository.save(goodsIssue);
    }

    private GoodsIssue resolveDraft(GoodsIssueReferenceType referenceType,
                                    Long referenceId,
                                    String referenceCode,
                                    Long partyId,
                                    GoodsIssuePartyType partyType,
                                    Long facilityId,
                                    Long currencyId,
                                    BigDecimal exchangeRate) {
        if (referenceType != null && referenceId != null && referenceType != GoodsIssueReferenceType.MANUAL) {
            return resolverRegistry.getResolver(referenceType).resolve(referenceId);
        }
        return GoodsIssue.createNew(
                null, null, referenceType != null ? referenceType : GoodsIssueReferenceType.MANUAL,
                referenceId, referenceCode, partyId, partyType, facilityId, currencyId, exchangeRate, List.of()
        );
    }

    private List<GoodsIssueLine> buildLines(List<GoodsIssueLineCommand> commands) {
        if (commands == null) {
            return List.of();
        }
        return commands.stream()
                .filter(this::hasPositiveQuantity)
                .map(this::toLine)
                .toList();
    }

    private GoodsIssueLine toLine(GoodsIssueLineCommand command) {
        return GoodsIssueLine.prefill(
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
        );
    }

    private boolean hasPositiveQuantity(GoodsIssueLineCommand command) {
        return command.quantityIssued() != null && command.quantityIssued().compareTo(BigDecimal.ZERO) > 0;
    }
}
