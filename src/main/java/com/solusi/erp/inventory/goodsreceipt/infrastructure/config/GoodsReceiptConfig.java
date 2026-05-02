package com.solusi.erp.inventory.goodsreceipt.infrastructure.config;

import com.solusi.erp.accounting.journal.application.usecase.command.PostJournalForEventUseCase;
import com.solusi.erp.accounting.period.application.usecase.query.EnsureOpenPeriodForDateUseCase;
import com.solusi.erp.core.infrastructure.sequence.SequenceGeneratorService;
import com.solusi.erp.inventory.goodsreceipt.application.usecase.command.*;
import com.solusi.erp.inventory.goodsreceipt.application.usecase.query.*;
import com.solusi.erp.inventory.goodsreceipt.domain.port.GoodsReceiptReferenceLookupProvider;
import com.solusi.erp.inventory.goodsreceipt.domain.port.GoodsReceiptSourceResolver;
import com.solusi.erp.inventory.goodsreceipt.infrastructure.adapter.GoodsReceiptReferenceLookupProviderImpl;
import com.solusi.erp.inventory.goodsreceipt.domain.repository.GoodsReceiptRepository;
import com.solusi.erp.inventory.goodsreceipt.infrastructure.adapter.PurchaseOrderGoodsReceiptSourceResolver;
import com.solusi.erp.inventory.goodsreceipt.infrastructure.adapter.GoodsReceiptRepositoryImpl;
import com.solusi.erp.inventory.goodsreceipt.infrastructure.persistence.GoodsReceiptJpaRepository;
import com.solusi.erp.inventory.goodsreceipt.infrastructure.persistence.GoodsReceiptPersistenceMapper;
import com.solusi.erp.inventory.goodsreceipt.infrastructure.service.GoodsReceiptSourceResolverRegistry;
import com.solusi.erp.inventory.product.domain.repository.ProductRepository;
import com.solusi.erp.inventory.stock.domain.port.StockService;
import com.solusi.erp.inventory.uomconversion.domain.port.UomConversionService;
import com.solusi.erp.purchasing.purchaseorder.domain.repository.PurchaseOrderRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

@Configuration
public class GoodsReceiptConfig {

    @Bean
    public GoodsReceiptRepository goodsReceiptRepository(GoodsReceiptJpaRepository jpaRepository,
                                                         GoodsReceiptPersistenceMapper mapper) {
        return new GoodsReceiptRepositoryImpl(jpaRepository, mapper);
    }

    @Bean
    public CreateGoodsReceiptUseCase createGoodsReceiptUseCase(GoodsReceiptRepository repository,
                                                               PurchaseOrderRepository poRepository,
                                                               SequenceGeneratorService sequenceService) {
        return new CreateGoodsReceiptUseCaseImpl(repository, poRepository, sequenceService);
    }

    @Bean
    public UpdateGoodsReceiptUseCase updateGoodsReceiptUseCase(GoodsReceiptRepository repository,
                                                               PurchaseOrderRepository poRepository) {
        return new UpdateGoodsReceiptUseCaseImpl(repository, poRepository);
    }

    @Bean
    public DeleteGoodsReceiptUseCase deleteGoodsReceiptUseCase(GoodsReceiptRepository repository) {
        return new DeleteGoodsReceiptUseCaseImpl(repository);
    }

    @Bean
    public CompleteGoodsReceiptUseCase completeGoodsReceiptUseCase(GoodsReceiptRepository repository,
                                                                   PurchaseOrderRepository poRepository,
                                                                   EnsureOpenPeriodForDateUseCase ensureOpenPeriod,
                                                                   StockService stockService,
                                                                   UomConversionService uomConversionService,
                                                                   PostJournalForEventUseCase postJournalForEventUseCase,
                                                                   PlatformTransactionManager txManager) {
        CompleteGoodsReceiptUseCase pure = new CompleteGoodsReceiptUseCaseImpl(
                repository, poRepository, ensureOpenPeriod, stockService, uomConversionService, postJournalForEventUseCase);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return id -> tx.execute(status -> { pure.execute(id); return null; });
    }

    @Bean
    public GetGoodsReceiptUseCase getGoodsReceiptUseCase(GoodsReceiptRepository repository) {
        return new GetGoodsReceiptUseCaseImpl(repository);
    }

    @Bean
    public FindGoodsReceiptsUseCase findGoodsReceiptsUseCase(GoodsReceiptRepository repository) {
        return new FindGoodsReceiptsUseCaseImpl(repository);
    }

    @Bean
    public CountGoodsReceiptsByPoUseCase countGoodsReceiptsByPoUseCase(GoodsReceiptRepository repository) {
        return new CountGoodsReceiptsByPoUseCaseImpl(repository);
    }

    @Bean
    public GoodsReceiptSourceResolver purchaseOrderGoodsReceiptSourceResolver(PurchaseOrderRepository poRepository,
                                                                             ProductRepository productRepository) {
        return new PurchaseOrderGoodsReceiptSourceResolver(poRepository, productRepository);
    }

    @Bean
    public GoodsReceiptReferenceLookupProvider goodsReceiptReferenceLookupProvider(PurchaseOrderRepository poRepository) {
        return new GoodsReceiptReferenceLookupProviderImpl(poRepository);
    }

    @Bean
    public GoodsReceiptSourceResolverRegistry goodsReceiptSourceResolverRegistry(List<GoodsReceiptSourceResolver> resolvers) {
        return new GoodsReceiptSourceResolverRegistry(resolvers);
    }

    @Bean
    public GetGoodsReceiptCreateViewUseCase getGoodsReceiptCreateViewUseCase(GoodsReceiptSourceResolverRegistry registry) {
        return new GetGoodsReceiptCreateViewUseCaseImpl(registry);
    }

    @Bean
    public GetGoodsReceiptEditViewUseCase getGoodsReceiptEditViewUseCase(GoodsReceiptRepository repository) {
        return new GetGoodsReceiptEditViewUseCaseImpl(repository);
    }
}
