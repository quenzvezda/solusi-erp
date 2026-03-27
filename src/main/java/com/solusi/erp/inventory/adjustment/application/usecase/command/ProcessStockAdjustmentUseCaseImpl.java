package com.solusi.erp.inventory.adjustment.application.usecase.command;

import com.solusi.erp.inventory.adjustment.domain.model.StockAdjustment;
import com.solusi.erp.inventory.adjustment.domain.model.StockAdjustmentLineItem;
import com.solusi.erp.inventory.adjustment.domain.repository.StockAdjustmentRepository;
import com.solusi.erp.inventory.dto.StockMovementPayload;
import com.solusi.erp.inventory.model.MovementType;
import com.solusi.erp.inventory.model.ReferenceType;
import com.solusi.erp.inventory.service.StockService;
import com.solusi.erp.inventory.util.SerialNumberGenerator;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ProcessStockAdjustmentUseCaseImpl implements ProcessStockAdjustmentUseCase {

    private final StockAdjustmentRepository repository;
    private final StockService stockService;
    private final MessageSource messageSource;

    public ProcessStockAdjustmentUseCaseImpl(StockAdjustmentRepository repository,
                                              StockService stockService,
                                              MessageSource messageSource) {
        this.repository = repository;
        this.stockService = stockService;
        this.messageSource = messageSource;
    }

    @Override
    public void execute(Long id) {
        StockAdjustment domain = repository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        messageSource.getMessage("msg.error.notfound", null, LocaleContextHolder.getLocale())));

        List<StockAdjustmentLineItem> linesToProcess = domain.process();

        for (StockAdjustmentLineItem line : linesToProcess) {
            if (Boolean.TRUE.equals(line.getIsSerialized())) {
                processSerializedLine(domain, line);
            } else {
                processStandardLine(domain, line);
            }
        }

        repository.save(domain);
    }

    private void processSerializedLine(StockAdjustment header, StockAdjustmentLineItem line) {
        int totalUnits = line.getQuantity().abs().intValue();
        if (totalUnits == 0) return;

        String[] providedSns = StringUtils.hasText(line.getSerialNumber())
                ? line.getSerialNumber().split(",")
                : new String[0];

        List<String> finalSns = new ArrayList<>();

        for (int i = 0; i < totalUnits; i++) {
            String sn = null;
            if (i < providedSns.length) {
                sn = providedSns[i].trim();
            } else if (line.getQuantity().signum() > 0) {
                sn = SerialNumberGenerator.generate();
            }
            if (sn != null) finalSns.add(sn);

            BigDecimal unitQty = line.getQuantity().signum() < 0 ? new BigDecimal("-1") : BigDecimal.ONE;
            StockMovementPayload payload = buildBasePayload(header, line);
            payload.setQuantity(unitQty);
            payload.setSerialNumber(sn);
            stockService.adjust(payload);
        }
    }

    private void processStandardLine(StockAdjustment header, StockAdjustmentLineItem line) {
        StockMovementPayload payload = buildBasePayload(header, line);
        payload.setQuantity(line.getQuantity());
        payload.setSerialNumber(line.getSerialNumber());
        stockService.adjust(payload);
    }

    private StockMovementPayload buildBasePayload(StockAdjustment header, StockAdjustmentLineItem line) {
        return StockMovementPayload.builder()
                .productId(line.getProductId())
                .containerId(line.getContainerId())
                .movementType(MovementType.ADJUSTMENT)
                .referenceType(ReferenceType.STOCK_ADJUSTMENT)
                .referenceId(header.getId())
                .referenceCode(header.getCode())
                .currencyId(header.getCurrencyId())
                .exchangeRate(header.getExchangeRate())
                .netPrice(line.getUnitCost())
                .transactionDate(header.getTransactionDate().atStartOfDay())
                .build();
    }
}
