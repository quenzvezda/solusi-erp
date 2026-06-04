package com.solusi.erp.accountspayable.debitmemo.infrastructure.config;

import com.solusi.erp.accountspayable.debitmemo.application.usecase.command.CreateDebitMemoFromPurchaseReturnUseCase;
import com.solusi.erp.accountspayable.debitmemo.application.usecase.command.CreateDebitMemoFromPurchaseReturnUseCaseImpl;
import com.solusi.erp.accountspayable.debitmemo.application.usecase.query.FindDebitMemoByPurchaseReturnUseCase;
import com.solusi.erp.accountspayable.debitmemo.application.usecase.query.FindDebitMemoByPurchaseReturnUseCaseImpl;
import com.solusi.erp.accountspayable.debitmemo.application.usecase.query.FindDebitMemosUseCase;
import com.solusi.erp.accountspayable.debitmemo.application.usecase.query.FindDebitMemosUseCaseImpl;
import com.solusi.erp.accountspayable.debitmemo.application.usecase.query.GetDebitMemoDetailUseCase;
import com.solusi.erp.accountspayable.debitmemo.application.usecase.query.GetDebitMemoDetailUseCaseImpl;
import com.solusi.erp.accountspayable.debitmemo.domain.repository.DebitMemoRepository;
import com.solusi.erp.accountspayable.debitmemo.infrastructure.adapter.DebitMemoRepositoryImpl;
import com.solusi.erp.accountspayable.debitmemo.infrastructure.persistence.DebitMemoJpaRepository;
import com.solusi.erp.accountspayable.debitmemo.infrastructure.persistence.DebitMemoPersistenceMapper;
import com.solusi.erp.core.infrastructure.sequence.SequenceGeneratorService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
public class DebitMemoConfig {

    @Bean
    public DebitMemoRepository debitMemoRepository(DebitMemoJpaRepository jpaRepository,
                                                   DebitMemoPersistenceMapper mapper) {
        return new DebitMemoRepositoryImpl(jpaRepository, mapper);
    }

    @Bean
    public CreateDebitMemoFromPurchaseReturnUseCase createDebitMemoFromPurchaseReturnUseCase(
            DebitMemoRepository repository,
            SequenceGeneratorService sequenceGeneratorService,
            PlatformTransactionManager txManager) {
        CreateDebitMemoFromPurchaseReturnUseCase pure = new CreateDebitMemoFromPurchaseReturnUseCaseImpl(
                repository, sequenceGeneratorService);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return source -> tx.execute(status -> pure.execute(source));
    }

    @Bean
    public FindDebitMemosUseCase findDebitMemosUseCase(DebitMemoRepository repository,
                                                       PlatformTransactionManager txManager) {
        FindDebitMemosUseCase pure = new FindDebitMemosUseCaseImpl(repository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return (keyword, vendorId, settlementStatus, memoDateFrom, memoDateTo, pageable) ->
                tx.execute(status -> pure.execute(keyword, vendorId, settlementStatus, memoDateFrom, memoDateTo, pageable));
    }

    @Bean
    public GetDebitMemoDetailUseCase getDebitMemoDetailUseCase(DebitMemoRepository repository,
                                                               PlatformTransactionManager txManager) {
        GetDebitMemoDetailUseCase pure = new GetDebitMemoDetailUseCaseImpl(repository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return id -> tx.execute(status -> pure.execute(id));
    }

    @Bean
    public FindDebitMemoByPurchaseReturnUseCase findDebitMemoByPurchaseReturnUseCase(DebitMemoRepository repository,
                                                                                     PlatformTransactionManager txManager) {
        FindDebitMemoByPurchaseReturnUseCase pure = new FindDebitMemoByPurchaseReturnUseCaseImpl(repository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return purchaseReturnId -> tx.execute(status -> pure.execute(purchaseReturnId));
    }
}
