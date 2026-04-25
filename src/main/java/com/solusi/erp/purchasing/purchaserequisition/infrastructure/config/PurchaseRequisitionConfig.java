package com.solusi.erp.purchasing.purchaserequisition.infrastructure.config;

import com.solusi.erp.core.infrastructure.sequence.SequenceGeneratorService;
import com.solusi.erp.purchasing.purchaserequisition.application.usecase.command.*;
import com.solusi.erp.purchasing.purchaserequisition.application.usecase.query.*;
import com.solusi.erp.purchasing.purchaserequisition.domain.port.PurchaseRequisitionEventPublisher;
import com.solusi.erp.purchasing.purchaserequisition.domain.repository.PurchaseRequisitionRepository;
import com.solusi.erp.purchasing.purchaserequisition.infrastructure.adapter.PurchaseRequisitionRepositoryImpl;
import com.solusi.erp.purchasing.purchaserequisition.infrastructure.persistence.PurchaseRequisitionJpaRepository;
import com.solusi.erp.purchasing.purchaserequisition.infrastructure.persistence.PurchaseRequisitionPersistenceMapper;
import com.solusi.erp.purchasing.supplierpricelist.domain.repository.SupplierPriceListRepository;
import com.solusi.erp.purchasing.supplierpricelist.domain.service.SupplierPriceListResolutionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
public class PurchaseRequisitionConfig {

    @Bean
    public PurchaseRequisitionRepository purchaseRequisitionDomainRepository(
            PurchaseRequisitionJpaRepository jpaRepository,
            PurchaseRequisitionPersistenceMapper mapper) {
        return new PurchaseRequisitionRepositoryImpl(jpaRepository, mapper);
    }

    @Bean
    public CreatePurchaseRequisitionUseCase createPurchaseRequisitionUseCase(
            PurchaseRequisitionRepository purchaseRequisitionDomainRepository,
            SequenceGeneratorService sequenceGeneratorService,
            PlatformTransactionManager txManager) {
        CreatePurchaseRequisitionUseCase pure = new CreatePurchaseRequisitionUseCaseImpl(
            purchaseRequisitionDomainRepository, sequenceGeneratorService);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (requestDate, requesterId, facilityId, department, priority, note, suggestedSupplierId, currencyId, lines) ->
            tx.execute(status -> pure.execute(requestDate, requesterId, facilityId,
                department, priority, note, suggestedSupplierId, currencyId, lines));
    }

    @Bean
    public UpdatePurchaseRequisitionUseCase updatePurchaseRequisitionUseCase(
            PurchaseRequisitionRepository purchaseRequisitionDomainRepository,
            PlatformTransactionManager txManager) {
        UpdatePurchaseRequisitionUseCase pure = new UpdatePurchaseRequisitionUseCaseImpl(
            purchaseRequisitionDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (id, requestDate, facilityId, department, priority, note, suggestedSupplierId, currencyId, lines) ->
            tx.execute(status -> pure.execute(id, requestDate, facilityId,
                department, priority, note, suggestedSupplierId, currencyId, lines));
    }

    @Bean
    public DeletePurchaseRequisitionUseCase deletePurchaseRequisitionUseCase(
            PurchaseRequisitionRepository purchaseRequisitionDomainRepository,
            PlatformTransactionManager txManager) {
        DeletePurchaseRequisitionUseCase pure = new DeletePurchaseRequisitionUseCaseImpl(
            purchaseRequisitionDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (id) -> tx.executeWithoutResult(status -> pure.execute(id));
    }

    @Bean
    public SubmitPurchaseRequisitionUseCase submitPurchaseRequisitionUseCase(
            PurchaseRequisitionRepository purchaseRequisitionDomainRepository,
            PurchaseRequisitionEventPublisher eventPublisher,
            PlatformTransactionManager txManager) {
        SubmitPurchaseRequisitionUseCase pure = new SubmitPurchaseRequisitionUseCaseImpl(
            purchaseRequisitionDomainRepository, eventPublisher);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (id, approverId) ->
            tx.execute(status -> pure.execute(id, approverId));
    }

    @Bean
    public CancelPurchaseRequisitionUseCase cancelPurchaseRequisitionUseCase(
            PurchaseRequisitionRepository purchaseRequisitionDomainRepository,
            PlatformTransactionManager txManager) {
        CancelPurchaseRequisitionUseCase pure = new CancelPurchaseRequisitionUseCaseImpl(
            purchaseRequisitionDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (id) -> tx.execute(status -> pure.execute(id));
    }

    @Bean
    public FindPurchaseRequisitionsUseCase findPurchaseRequisitionsUseCase(
            PurchaseRequisitionRepository purchaseRequisitionDomainRepository,
            PlatformTransactionManager txManager) {
        FindPurchaseRequisitionsUseCase pure = new FindPurchaseRequisitionsUseCaseImpl(
            purchaseRequisitionDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return (keyword, pageable) -> tx.execute(status -> pure.execute(keyword, pageable));
    }

    @Bean
    public GetPurchaseRequisitionEditViewUseCase getPurchaseRequisitionEditViewUseCase(
            PurchaseRequisitionRepository purchaseRequisitionDomainRepository,
            PlatformTransactionManager txManager) {
        GetPurchaseRequisitionEditViewUseCase pure = new GetPurchaseRequisitionEditViewUseCaseImpl(
            purchaseRequisitionDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return (id) -> tx.execute(status -> pure.execute(id));
    }

    @Bean
    public ResolvePurchaseRequisitionSplPriceUseCase resolvePurchaseRequisitionSplPriceUseCase(
            SupplierPriceListRepository supplierPriceListRepository,
            PlatformTransactionManager txManager) {
        ResolvePurchaseRequisitionSplPriceUseCase pure = new ResolvePurchaseRequisitionSplPriceUseCaseImpl(
                new SupplierPriceListResolutionService(supplierPriceListRepository)
        );
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return (supplierId, productId, uomId, currencyId, requiredDate) ->
                tx.execute(status -> pure.execute(supplierId, productId, uomId, currencyId, requiredDate));
    }
}
