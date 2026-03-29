package com.solusi.erp.inventory.adjustment.application.usecase.command;

import com.solusi.erp.inventory.adjustment.domain.model.StockAdjustment;
import com.solusi.erp.inventory.adjustment.domain.model.StockAdjustmentLineItem;
import com.solusi.erp.inventory.adjustment.domain.repository.StockAdjustmentRepository;
import com.solusi.erp.master.currency.domain.repository.CurrencyRepository;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class CreateStockAdjustmentUseCaseImpl implements CreateStockAdjustmentUseCase {

    private final StockAdjustmentRepository repository;
    private final com.solusi.erp.inventory.repository.FacilityRepository facilityRepository;
    private final CurrencyRepository currencyRepository;
    private final com.solusi.erp.core.infrastructure.sequence.SequenceGeneratorService sequenceGeneratorService;
    private final MessageSource messageSource;

    public CreateStockAdjustmentUseCaseImpl(
            StockAdjustmentRepository repository,
            com.solusi.erp.inventory.repository.FacilityRepository facilityRepository,
            CurrencyRepository currencyRepository,
            com.solusi.erp.core.infrastructure.sequence.SequenceGeneratorService sequenceGeneratorService,
            MessageSource messageSource) {
        this.repository = repository;
        this.facilityRepository = facilityRepository;
        this.currencyRepository = currencyRepository;
        this.sequenceGeneratorService = sequenceGeneratorService;
        this.messageSource = messageSource;
    }

    @Override
    public StockAdjustment execute(LocalDate transactionDate, String note, Long facilityId,
                                   Long currencyId, BigDecimal exchangeRate, List<LineCommand> lines) {
        com.solusi.erp.inventory.model.Facility facility = facilityRepository.findById(facilityId)
                .orElseThrow(() -> new RuntimeException(
                        messageSource.getMessage("msg.error.notfound", null, LocaleContextHolder.getLocale())));
        String facilityName = facility.getName();

        com.solusi.erp.master.currency.domain.model.Currency currency = currencyRepository.findById(currencyId)
                .orElseThrow(() -> new RuntimeException(
                        messageSource.getMessage("msg.error.notfound", null, LocaleContextHolder.getLocale())));
        String currencyAlias = currency.getAlias();

        List<StockAdjustmentLineItem> lineItems = buildLineItems(lines);

        StockAdjustment domain = StockAdjustment.createNew(transactionDate, note, facilityId, facilityName,
                currencyId, currencyAlias, exchangeRate, lineItems);

        domain.setCode(sequenceGeneratorService.generate("STOCK_ADJUSTMENT"));

        return repository.save(domain);
    }

    private List<StockAdjustmentLineItem> buildLineItems(List<LineCommand> lines) {
        return lines.stream().map(l -> {
            BigDecimal total = (l.quantity() != null && l.unitCost() != null)
                    ? l.quantity().multiply(l.unitCost()) : BigDecimal.ZERO;
            return new StockAdjustmentLineItem(l.id(), l.version(), l.productId(), l.productCode(),
                    l.productName(), l.isSerialized(), l.gridId(), l.gridCode(), l.gridName(),
                    l.containerId(), l.containerCode(), l.containerName(), l.facilityName(),
                    l.uomId(), l.uomName(), l.conversionFactor(), l.quantity(), l.unitCost(), total,
                    l.serialNumber());
        }).collect(Collectors.toList());
    }
}

