package com.solusi.erp.inventory.container.infrastructure.config;

import com.solusi.erp.core.infrastructure.sequence.SequenceGeneratorService;
import com.solusi.erp.inventory.container.application.usecase.command.*;
import com.solusi.erp.inventory.container.application.usecase.query.*;
import com.solusi.erp.inventory.container.domain.port.ContainerLookupProvider;
import com.solusi.erp.inventory.container.domain.repository.ContainerRepository;
import com.solusi.erp.inventory.container.infrastructure.adapter.ContainerGridUsageChecker;
import com.solusi.erp.inventory.container.infrastructure.adapter.ContainerInUseCheckerComposite;
import com.solusi.erp.inventory.container.infrastructure.adapter.ContainerLookupProviderImpl;
import com.solusi.erp.inventory.container.infrastructure.adapter.ContainerRepositoryImpl;
import com.solusi.erp.inventory.container.domain.port.ContainerUsageChecker;
import com.solusi.erp.inventory.container.infrastructure.persistence.ContainerJpaRepository;
import com.solusi.erp.inventory.container.infrastructure.persistence.ContainerPersistenceMapper;
import com.solusi.erp.inventory.grid.domain.port.GridUsageChecker;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
public class ContainerConfig {

    @Bean
    public GridUsageChecker containerGridUsageChecker(ContainerJpaRepository containerJpaRepository) {
        return new ContainerGridUsageChecker(containerJpaRepository);
    }

    @Bean
    public ContainerRepository containerDomainRepository(
            ContainerJpaRepository jpaRepository,
            ContainerPersistenceMapper mapper) {
        return new ContainerRepositoryImpl(jpaRepository, mapper);
    }

    @Bean
    public ContainerLookupProvider containerLookupProvider(ContainerJpaRepository containerJpaRepository) {
        return new ContainerLookupProviderImpl(containerJpaRepository);
    }

    @Bean
    public CreateContainerUseCase createContainerUseCase(
            ContainerRepository containerDomainRepository,
            SequenceGeneratorService sequenceGeneratorService,
            PlatformTransactionManager txManager) {
        CreateContainerUseCase pure = new CreateContainerUseCaseImpl(containerDomainRepository, sequenceGeneratorService);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (gridId, name, barcode, length, width, height, maxWeight, note, isActive) ->
            tx.execute(status -> pure.execute(gridId, name, barcode, length, width, height, maxWeight, note, isActive));
    }

    @Bean
    public UpdateContainerUseCase updateContainerUseCase(
            ContainerRepository containerDomainRepository,
            PlatformTransactionManager txManager) {
        UpdateContainerUseCase pure = new UpdateContainerUseCaseImpl(containerDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (id, name, barcode, length, width, height, maxWeight, note, isActive) ->
            tx.execute(status -> pure.execute(id, name, barcode, length, width, height, maxWeight, note, isActive));
    }

    @Bean
    public ContainerInUseCheckerComposite containerInUseCheckerComposite(
            java.util.List<ContainerUsageChecker> checkers) {
        return new ContainerInUseCheckerComposite(checkers);
    }

    @Bean
    public DeleteContainerUseCase deleteContainerUseCase(
            ContainerRepository containerDomainRepository,
            ContainerInUseCheckerComposite containerInUseCheckerComposite,
            PlatformTransactionManager txManager) {
        DeleteContainerUseCase pure = new DeleteContainerUseCaseImpl(containerDomainRepository,
                containerInUseCheckerComposite);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (id) -> tx.executeWithoutResult(status -> pure.execute(id));
    }

    @Bean
    public FindContainersUseCase findContainersUseCase(
            ContainerRepository containerDomainRepository,
            PlatformTransactionManager txManager) {
        FindContainersUseCase pure = new FindContainersUseCaseImpl(containerDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return (keyword, gridId, pageable) ->
            tx.execute(status -> pure.execute(keyword, gridId, pageable));
    }

    @Bean
    public GetContainerEditViewUseCase getContainerEditViewUseCase(
            ContainerRepository containerDomainRepository,
            PlatformTransactionManager txManager) {
        GetContainerEditViewUseCase pure = new GetContainerEditViewUseCaseImpl(containerDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return (id) -> tx.execute(status -> pure.execute(id));
    }

    @Bean
    public GetContainerLookupUseCase getContainerLookupUseCase(
            ContainerRepository containerDomainRepository,
            PlatformTransactionManager txManager) {
        GetContainerLookupUseCase pure = new GetContainerLookupUseCaseImpl(containerDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return new GetContainerLookupUseCase() {
            @Override
            public com.solusi.erp.core.dto.LookupDto getById(Long id) {
                return tx.execute(status -> pure.getById(id));
            }
            @Override
            public java.util.List<com.solusi.erp.core.dto.LookupDto> search(String keyword, int limit) {
                return tx.execute(status -> pure.search(keyword, limit));
            }
            @Override
            public java.util.List<com.solusi.erp.core.dto.LookupDto> search(String keyword, Long gridId, Long facilityId, int limit) {
                return tx.execute(status -> pure.search(keyword, gridId, facilityId, limit));
            }
            @Override
            public java.util.List<com.solusi.erp.core.dto.LookupDto> findAll() {
                return tx.execute(status -> pure.findAll());
            }
        };
    }
}
