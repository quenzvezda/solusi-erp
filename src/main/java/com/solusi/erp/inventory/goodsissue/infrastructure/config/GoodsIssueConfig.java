package com.solusi.erp.inventory.goodsissue.infrastructure.config;

import com.solusi.erp.accounting.journal.application.usecase.command.PostJournalForEventUseCase;
import com.solusi.erp.accounting.journal.application.usecase.command.ReversePostedJournalUseCase;
import com.solusi.erp.accounting.journal.domain.repository.JournalEntryRepository;
import com.solusi.erp.accounting.period.application.usecase.query.EnsureOpenPeriodForDateUseCase;
import com.solusi.erp.core.infrastructure.sequence.SequenceGeneratorService;
import com.solusi.erp.inventory.goodsissue.application.usecase.command.CancelGoodsIssueUseCase;
import com.solusi.erp.inventory.goodsissue.application.usecase.command.CancelGoodsIssueUseCaseImpl;
import com.solusi.erp.inventory.goodsissue.application.usecase.command.CompleteGoodsIssueUseCase;
import com.solusi.erp.inventory.goodsissue.application.usecase.command.CompleteGoodsIssueUseCaseImpl;
import com.solusi.erp.inventory.goodsissue.application.usecase.command.CreateGoodsIssueUseCase;
import com.solusi.erp.inventory.goodsissue.application.usecase.command.CreateGoodsIssueUseCaseImpl;
import com.solusi.erp.inventory.goodsissue.application.usecase.command.DeleteGoodsIssueUseCase;
import com.solusi.erp.inventory.goodsissue.application.usecase.command.DeleteGoodsIssueUseCaseImpl;
import com.solusi.erp.inventory.goodsissue.application.usecase.command.GoodsIssueInUseChecker;
import com.solusi.erp.inventory.goodsissue.application.usecase.command.UpdateGoodsIssueUseCase;
import com.solusi.erp.inventory.goodsissue.application.usecase.command.UpdateGoodsIssueUseCaseImpl;
import com.solusi.erp.inventory.goodsissue.application.usecase.query.FindGoodsIssuesUseCase;
import com.solusi.erp.inventory.goodsissue.application.usecase.query.FindGoodsIssuesUseCaseImpl;
import com.solusi.erp.inventory.goodsissue.application.usecase.query.GetGoodsIssueCancelViewUseCase;
import com.solusi.erp.inventory.goodsissue.application.usecase.query.GetGoodsIssueCancelViewUseCaseImpl;
import com.solusi.erp.inventory.goodsissue.application.usecase.query.GetGoodsIssueCreateViewUseCase;
import com.solusi.erp.inventory.goodsissue.application.usecase.query.GetGoodsIssueCreateViewUseCaseImpl;
import com.solusi.erp.inventory.goodsissue.application.usecase.query.GetGoodsIssueEditViewUseCase;
import com.solusi.erp.inventory.goodsissue.application.usecase.query.GetGoodsIssueEditViewUseCaseImpl;
import com.solusi.erp.inventory.goodsissue.application.usecase.query.GetGoodsIssueUseCase;
import com.solusi.erp.inventory.goodsissue.application.usecase.query.GetGoodsIssueUseCaseImpl;
import com.solusi.erp.inventory.goodsissue.domain.port.GoodsIssueReferenceLookupProvider;
import com.solusi.erp.inventory.goodsissue.domain.port.GoodsIssueSourceResolver;
import com.solusi.erp.inventory.goodsissue.domain.port.PurchaseReturnGoodsIssueSourcePort;
import com.solusi.erp.inventory.goodsissue.domain.repository.GoodsIssueRepository;
import com.solusi.erp.inventory.goodsissue.infrastructure.adapter.GoodsIssueReferenceLookupProviderImpl;
import com.solusi.erp.inventory.goodsissue.infrastructure.adapter.GoodsIssueRepositoryImpl;
import com.solusi.erp.inventory.goodsissue.infrastructure.adapter.PurchaseReturnGoodsIssueSourceResolver;
import com.solusi.erp.inventory.goodsissue.infrastructure.persistence.GoodsIssueJpaRepository;
import com.solusi.erp.inventory.goodsissue.infrastructure.persistence.GoodsIssuePersistenceMapper;
import com.solusi.erp.inventory.goodsissue.infrastructure.service.GoodsIssueSourceResolverRegistry;
import com.solusi.erp.inventory.container.domain.port.ContainerLookupProvider;
import com.solusi.erp.inventory.facility.domain.port.FacilityLookupProvider;
import com.solusi.erp.inventory.product.domain.port.ProductLookupProvider;
import com.solusi.erp.inventory.stock.domain.port.StockService;
import com.solusi.erp.inventory.stock.domain.port.InventoryReservationService;
import com.solusi.erp.inventory.stock.domain.port.StockMovementReversalService;
import com.solusi.erp.inventory.stock.infrastructure.persistence.InventoryMovementJpaRepository;
import com.solusi.erp.inventory.uom.domain.port.UomLookupProvider;
import com.solusi.erp.inventory.uomconversion.domain.port.UomConversionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

@Configuration
public class GoodsIssueConfig {

    @Bean
    public GoodsIssueRepository goodsIssueRepository(GoodsIssueJpaRepository jpaRepository,
                                                     GoodsIssuePersistenceMapper mapper) {
        return new GoodsIssueRepositoryImpl(jpaRepository, mapper);
    }

    @Bean
    public GoodsIssueReferenceLookupProvider goodsIssueReferenceLookupProvider() {
        return new GoodsIssueReferenceLookupProviderImpl();
    }

    @Bean
    public CreateGoodsIssueUseCase createGoodsIssueUseCase(GoodsIssueRepository repository,
                                                           SequenceGeneratorService sequenceGeneratorService,
                                                           GoodsIssueSourceResolverRegistry registry) {
        return new CreateGoodsIssueUseCaseImpl(repository, sequenceGeneratorService, registry);
    }

    @Bean
    public UpdateGoodsIssueUseCase updateGoodsIssueUseCase(GoodsIssueRepository repository) {
        return new UpdateGoodsIssueUseCaseImpl(repository);
    }

    @Bean
    public DeleteGoodsIssueUseCase deleteGoodsIssueUseCase(GoodsIssueRepository repository) {
        return new DeleteGoodsIssueUseCaseImpl(repository);
    }

    @Bean
    public CompleteGoodsIssueUseCase completeGoodsIssueUseCase(GoodsIssueRepository repository,
                                                               EnsureOpenPeriodForDateUseCase ensureOpenPeriod,
                                                               StockService stockService,
                                                               UomConversionService uomConversionService,
                                                               PostJournalForEventUseCase postJournalForEventUseCase,
                                                               InventoryReservationService reservationService,
                                                               PlatformTransactionManager txManager) {
        CompleteGoodsIssueUseCase pure = new CompleteGoodsIssueUseCaseImpl(
                repository, ensureOpenPeriod, stockService, uomConversionService, postJournalForEventUseCase,
                reservationService);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return id -> tx.execute(status -> { pure.execute(id); return null; });
    }

    @Bean
    public GoodsIssueInUseChecker goodsIssueInUseChecker() {
        return goodsIssue -> { };
    }

    @Bean
    public CancelGoodsIssueUseCase cancelGoodsIssueUseCase(GoodsIssueRepository repository,
                                                           EnsureOpenPeriodForDateUseCase ensureOpenPeriod,
                                                           StockMovementReversalService stockMovementReversalService,
                                                           InventoryMovementJpaRepository inventoryMovementJpaRepository,
                                                           JournalEntryRepository journalEntryRepository,
                                                           ReversePostedJournalUseCase reversePostedJournalUseCase,
                                                           GoodsIssueInUseChecker inUseChecker,
                                                           PlatformTransactionManager txManager) {
        CancelGoodsIssueUseCase pure = new CancelGoodsIssueUseCaseImpl(
                repository,
                ensureOpenPeriod,
                stockMovementReversalService,
                inventoryMovementJpaRepository,
                journalEntryRepository,
                reversePostedJournalUseCase,
                inUseChecker);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return command -> tx.execute(status -> { pure.execute(command); return null; });
    }

    @Bean
    public GoodsIssueSourceResolverRegistry goodsIssueSourceResolverRegistry(List<GoodsIssueSourceResolver> resolvers) {
        return new GoodsIssueSourceResolverRegistry(resolvers);
    }

    @Bean
    public GoodsIssueSourceResolver purchaseReturnGoodsIssueSourceResolver(
            PurchaseReturnGoodsIssueSourcePort sourcePort) {
        return new PurchaseReturnGoodsIssueSourceResolver(sourcePort);
    }

    @Bean
    public GetGoodsIssueCreateViewUseCase getGoodsIssueCreateViewUseCase(GoodsIssueSourceResolverRegistry registry) {
        return new GetGoodsIssueCreateViewUseCaseImpl(registry);
    }

    @Bean
    public GetGoodsIssueUseCase getGoodsIssueUseCase(GoodsIssueRepository repository) {
        return new GetGoodsIssueUseCaseImpl(repository);
    }

    @Bean
    public GetGoodsIssueEditViewUseCase getGoodsIssueEditViewUseCase(GoodsIssueRepository repository) {
        return new GetGoodsIssueEditViewUseCaseImpl(repository);
    }

    @Bean
    public GetGoodsIssueCancelViewUseCase getGoodsIssueCancelViewUseCase(
            GoodsIssueRepository repository,
            InventoryMovementJpaRepository inventoryMovementJpaRepository,
            ProductLookupProvider productLookupProvider,
            UomLookupProvider uomLookupProvider,
            ContainerLookupProvider containerLookupProvider,
            FacilityLookupProvider facilityLookupProvider) {
        return new GetGoodsIssueCancelViewUseCaseImpl(
                repository,
                inventoryMovementJpaRepository,
                productLookupProvider,
                uomLookupProvider,
                containerLookupProvider,
                facilityLookupProvider);
    }

    @Bean
    public FindGoodsIssuesUseCase findGoodsIssuesUseCase(GoodsIssueRepository repository) {
        return new FindGoodsIssuesUseCaseImpl(repository);
    }
}
