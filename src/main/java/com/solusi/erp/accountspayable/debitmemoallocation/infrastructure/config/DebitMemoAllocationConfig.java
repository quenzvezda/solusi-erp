package com.solusi.erp.accountspayable.debitmemoallocation.infrastructure.config;

import com.solusi.erp.accounting.journal.application.usecase.command.PostJournalForEventUseCase;
import com.solusi.erp.accounting.journal.application.usecase.command.ReversePostedJournalUseCase;
import com.solusi.erp.accounting.journal.domain.repository.JournalEntryRepository;
import com.solusi.erp.accounting.period.application.usecase.query.EnsureOpenPeriodForDateUseCase;
import com.solusi.erp.accountspayable.debitmemo.domain.repository.DebitMemoRepository;
import com.solusi.erp.accountspayable.debitmemoallocation.application.usecase.command.CancelDebitMemoAllocationUseCase;
import com.solusi.erp.accountspayable.debitmemoallocation.application.usecase.command.CancelDebitMemoAllocationUseCaseImpl;
import com.solusi.erp.accountspayable.debitmemoallocation.application.usecase.command.ConfirmDebitMemoAllocationUseCase;
import com.solusi.erp.accountspayable.debitmemoallocation.application.usecase.command.ConfirmDebitMemoAllocationUseCaseImpl;
import com.solusi.erp.accountspayable.debitmemoallocation.application.usecase.command.CreateDebitMemoAllocationUseCase;
import com.solusi.erp.accountspayable.debitmemoallocation.application.usecase.command.CreateDebitMemoAllocationUseCaseImpl;
import com.solusi.erp.accountspayable.debitmemoallocation.application.usecase.command.ReverseDebitMemoAllocationUseCase;
import com.solusi.erp.accountspayable.debitmemoallocation.application.usecase.command.ReverseDebitMemoAllocationUseCaseImpl;
import com.solusi.erp.accountspayable.debitmemoallocation.application.usecase.command.UpdateDebitMemoAllocationUseCase;
import com.solusi.erp.accountspayable.debitmemoallocation.application.usecase.command.UpdateDebitMemoAllocationUseCaseImpl;
import com.solusi.erp.accountspayable.debitmemoallocation.application.usecase.query.DebitMemoAllocationSelectorUseCase;
import com.solusi.erp.accountspayable.debitmemoallocation.application.usecase.query.DebitMemoAllocationSelectorUseCaseImpl;
import com.solusi.erp.accountspayable.debitmemoallocation.application.usecase.query.FindDebitMemoAllocationHistoryUseCase;
import com.solusi.erp.accountspayable.debitmemoallocation.application.usecase.query.FindDebitMemoAllocationHistoryUseCaseImpl;
import com.solusi.erp.accountspayable.debitmemoallocation.application.usecase.query.FindDebitMemoAllocationsUseCase;
import com.solusi.erp.accountspayable.debitmemoallocation.application.usecase.query.FindDebitMemoAllocationsUseCaseImpl;
import com.solusi.erp.accountspayable.debitmemoallocation.application.usecase.query.GetDebitMemoAllocationDetailUseCase;
import com.solusi.erp.accountspayable.debitmemoallocation.application.usecase.query.GetDebitMemoAllocationDetailUseCaseImpl;
import com.solusi.erp.accountspayable.debitmemoallocation.domain.port.DebitMemoAllocationSourcePort;
import com.solusi.erp.accountspayable.debitmemoallocation.domain.repository.DebitMemoAllocationRepository;
import com.solusi.erp.accountspayable.debitmemoallocation.domain.service.DebitMemoAllocationProrationService;
import com.solusi.erp.accountspayable.debitmemoallocation.infrastructure.adapter.DebitMemoAllocationRepositoryImpl;
import com.solusi.erp.accountspayable.debitmemoallocation.infrastructure.adapter.DebitMemoAllocationSourceAdapter;
import com.solusi.erp.accountspayable.debitmemoallocation.infrastructure.persistence.DebitMemoAllocationJpaRepository;
import com.solusi.erp.accountspayable.debitmemoallocation.infrastructure.persistence.DebitMemoAllocationPersistenceMapper;
import com.solusi.erp.accountspayable.vendorpayment.domain.port.VendorBillPaymentUpdatePort;
import com.solusi.erp.core.infrastructure.sequence.SequenceGeneratorService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
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
    public DebitMemoAllocationSourcePort debitMemoAllocationSourcePort(NamedParameterJdbcTemplate jdbcTemplate) {
        return new DebitMemoAllocationSourceAdapter(jdbcTemplate);
    }

    @Bean
    public CreateDebitMemoAllocationUseCase createDebitMemoAllocationUseCase(
            DebitMemoAllocationRepository repository,
            DebitMemoAllocationSourcePort sourcePort,
            SequenceGeneratorService sequenceGeneratorService,
            DebitMemoAllocationProrationService prorationService,
            PlatformTransactionManager txManager) {
        CreateDebitMemoAllocationUseCase pure = new CreateDebitMemoAllocationUseCaseImpl(
                repository, sourcePort, sequenceGeneratorService, prorationService);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return command -> tx.execute(txStatus -> pure.execute(command));
    }

    @Bean
    public UpdateDebitMemoAllocationUseCase updateDebitMemoAllocationUseCase(
            DebitMemoAllocationRepository repository,
            DebitMemoAllocationSourcePort sourcePort,
            DebitMemoAllocationProrationService prorationService,
            PlatformTransactionManager txManager) {
        UpdateDebitMemoAllocationUseCase pure = new UpdateDebitMemoAllocationUseCaseImpl(repository, sourcePort, prorationService);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return command -> tx.execute(txStatus -> pure.execute(command));
    }

    @Bean
    public CancelDebitMemoAllocationUseCase cancelDebitMemoAllocationUseCase(
            DebitMemoAllocationRepository repository,
            PlatformTransactionManager txManager) {
        CancelDebitMemoAllocationUseCase pure = new CancelDebitMemoAllocationUseCaseImpl(repository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return id -> tx.executeWithoutResult(txStatus -> pure.execute(id));
    }

    @Bean
    public ConfirmDebitMemoAllocationUseCase confirmDebitMemoAllocationUseCase(
            DebitMemoAllocationRepository repository,
            DebitMemoRepository debitMemoRepository,
            DebitMemoAllocationSourcePort sourcePort,
            PostJournalForEventUseCase postJournalForEventUseCase,
            JournalEntryRepository journalEntryRepository,
            VendorBillPaymentUpdatePort vendorBillPaymentUpdatePort,
            EnsureOpenPeriodForDateUseCase ensureOpenPeriodForDateUseCase,
            PlatformTransactionManager txManager) {
        ConfirmDebitMemoAllocationUseCase pure = new ConfirmDebitMemoAllocationUseCaseImpl(
                repository,
                debitMemoRepository,
                sourcePort,
                postJournalForEventUseCase,
                journalEntryRepository,
                vendorBillPaymentUpdatePort,
                ensureOpenPeriodForDateUseCase);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return id -> tx.executeWithoutResult(txStatus -> pure.execute(id));
    }

    @Bean
    public ReverseDebitMemoAllocationUseCase reverseDebitMemoAllocationUseCase(
            DebitMemoAllocationRepository repository,
            DebitMemoRepository debitMemoRepository,
            DebitMemoAllocationSourcePort sourcePort,
            ReversePostedJournalUseCase reversePostedJournalUseCase,
            VendorBillPaymentUpdatePort vendorBillPaymentUpdatePort,
            PlatformTransactionManager txManager) {
        ReverseDebitMemoAllocationUseCase pure = new ReverseDebitMemoAllocationUseCaseImpl(
                repository,
                debitMemoRepository,
                sourcePort,
                reversePostedJournalUseCase,
                vendorBillPaymentUpdatePort);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return command -> tx.executeWithoutResult(txStatus -> pure.execute(command));
    }

    @Bean
    public FindDebitMemoAllocationsUseCase findDebitMemoAllocationsUseCase(
            DebitMemoAllocationRepository repository,
            DebitMemoAllocationSourcePort sourcePort,
            PlatformTransactionManager txManager) {
        FindDebitMemoAllocationsUseCase pure = new FindDebitMemoAllocationsUseCaseImpl(repository, sourcePort);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return (keyword, debitMemoId, vendorId, status, allocationDateFrom, allocationDateTo, pageable) ->
                tx.execute(txStatus -> pure.execute(keyword, debitMemoId, vendorId, status, allocationDateFrom, allocationDateTo, pageable));
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

    @Bean
    public DebitMemoAllocationSelectorUseCase debitMemoAllocationSelectorUseCase(
            DebitMemoAllocationSourcePort sourcePort,
            PlatformTransactionManager txManager) {
        DebitMemoAllocationSelectorUseCase pure = new DebitMemoAllocationSelectorUseCaseImpl(sourcePort);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return new DebitMemoAllocationSelectorUseCase() {
            @Override
            public com.solusi.erp.core.domain.model.Page<com.solusi.erp.accountspayable.debitmemoallocation.domain.port.DebitMemoAllocationSourcePort.EligibleVendorBill> eligibleVendorBills(
                    Long debitMemoId, String keyword, com.solusi.erp.core.domain.model.Pageable pageable) {
                return tx.execute(txStatus -> pure.eligibleVendorBills(debitMemoId, keyword, pageable));
            }

            @Override
            public com.solusi.erp.core.domain.model.Page<com.solusi.erp.accountspayable.debitmemoallocation.domain.port.DebitMemoAllocationSourcePort.EligibleDebitMemo> eligibleDebitMemos(
                    Long vendorBillId, String keyword, com.solusi.erp.core.domain.model.Pageable pageable) {
                return tx.execute(txStatus -> pure.eligibleDebitMemos(vendorBillId, keyword, pageable));
            }
        };
    }
}
