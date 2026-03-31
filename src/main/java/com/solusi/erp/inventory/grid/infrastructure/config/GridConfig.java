package com.solusi.erp.inventory.grid.infrastructure.config;

import com.solusi.erp.inventory.grid.application.usecase.command.*;
import com.solusi.erp.inventory.grid.application.usecase.query.*;
import com.solusi.erp.inventory.grid.domain.port.GridUsageChecker;
import com.solusi.erp.inventory.grid.domain.repository.GridRepository;
import com.solusi.erp.inventory.grid.infrastructure.adapter.GridFacilityUsageChecker;
import com.solusi.erp.inventory.grid.infrastructure.adapter.GridInUseCheckerComposite;
import com.solusi.erp.inventory.grid.infrastructure.adapter.GridRepositoryImpl;
import com.solusi.erp.inventory.grid.infrastructure.persistence.GridJpaRepository;
import com.solusi.erp.inventory.grid.infrastructure.persistence.GridPersistenceMapper;
import com.solusi.erp.inventory.facility.domain.port.FacilityUsageChecker;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

@Configuration
public class GridConfig {

    @Bean
    public GridRepository gridDomainRepository(
            GridJpaRepository jpaRepository,
            GridPersistenceMapper mapper) {
        return new GridRepositoryImpl(jpaRepository, mapper);
    }

    @Bean
    public CreateGridUseCase createGridUseCase(
            GridRepository gridDomainRepository,
            PlatformTransactionManager txManager) {
        CreateGridUseCase pure = new CreateGridUseCaseImpl(gridDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (facilityId, code, name, note, isActive) ->
            tx.execute(status -> pure.execute(facilityId, code, name, note, isActive));
    }

    @Bean
    public UpdateGridUseCase updateGridUseCase(
            GridRepository gridDomainRepository,
            PlatformTransactionManager txManager) {
        UpdateGridUseCase pure = new UpdateGridUseCaseImpl(gridDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (id, name, note, isActive) ->
            tx.execute(status -> pure.execute(id, name, note, isActive));
    }

    @Bean
    public FacilityUsageChecker gridFacilityUsageChecker(GridJpaRepository gridJpaRepository) {
        return new GridFacilityUsageChecker(gridJpaRepository);
    }

    @Bean
    public GridInUseCheckerComposite gridInUseCheckerComposite(
            List<GridUsageChecker> gridUsageCheckers) {
        return new GridInUseCheckerComposite(gridUsageCheckers);
    }

    @Bean
    public DeleteGridUseCase deleteGridUseCase(
            GridRepository gridDomainRepository,
            GridInUseCheckerComposite gridInUseCheckerComposite,
            PlatformTransactionManager txManager) {
        DeleteGridUseCase pure = new DeleteGridUseCaseImpl(gridDomainRepository, gridInUseCheckerComposite);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (id) -> tx.executeWithoutResult(status -> pure.execute(id));
    }

    @Bean
    public FindGridsUseCase findGridsUseCase(
            GridRepository gridDomainRepository,
            PlatformTransactionManager txManager) {
        FindGridsUseCase pure = new FindGridsUseCaseImpl(gridDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return (keyword, facilityId, pageable) ->
            tx.execute(status -> pure.execute(keyword, facilityId, pageable));
    }

    @Bean
    public GetGridEditViewUseCase getGridEditViewUseCase(
            GridRepository gridDomainRepository,
            PlatformTransactionManager txManager) {
        GetGridEditViewUseCase pure = new GetGridEditViewUseCaseImpl(gridDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return (id) -> tx.execute(status -> pure.execute(id));
    }

    @Bean
    public GetGridLookupUseCase getGridLookupUseCase(
            GridRepository gridDomainRepository,
            PlatformTransactionManager txManager) {
        GetGridLookupUseCase pure = new GetGridLookupUseCaseImpl(gridDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return new GetGridLookupUseCase() {
            @Override
            public com.solusi.erp.core.dto.LookupDto getById(Long id) {
                return tx.execute(status -> pure.getById(id));
            }
            @Override
            public java.util.List<com.solusi.erp.core.dto.LookupDto> search(String keyword, int limit) {
                return tx.execute(status -> pure.search(keyword, limit));
            }
            @Override
            public java.util.List<com.solusi.erp.core.dto.LookupDto> search(String keyword, Long facilityId, int limit) {
                return tx.execute(status -> pure.search(keyword, facilityId, limit));
            }
        };
    }
}
