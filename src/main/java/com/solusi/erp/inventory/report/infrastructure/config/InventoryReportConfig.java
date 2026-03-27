package com.solusi.erp.inventory.report.infrastructure.config;

import com.solusi.erp.inventory.mapper.InventoryMovementMapper;
import com.solusi.erp.inventory.report.application.usecase.query.GetOnHandDetailUseCase;
import com.solusi.erp.inventory.report.application.usecase.query.GetOnHandDetailUseCaseImpl;
import com.solusi.erp.inventory.report.application.usecase.query.GetOnHandSummaryUseCase;
import com.solusi.erp.inventory.report.application.usecase.query.GetOnHandSummaryUseCaseImpl;
import com.solusi.erp.inventory.report.application.usecase.query.GetStockCardUseCase;
import com.solusi.erp.inventory.report.application.usecase.query.GetStockCardUseCaseImpl;
import com.solusi.erp.inventory.repository.InventoryMovementRepository;
import com.solusi.erp.inventory.repository.StockBalanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Composition Root for InventoryReport module (read-only).
 * Each use case gets its own TransactionTemplate instance to prevent state mutation bugs.
 */
@Configuration
@RequiredArgsConstructor
public class InventoryReportConfig {

    private final StockBalanceRepository stockBalanceRepository;
    private final InventoryMovementRepository inventoryMovementRepository;
    private final InventoryMovementMapper inventoryMovementMapper;
    private final PlatformTransactionManager txManager;

    @Bean
    public GetOnHandSummaryUseCase getOnHandSummaryUseCase() {
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return (keyword, pageable) -> tx.execute(s ->
                new GetOnHandSummaryUseCaseImpl(stockBalanceRepository).execute(keyword, pageable));
    }

    @Bean
    public GetOnHandDetailUseCase getOnHandDetailUseCase() {
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return productId -> tx.execute(s ->
                new GetOnHandDetailUseCaseImpl(stockBalanceRepository).execute(productId));
    }

    @Bean
    public GetStockCardUseCase getStockCardUseCase() {
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return (filter, pageable) -> tx.execute(s ->
                new GetStockCardUseCaseImpl(inventoryMovementRepository, inventoryMovementMapper).execute(filter, pageable));
    }
}
