package com.solusi.erp.inventory.adjustment.application.usecase.command;

import com.solusi.erp.inventory.adjustment.domain.model.AdjustmentStatus;
import com.solusi.erp.inventory.adjustment.domain.model.StockAdjustment;
import com.solusi.erp.inventory.adjustment.domain.repository.StockAdjustmentRepository;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

public class DeleteStockAdjustmentUseCaseImpl implements DeleteStockAdjustmentUseCase {

    private final StockAdjustmentRepository repository;
    private final MessageSource messageSource;

    public DeleteStockAdjustmentUseCaseImpl(StockAdjustmentRepository repository, MessageSource messageSource) {
        this.repository = repository;
        this.messageSource = messageSource;
    }

    @Override
    public void execute(Long id) {
        StockAdjustment domain = repository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        messageSource.getMessage("msg.error.notfound", null, LocaleContextHolder.getLocale())));
        if (domain.getStatus() == AdjustmentStatus.COMPLETED) {
            throw new RuntimeException(
                    messageSource.getMessage("msg.error.stock-adjustment.already-completed", null, LocaleContextHolder.getLocale()));
        }
        repository.deleteById(id);
    }
}
