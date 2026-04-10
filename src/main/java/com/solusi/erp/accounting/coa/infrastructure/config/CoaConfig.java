package com.solusi.erp.accounting.coa.infrastructure.config;

import com.solusi.erp.accounting.coa.application.usecase.command.*;
import com.solusi.erp.accounting.coa.application.usecase.query.*;
import com.solusi.erp.accounting.coa.domain.port.CoaInUseChecker;
import com.solusi.erp.accounting.coa.domain.port.CoaLookupProvider;
import com.solusi.erp.accounting.coa.domain.repository.CoaRepository;
import com.solusi.erp.accounting.coa.infrastructure.adapter.CoaInUseCheckerImpl;
import com.solusi.erp.accounting.coa.infrastructure.adapter.CoaLookupProviderImpl;
import com.solusi.erp.accounting.coa.infrastructure.adapter.CoaRepositoryImpl;
import com.solusi.erp.accounting.coa.infrastructure.persistence.CoaJpaRepository;
import com.solusi.erp.accounting.coa.infrastructure.persistence.CoaPersistenceMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
public class CoaConfig {

    @Bean
    public CoaRepository coaDomainRepository(CoaJpaRepository jpaRepository, CoaPersistenceMapper mapper) {
        return new CoaRepositoryImpl(jpaRepository, mapper);
    }

    @Bean
    public CoaInUseChecker coaInUseChecker() {
        return new CoaInUseCheckerImpl();
    }

    @Bean
    public CoaLookupProvider coaLookupProvider(CoaJpaRepository jpaRepository) {
        return new CoaLookupProviderImpl(jpaRepository);
    }

    @Bean
    public CreateCoaUseCase createCoaUseCase(CoaRepository coaDomainRepository,
                                              PlatformTransactionManager txManager) {
        CreateCoaUseCase pure = new CreateCoaUseCaseImpl(coaDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (code, name, accountType, parentId, level, isHeader, note, isActive) ->
                tx.execute(status -> pure.execute(code, name, accountType, parentId, level, isHeader, note, isActive));
    }

    @Bean
    public UpdateCoaUseCase updateCoaUseCase(CoaRepository coaDomainRepository,
                                              PlatformTransactionManager txManager) {
        UpdateCoaUseCase pure = new UpdateCoaUseCaseImpl(coaDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (id, name, accountType, parentId, level, isHeader, note, isActive) ->
                tx.execute(status -> pure.execute(id, name, accountType, parentId, level, isHeader, note, isActive));
    }

    @Bean
    public DeleteCoaUseCase deleteCoaUseCase(CoaRepository coaDomainRepository,
                                              CoaInUseChecker coaInUseChecker,
                                              PlatformTransactionManager txManager) {
        DeleteCoaUseCase pure = new DeleteCoaUseCaseImpl(coaDomainRepository, coaInUseChecker);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (id) -> tx.execute(status -> pure.execute(id));
    }

    @Bean
    public FindCoaUseCase findCoaUseCase(CoaRepository coaDomainRepository,
                                          PlatformTransactionManager txManager) {
        FindCoaUseCase pure = new FindCoaUseCaseImpl(coaDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return (keyword, pageable) -> tx.execute(status -> pure.execute(keyword, pageable));
    }

    @Bean
    public GetCoaEditViewUseCase getCoaEditViewUseCase(CoaRepository coaDomainRepository,
                                                        PlatformTransactionManager txManager) {
        GetCoaEditViewUseCase pure = new GetCoaEditViewUseCaseImpl(coaDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return (id) -> tx.execute(status -> pure.execute(id));
    }

    @Bean
    public GetCoaLookupUseCase getCoaLookupUseCase(CoaRepository coaDomainRepository,
                                                    PlatformTransactionManager txManager) {
        GetCoaLookupUseCaseImpl pure = new GetCoaLookupUseCaseImpl(coaDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return new GetCoaLookupUseCase() {
            @Override
            public com.solusi.erp.core.dto.LookupDto getById(Long id) {
                return tx.execute(status -> pure.getById(id));
            }
            @Override
            public java.util.List<com.solusi.erp.core.dto.LookupDto> search(String keyword, int limit) {
                return tx.execute(status -> pure.search(keyword, limit));
            }
        };
    }
}
