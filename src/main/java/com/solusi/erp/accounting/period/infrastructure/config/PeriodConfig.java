package com.solusi.erp.accounting.period.infrastructure.config;

import com.solusi.erp.accounting.period.application.usecase.command.*;
import com.solusi.erp.accounting.period.application.usecase.query.*;
import com.solusi.erp.accounting.period.domain.port.FiscalYearInUseChecker;
import com.solusi.erp.accounting.period.domain.port.OpenAccountingPeriodLookup;
import com.solusi.erp.accounting.period.domain.repository.FiscalYearRepository;
import com.solusi.erp.accounting.period.infrastructure.adapter.FiscalYearInUseCheckerImpl;
import com.solusi.erp.accounting.period.infrastructure.adapter.FiscalYearRepositoryImpl;
import com.solusi.erp.accounting.period.infrastructure.adapter.OpenAccountingPeriodLookupImpl;
import com.solusi.erp.accounting.period.infrastructure.persistence.*;
import com.solusi.erp.core.infrastructure.sequence.SequenceGeneratorService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
public class PeriodConfig {

    @Bean
    public FiscalYearRepository fiscalYearDomainRepository(
            FiscalYearJpaRepository fyJpaRepo,
            AccountingPeriodJpaRepository periodJpaRepo,
            FiscalYearPersistenceMapper fyMapper,
            PeriodPersistenceMapper periodMapper) {
        return new FiscalYearRepositoryImpl(fyJpaRepo, periodJpaRepo, fyMapper, periodMapper);
    }

    @Bean
    public FiscalYearInUseChecker fiscalYearInUseChecker() {
        return new FiscalYearInUseCheckerImpl();
    }

    @Bean
    public OpenAccountingPeriodLookup openAccountingPeriodLookup(
            AccountingPeriodJpaRepository periodJpaRepo,
            PeriodPersistenceMapper periodMapper) {
        return new OpenAccountingPeriodLookupImpl(periodJpaRepo, periodMapper);
    }

    @Bean
    public EnsureOpenPeriodForDateUseCase ensureOpenPeriodForDateUseCase(
            OpenAccountingPeriodLookup openAccountingPeriodLookup,
            PlatformTransactionManager txManager) {
        EnsureOpenPeriodForDateUseCase pure = new EnsureOpenPeriodForDateUseCaseImpl(openAccountingPeriodLookup);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return date -> tx.executeWithoutResult(status -> pure.execute(date));
    }

    @Bean
    public CreateFiscalYearUseCase createFiscalYearUseCase(
            FiscalYearRepository fiscalYearDomainRepository,
            SequenceGeneratorService sequenceGeneratorService,
            PlatformTransactionManager txManager) {
        CreateFiscalYearUseCase pure = new CreateFiscalYearUseCaseImpl(
                fiscalYearDomainRepository, sequenceGeneratorService);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (name, startDate, endDate, isActive) ->
                tx.execute(status -> pure.execute(name, startDate, endDate, isActive));
    }

    @Bean
    public UpdateFiscalYearUseCase updateFiscalYearUseCase(
            FiscalYearRepository fiscalYearDomainRepository,
            PlatformTransactionManager txManager) {
        UpdateFiscalYearUseCase pure = new UpdateFiscalYearUseCaseImpl(fiscalYearDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (id, name, isActive) -> tx.execute(status -> pure.execute(id, name, isActive));
    }

    @Bean
    public DeleteFiscalYearUseCase deleteFiscalYearUseCase(
            FiscalYearRepository fiscalYearDomainRepository,
            FiscalYearInUseChecker fiscalYearInUseChecker,
            PlatformTransactionManager txManager) {
        DeleteFiscalYearUseCase pure = new DeleteFiscalYearUseCaseImpl(
                fiscalYearDomainRepository, fiscalYearInUseChecker);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (id) -> tx.execute(status -> pure.execute(id));
    }

    @Bean
    public ClosePeriodUseCase closePeriodUseCase(
            FiscalYearRepository fiscalYearDomainRepository,
            PlatformTransactionManager txManager) {
        ClosePeriodUseCase pure = new ClosePeriodUseCaseImpl(fiscalYearDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (periodId) -> tx.execute(status -> pure.execute(periodId));
    }

    @Bean
    public ReopenPeriodUseCase reopenPeriodUseCase(
            FiscalYearRepository fiscalYearDomainRepository,
            PlatformTransactionManager txManager) {
        ReopenPeriodUseCase pure = new ReopenPeriodUseCaseImpl(fiscalYearDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (periodId) -> tx.execute(status -> pure.execute(periodId));
    }

    @Bean
    public FindFiscalYearsUseCase findFiscalYearsUseCase(
            FiscalYearRepository fiscalYearDomainRepository,
            PlatformTransactionManager txManager) {
        FindFiscalYearsUseCase pure = new FindFiscalYearsUseCaseImpl(fiscalYearDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return (keyword, pageable) -> tx.execute(status -> pure.execute(keyword, pageable));
    }

    @Bean
    public GetFiscalYearDetailUseCase getFiscalYearDetailUseCase(
            FiscalYearRepository fiscalYearDomainRepository,
            PlatformTransactionManager txManager) {
        GetFiscalYearDetailUseCase pure = new GetFiscalYearDetailUseCaseImpl(fiscalYearDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return (id) -> tx.execute(status -> pure.execute(id));
    }
}
