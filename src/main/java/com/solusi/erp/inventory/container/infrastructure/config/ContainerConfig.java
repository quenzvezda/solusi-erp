package com.solusi.erp.inventory.container.infrastructure.config;

import com.solusi.erp.core.service.SequenceGeneratorService;
import com.solusi.erp.inventory.container.application.usecase.command.*;
import com.solusi.erp.inventory.container.application.usecase.query.*;
import com.solusi.erp.inventory.container.domain.repository.ContainerRepository;
import com.solusi.erp.inventory.container.infrastructure.adapter.ContainerRepositoryImpl;
import com.solusi.erp.inventory.container.infrastructure.persistence.ContainerPersistenceMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
public class ContainerConfig {

    @Bean
    public ContainerRepository containerDomainRepository(
            com.solusi.erp.inventory.repository.ContainerRepository jpaRepository,
            ContainerPersistenceMapper mapper) {
        return new ContainerRepositoryImpl(jpaRepository, mapper);
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
    public DeleteContainerUseCase deleteContainerUseCase(
            ContainerRepository containerDomainRepository,
            PlatformTransactionManager txManager) {
        DeleteContainerUseCase pure = new DeleteContainerUseCaseImpl(containerDomainRepository);
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
            public java.util.List<com.solusi.erp.core.dto.LookupDto> findAll() {
                return tx.execute(status -> pure.findAll());
            }
        };
    }
}
