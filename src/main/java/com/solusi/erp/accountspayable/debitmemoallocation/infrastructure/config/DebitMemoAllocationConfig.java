package com.solusi.erp.accountspayable.debitmemoallocation.infrastructure.config;

import com.solusi.erp.accountspayable.debitmemoallocation.application.usecase.query.FindDebitMemoAllocationHistoryUseCase;
import com.solusi.erp.accountspayable.debitmemoallocation.application.usecase.query.FindDebitMemoAllocationHistoryUseCaseImpl;
import com.solusi.erp.accountspayable.debitmemoallocation.application.usecase.query.FindDebitMemoAllocationsUseCase;
import com.solusi.erp.accountspayable.debitmemoallocation.application.usecase.query.FindDebitMemoAllocationsUseCaseImpl;
import com.solusi.erp.accountspayable.debitmemoallocation.application.usecase.query.GetDebitMemoAllocationDetailUseCase;
import com.solusi.erp.accountspayable.debitmemoallocation.application.usecase.query.GetDebitMemoAllocationDetailUseCaseImpl;
import com.solusi.erp.accountspayable.debitmemoallocation.domain.repository.DebitMemoAllocationRepository;
import com.solusi.erp.accountspayable.debitmemoallocation.domain.service.DebitMemoAllocationProrationService;
import com.solusi.erp.accountspayable.debitmemoallocation.infrastructure.adapter.DebitMemoAllocationRepositoryImpl;
import com.solusi.erp.accountspayable.debitmemoallocation.infrastructure.persistence.DebitMemoAllocationJpaRepository;
import com.solusi.erp.accountspayable.debitmemoallocation.infrastructure.persistence.DebitMemoAllocationPersistenceMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
public class DebitMemoAllocationConfig {

    @Bean
    public DebitMemoAllocationRepository debitMemoAllocationRepository(DebitMemoAllocationJpaRepository jpaRepository,
                                                                       DebitMemoAllocationPersistenceMapper mapper) {
        return new DebitMemoAllocationRepositoryImpl(jpaRepository, mapper);
    }

    @Bean
    public DebitMemoAllocationProrationService debitMemoAllocationProrationService() {
        return new DebitMemoAllocationProrationService();
    }

    @Bean
    public FindDebitMemoAllocationsUseCase findDebitMemoAllocationsUseCase(
            DebitMemoAllocationRepository repository,
            PlatformTransactionManager txManager) {
        FindDebitMemoAllocationsUseCase pure = new FindDebitMemoAllocationsUseCaseImpl(repository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return (keyword, debitMemoId, status, allocationDateFrom, allocationDateTo, pageable) ->
                tx.execute(txStatus -> pure.execute(keyword, debitMemoId, status, allocationDateFrom, allocationDateTo, pageable));
    }

    @Bean
    public GetDebitMemoAllocationDetailUseCase getDebitMemoAllocationDetailUseCase(
            DebitMemoAllocationRepository repository,
            PlatformTransactionManager txManager) {
        GetDebitMemoAllocationDetailUseCase pure = new GetDebitMemoAllocationDetailUseCaseImpl(repository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return id -> tx.execute(txStatus -> pure.execute(id));
    }

    @Bean
    public FindDebitMemoAllocationHistoryUseCase findDebitMemoAllocationHistoryUseCase(
            DebitMemoAllocationRepository repository,
            PlatformTransactionManager txManager) {
        FindDebitMemoAllocationHistoryUseCase pure = new FindDebitMemoAllocationHistoryUseCaseImpl(repository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return new FindDebitMemoAllocationHistoryUseCase() {
            @Override
            public java.util.List<com.solusi.erp.accountspayable.debitmemoallocation.application.usecase.query.DebitMemoAllocationHistoryView> byDebitMemoId(Long debitMemoId) {
                return tx.execute(txStatus -> pure.byDebitMemoId(debitMemoId));
            }

            @Override
            public java.util.List<com.solusi.erp.accountspayable.debitmemoallocation.application.usecase.query.DebitMemoAllocationHistoryView> byVendorBillId(Long vendorBillId) {
                return tx.execute(txStatus -> pure.byVendorBillId(vendorBillId));
            }
        };
    }
}
