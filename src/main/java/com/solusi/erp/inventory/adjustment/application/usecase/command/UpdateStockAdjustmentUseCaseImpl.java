package com.solusi.erp.inventory.adjustment.application.usecase.command;

import com.solusi.erp.inventory.adjustment.domain.model.AdjustmentStatus;
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

public class UpdateStockAdjustmentUseCaseImpl implements UpdateStockAdjustmentUseCase {

    private final StockAdjustmentRepository repository;
    private final com.solusi.erp.inventory.repository.FacilityRepository facilityRepository;
    private final CurrencyRepository currencyRepository;
    private final MessageSource messageSource;

    public UpdateStockAdjustmentUseCaseImpl(
            StockAdjustmentRepository repository,
            com.solusi.erp.inventory.repository.FacilityRepository facilityRepository,
            CurrencyRepository currencyRepository,
            MessageSource messageSource) {
        this.repository = repository;
        this.facilityRepository = facilityRepository;
        this.currencyRepository = currencyRepository;
        this.messageSource = messageSource;
    }

    @Override
    public StockAdjustment execute(Long id, LocalDate transactionDate, String note, Long facilityId,
                                   Long currencyId, BigDecimal exchangeRate, List<LineCommand> lines) {
        StockAdjustment domain = repository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        messageSource.getMessage("msg.error.notfound", null, LocaleContextHolder.getLocale())));

        if (domain.getStatus() == AdjustmentStatus.COMPLETED) {
            throw new RuntimeException(
                    messageSource.getMessage("msg.error.stock-adjustment.already-completed", null, LocaleContextHolder.getLocale()));
        }

        com.solusi.erp.inventory.model.Facility facility = facilityRepository.findById(facilityId)
                .orElseThrow(() -> new RuntimeException(
                        messageSource.getMessage("msg.error.notfound", null, LocaleContextHolder.getLocale())));
        String facilityName = facility.getName();

        com.solusi.erp.master.currency.domain.model.Currency currency = currencyRepository.findById(currencyId)
                .orElseThrow(() -> new RuntimeException(
                        messageSource.getMessage("msg.error.notfound", null, LocaleContextHolder.getLocale())));
        String currencyAlias = currency.getAlias();

        List<StockAdjustmentLineItem> lineItems = buildLineItems(lines);
        domain.update(transactionDate, note, facilityId, facilityName, currencyId, currencyAlias, exchangeRate, lineItems);

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

