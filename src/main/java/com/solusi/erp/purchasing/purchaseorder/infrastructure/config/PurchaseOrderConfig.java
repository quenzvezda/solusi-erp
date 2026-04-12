package com.solusi.erp.purchasing.purchaseorder.infrastructure.config;

import com.solusi.erp.core.infrastructure.sequence.SequenceGeneratorService;
import com.solusi.erp.purchasing.purchaserequisition.domain.repository.PurchaseRequisitionRepository;
import com.solusi.erp.purchasing.purchaseorder.application.usecase.command.*;
import com.solusi.erp.purchasing.purchaseorder.application.usecase.query.*;
import com.solusi.erp.purchasing.purchaseorder.domain.port.PurchaseOrderEventPublisher;
import com.solusi.erp.purchasing.purchaseorder.domain.repository.PurchaseOrderRepository;
import com.solusi.erp.purchasing.purchaseorder.infrastructure.adapter.PurchaseOrderRepositoryImpl;
import com.solusi.erp.purchasing.purchaseorder.infrastructure.persistence.PurchaseOrderJpaRepository;
import com.solusi.erp.purchasing.purchaseorder.infrastructure.persistence.PurchaseOrderPersistenceMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
public class PurchaseOrderConfig {

    @Bean
    public PurchaseOrderRepository purchaseOrderDomainRepository(
            PurchaseOrderJpaRepository jpaRepository,
            PurchaseOrderPersistenceMapper mapper) {
        return new PurchaseOrderRepositoryImpl(jpaRepository, mapper);
    }

    @Bean
    public CreatePurchaseOrderUseCase createPurchaseOrderUseCase(
            PurchaseOrderRepository purchaseOrderDomainRepository,
            SequenceGeneratorService sequenceGeneratorService,
            PurchaseRequisitionRepository purchaseRequisitionRepository,
            PlatformTransactionManager txManager) {
        CreatePurchaseOrderUseCase pure = new CreatePurchaseOrderUseCaseImpl(
            purchaseOrderDomainRepository, sequenceGeneratorService, purchaseRequisitionRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (orderDate, expectedDate, supplierId, facilityId, currencyId,
                exchangeRate, paymentTermDays, prId, poType, note, lines) ->
            tx.execute(status -> pure.execute(orderDate, expectedDate, supplierId,
                facilityId, currencyId, exchangeRate, paymentTermDays, prId, poType, note, lines));
    }

    @Bean
    public UpdatePurchaseOrderUseCase updatePurchaseOrderUseCase(
            PurchaseOrderRepository purchaseOrderDomainRepository,
            PlatformTransactionManager txManager) {
        UpdatePurchaseOrderUseCase pure = new UpdatePurchaseOrderUseCaseImpl(
            purchaseOrderDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (id, orderDate, expectedDate, facilityId, currencyId,
                exchangeRate, paymentTermDays, note, lines) ->
            tx.execute(status -> pure.execute(id, orderDate, expectedDate,
                facilityId, currencyId, exchangeRate, paymentTermDays, note, lines));
    }

    @Bean
    public DeletePurchaseOrderUseCase deletePurchaseOrderUseCase(
            PurchaseOrderRepository purchaseOrderDomainRepository,
            PlatformTransactionManager txManager) {
        DeletePurchaseOrderUseCase pure = new DeletePurchaseOrderUseCaseImpl(
            purchaseOrderDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (id) -> tx.executeWithoutResult(status -> pure.execute(id));
    }

    @Bean
    public SubmitPurchaseOrderUseCase submitPurchaseOrderUseCase(
            PurchaseOrderRepository purchaseOrderDomainRepository,
            PurchaseOrderEventPublisher eventPublisher,
            PlatformTransactionManager txManager) {
        SubmitPurchaseOrderUseCase pure = new SubmitPurchaseOrderUseCaseImpl(
            purchaseOrderDomainRepository, eventPublisher);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (id, approverId) ->
            tx.execute(status -> pure.execute(id, approverId));
    }

    @Bean
    public SendPurchaseOrderUseCase sendPurchaseOrderUseCase(
            PurchaseOrderRepository purchaseOrderDomainRepository,
            PlatformTransactionManager txManager) {
        SendPurchaseOrderUseCase pure = new SendPurchaseOrderUseCaseImpl(
            purchaseOrderDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (id) -> tx.execute(status -> pure.execute(id));
    }

    @Bean
    public CancelPurchaseOrderUseCase cancelPurchaseOrderUseCase(
            PurchaseOrderRepository purchaseOrderDomainRepository,
            PlatformTransactionManager txManager) {
        CancelPurchaseOrderUseCase pure = new CancelPurchaseOrderUseCaseImpl(
            purchaseOrderDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (id) -> tx.execute(status -> pure.execute(id));
    }

    @Bean
    public FindPurchaseOrdersUseCase findPurchaseOrdersUseCase(
            PurchaseOrderRepository purchaseOrderDomainRepository,
            PlatformTransactionManager txManager) {
        FindPurchaseOrdersUseCase pure = new FindPurchaseOrdersUseCaseImpl(
            purchaseOrderDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return (keyword, pageable) -> tx.execute(status -> pure.execute(keyword, pageable));
    }

    @Bean
    public GetPurchaseOrderEditViewUseCase getPurchaseOrderEditViewUseCase(
            PurchaseOrderRepository purchaseOrderDomainRepository,
            PlatformTransactionManager txManager) {
        GetPurchaseOrderEditViewUseCase pure = new GetPurchaseOrderEditViewUseCaseImpl(
            purchaseOrderDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return (id) -> tx.execute(status -> pure.execute(id));
    }
}

