package com.solusi.erp.inventory.facility.infrastructure.config;

import com.solusi.erp.core.service.SequenceGeneratorService;
import com.solusi.erp.inventory.facility.application.usecase.command.*;
import com.solusi.erp.inventory.facility.application.usecase.query.*;
import com.solusi.erp.inventory.facility.domain.repository.FacilityRepository;
import com.solusi.erp.inventory.facility.infrastructure.adapter.FacilityRepositoryImpl;
import com.solusi.erp.inventory.facility.infrastructure.persistence.FacilityPersistenceMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
public class FacilityConfig {

    @Bean
    public FacilityRepository facilityDomainRepository(
            com.solusi.erp.inventory.repository.FacilityRepository jpaRepository,
            FacilityPersistenceMapper mapper) {
        return new FacilityRepositoryImpl(jpaRepository, mapper);
    }

    @Bean
    public CreateFacilityUseCase createFacilityUseCase(
            FacilityRepository facilityDomainRepository,
            SequenceGeneratorService sequenceGeneratorService,
            PlatformTransactionManager txManager) {
        CreateFacilityUseCase pure = new CreateFacilityUseCaseImpl(facilityDomainRepository, sequenceGeneratorService);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (name, ownerId, addressLine1, cityId, postalCode, note, isActive) ->
            tx.execute(status -> pure.execute(name, ownerId, addressLine1, cityId, postalCode, note, isActive));
    }

    @Bean
    public UpdateFacilityUseCase updateFacilityUseCase(
            FacilityRepository facilityDomainRepository,
            PlatformTransactionManager txManager) {
        UpdateFacilityUseCase pure = new UpdateFacilityUseCaseImpl(facilityDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (id, name, ownerId, addressLine1, cityId, postalCode, note, isActive) ->
            tx.execute(status -> pure.execute(id, name, ownerId, addressLine1, cityId, postalCode, note, isActive));
    }

    @Bean
    public DeleteFacilityUseCase deleteFacilityUseCase(
            FacilityRepository facilityDomainRepository,
            PlatformTransactionManager txManager) {
        DeleteFacilityUseCase pure = new DeleteFacilityUseCaseImpl(facilityDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (id) -> tx.executeWithoutResult(status -> pure.execute(id));
    }

    @Bean
    public FindFacilitiesUseCase findFacilitiesUseCase(
            FacilityRepository facilityDomainRepository,
            PlatformTransactionManager txManager) {
        FindFacilitiesUseCase pure = new FindFacilitiesUseCaseImpl(facilityDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return (keyword, pageable) -> tx.execute(status -> pure.execute(keyword, pageable));
    }

    @Bean
    public GetFacilityEditViewUseCase getFacilityEditViewUseCase(
            FacilityRepository facilityDomainRepository,
            PlatformTransactionManager txManager) {
        GetFacilityEditViewUseCase pure = new GetFacilityEditViewUseCaseImpl(facilityDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return (id) -> tx.execute(status -> pure.execute(id));
    }

    @Bean
    public GetFacilityLookupUseCase getFacilityLookupUseCase(
            FacilityRepository facilityDomainRepository,
            PlatformTransactionManager txManager) {
        GetFacilityLookupUseCase pure = new GetFacilityLookupUseCaseImpl(facilityDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return new GetFacilityLookupUseCase() {
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
