package com.solusi.erp.purchasing.purchasereturn.infrastructure.config;

import com.solusi.erp.core.infrastructure.sequence.SequenceGeneratorService;
import com.solusi.erp.accountspayable.debitmemo.application.usecase.command.CreateDebitMemoFromPurchaseReturnUseCase;
import com.solusi.erp.inventory.goodsissue.domain.port.PurchaseReturnGoodsIssueSourcePort;
import com.solusi.erp.inventory.goodsissue.application.usecase.command.CompleteGoodsIssueUseCase;
import com.solusi.erp.inventory.goodsissue.domain.repository.GoodsIssueRepository;
import com.solusi.erp.inventory.goodsissue.infrastructure.service.GoodsIssueSourceResolverRegistry;
import com.solusi.erp.accounting.period.application.usecase.query.EnsureOpenPeriodForDateUseCase;
import com.solusi.erp.inventory.stock.domain.port.InventoryReservationService;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.command.CancelApprovedPurchaseReturnUseCase;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.command.CancelApprovedPurchaseReturnUseCaseImpl;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.command.CancelDraftPurchaseReturnUseCase;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.command.CancelDraftPurchaseReturnUseCaseImpl;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.command.CancelPurchaseReturnSubmissionUseCase;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.command.CancelPurchaseReturnSubmissionUseCaseImpl;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.command.CreatePurchaseReturnUseCase;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.command.CreatePurchaseReturnUseCaseImpl;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.command.ConfirmPurchaseReturnUseCase;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.command.ConfirmPurchaseReturnUseCaseImpl;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.command.SubmitPurchaseReturnUseCase;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.command.SubmitPurchaseReturnUseCaseImpl;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.command.UpdatePurchaseReturnUseCase;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.command.UpdatePurchaseReturnUseCaseImpl;
import com.solusi.erp.purchasing.purchasereturn.domain.port.PurchaseReturnApprovalCancellationPort;
import com.solusi.erp.purchasing.purchasereturn.domain.port.PurchaseReturnEventPublisher;
import com.solusi.erp.purchasing.purchasereturn.domain.repository.PurchaseReturnRepository;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.query.FindEligiblePurchaseReturnGoodsReceiptsUseCase;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.query.FindEligiblePurchaseReturnGoodsReceiptsUseCaseImpl;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.query.FindPurchaseReturnGrLineSlicesUseCase;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.query.FindPurchaseReturnGrLineSlicesUseCaseImpl;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.query.FindPurchaseReturnSerialsUseCase;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.query.FindPurchaseReturnSerialsUseCaseImpl;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.query.GetEligiblePurchaseReturnPurchaseOrderLookupUseCase;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.query.GetEligiblePurchaseReturnPurchaseOrderLookupUseCaseImpl;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.query.GetPurchaseReturnCreateViewUseCase;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.query.GetPurchaseReturnCreateViewUseCaseImpl;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.query.FindPurchaseReturnsUseCase;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.query.FindPurchaseReturnsUseCaseImpl;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.query.GetPurchaseReturnEditViewUseCase;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.query.GetPurchaseReturnEditViewUseCaseImpl;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.query.GetPurchaseReturnUseCase;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.query.GetPurchaseReturnUseCaseImpl;
import com.solusi.erp.purchasing.purchasereturn.domain.port.PurchaseReturnSourceQueryPort;
import com.solusi.erp.purchasing.purchasereturn.infrastructure.adapter.PurchaseReturnSourceQueryAdapter;
import com.solusi.erp.purchasing.purchasereturn.infrastructure.adapter.PurchaseReturnRepositoryImpl;
import com.solusi.erp.purchasing.purchasereturn.infrastructure.adapter.PurchaseReturnGoodsIssueSourceAdapter;
import com.solusi.erp.purchasing.purchasereturn.infrastructure.persistence.PurchaseReturnJpaRepository;
import com.solusi.erp.purchasing.purchasereturn.infrastructure.persistence.PurchaseReturnPersistenceMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
public class PurchaseReturnConfig {

    @Bean
    public PurchaseReturnRepository purchaseReturnDomainRepository(
            PurchaseReturnJpaRepository jpaRepository,
            PurchaseReturnPersistenceMapper mapper) {
        return new PurchaseReturnRepositoryImpl(jpaRepository, mapper);
    }

    @Bean
    public PurchaseReturnSourceQueryPort purchaseReturnSourceQueryPort(
            NamedParameterJdbcTemplate jdbcTemplate) {
        return new PurchaseReturnSourceQueryAdapter(jdbcTemplate);
    }

    @Bean
    public PurchaseReturnGoodsIssueSourcePort purchaseReturnGoodsIssueSourcePort(
            NamedParameterJdbcTemplate jdbcTemplate) {
        return new PurchaseReturnGoodsIssueSourceAdapter(jdbcTemplate);
    }

    @Bean
    public FindEligiblePurchaseReturnGoodsReceiptsUseCase findEligiblePurchaseReturnGoodsReceiptsUseCase(
            PurchaseReturnSourceQueryPort queryPort) {
        return new FindEligiblePurchaseReturnGoodsReceiptsUseCaseImpl(queryPort);
    }

    @Bean
    public GetEligiblePurchaseReturnPurchaseOrderLookupUseCase getEligiblePurchaseReturnPurchaseOrderLookupUseCase(
            PurchaseReturnSourceQueryPort queryPort) {
        return new GetEligiblePurchaseReturnPurchaseOrderLookupUseCaseImpl(queryPort);
    }

    @Bean
    public GetPurchaseReturnCreateViewUseCase getPurchaseReturnCreateViewUseCase(
            PurchaseReturnSourceQueryPort queryPort) {
        return new GetPurchaseReturnCreateViewUseCaseImpl(queryPort);
    }

    @Bean
    public FindPurchaseReturnGrLineSlicesUseCase findPurchaseReturnGrLineSlicesUseCase(
            PurchaseReturnSourceQueryPort queryPort) {
        return new FindPurchaseReturnGrLineSlicesUseCaseImpl(queryPort);
    }

    @Bean
    public FindPurchaseReturnSerialsUseCase findPurchaseReturnSerialsUseCase(
            PurchaseReturnSourceQueryPort queryPort) {
        return new FindPurchaseReturnSerialsUseCaseImpl(queryPort);
    }

    @Bean
    public CreatePurchaseReturnUseCase createPurchaseReturnUseCase(
            PurchaseReturnRepository repository,
            SequenceGeneratorService sequenceGeneratorService,
            PurchaseReturnSourceQueryPort queryPort,
            PlatformTransactionManager txManager) {
        CreatePurchaseReturnUseCaseImpl pure = new CreatePurchaseReturnUseCaseImpl(
                repository, sequenceGeneratorService, queryPort);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (goodsReceiptId, returnDate, reason, note, lines) -> tx.execute(
                status -> pure.execute(goodsReceiptId, returnDate, reason, note, lines));
    }

    @Bean
    public UpdatePurchaseReturnUseCase updatePurchaseReturnUseCase(
            PurchaseReturnRepository repository,
            PurchaseReturnSourceQueryPort queryPort,
            PlatformTransactionManager txManager) {
        UpdatePurchaseReturnUseCaseImpl pure = new UpdatePurchaseReturnUseCaseImpl(repository, queryPort);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (id, returnDate, reason, note, lines) -> tx.execute(
                status -> pure.execute(id, returnDate, reason, note, lines));
    }

    @Bean
    public CancelDraftPurchaseReturnUseCase cancelDraftPurchaseReturnUseCase(
            PurchaseReturnRepository repository,
            PlatformTransactionManager txManager) {
        CancelDraftPurchaseReturnUseCaseImpl pure = new CancelDraftPurchaseReturnUseCaseImpl(repository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return id -> tx.execute(status -> pure.execute(id));
    }

    @Bean
    public SubmitPurchaseReturnUseCase submitPurchaseReturnUseCase(
            PurchaseReturnRepository repository,
            InventoryReservationService reservationService,
            PurchaseReturnEventPublisher eventPublisher,
            PlatformTransactionManager txManager) {
        SubmitPurchaseReturnUseCaseImpl pure = new SubmitPurchaseReturnUseCaseImpl(
                repository, reservationService, eventPublisher);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (id, submitterUserId, requesterPartyId, approverId) -> tx.execute(
                status -> pure.execute(id, submitterUserId, requesterPartyId, approverId));
    }

    @Bean
    public CancelPurchaseReturnSubmissionUseCase cancelPurchaseReturnSubmissionUseCase(
            PurchaseReturnRepository repository,
            InventoryReservationService reservationService,
            PurchaseReturnApprovalCancellationPort approvalCancellationPort,
            PlatformTransactionManager txManager) {
        CancelPurchaseReturnSubmissionUseCaseImpl pure = new CancelPurchaseReturnSubmissionUseCaseImpl(
                repository, reservationService, approvalCancellationPort);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (id, actorUserId, notes) -> tx.execute(
                status -> pure.execute(id, actorUserId, notes));
    }

    @Bean
    public CancelApprovedPurchaseReturnUseCase cancelApprovedPurchaseReturnUseCase(
            PurchaseReturnRepository repository,
            InventoryReservationService reservationService,
            PlatformTransactionManager txManager) {
        CancelApprovedPurchaseReturnUseCaseImpl pure = new CancelApprovedPurchaseReturnUseCaseImpl(
                repository, reservationService);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return id -> tx.execute(status -> pure.execute(id));
    }

    @Bean
    public ConfirmPurchaseReturnUseCase confirmPurchaseReturnUseCase(
            PurchaseReturnRepository purchaseReturnRepository,
            PurchaseReturnGoodsIssueSourcePort sourcePort,
            GoodsIssueSourceResolverRegistry resolverRegistry,
            SequenceGeneratorService sequenceGeneratorService,
            GoodsIssueRepository goodsIssueRepository,
            CompleteGoodsIssueUseCase completeGoodsIssueUseCase,
            EnsureOpenPeriodForDateUseCase ensureOpenPeriodForDateUseCase,
            CreateDebitMemoFromPurchaseReturnUseCase createDebitMemoFromPurchaseReturnUseCase,
            PlatformTransactionManager txManager) {
        ConfirmPurchaseReturnUseCaseImpl pure = new ConfirmPurchaseReturnUseCaseImpl(
                purchaseReturnRepository, sourcePort, resolverRegistry, sequenceGeneratorService,
                goodsIssueRepository, completeGoodsIssueUseCase, ensureOpenPeriodForDateUseCase,
                createDebitMemoFromPurchaseReturnUseCase);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return id -> tx.execute(status -> pure.execute(id));
    }

    @Bean
    public FindPurchaseReturnsUseCase findPurchaseReturnsUseCase(PurchaseReturnRepository repository) {
        return new FindPurchaseReturnsUseCaseImpl(repository);
    }

    @Bean
    public GetPurchaseReturnUseCase getPurchaseReturnUseCase(PurchaseReturnRepository repository) {
        return new GetPurchaseReturnUseCaseImpl(repository);
    }

    @Bean
    public GetPurchaseReturnEditViewUseCase getPurchaseReturnEditViewUseCase(
            PurchaseReturnRepository repository,
            PurchaseReturnSourceQueryPort queryPort) {
        return new GetPurchaseReturnEditViewUseCaseImpl(repository, queryPort);
    }
}
