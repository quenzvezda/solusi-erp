package com.solusi.erp.security.permission.infrastructure.config;

import com.solusi.erp.security.permission.application.usecase.command.*;
import com.solusi.erp.security.permission.application.usecase.query.*;
import com.solusi.erp.security.permission.domain.repository.PermissionRepository;
import com.solusi.erp.security.permission.infrastructure.adapter.PermissionRepositoryAdapter;
import com.solusi.erp.security.permission.infrastructure.persistence.PermissionJpaRepository;
import com.solusi.erp.security.permission.infrastructure.persistence.PermissionPersistenceMapper;
import com.solusi.erp.security.permissiongroup.infrastructure.persistence.PermissionGroupJpaRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
public class PermissionConfig {

    @Bean
    public PermissionRepository permissionDomainRepository(
            PermissionJpaRepository permissionJpaRepository,
            PermissionGroupJpaRepository permissionGroupRepository,
            PermissionPersistenceMapper mapper) {
        return new PermissionRepositoryAdapter(permissionJpaRepository, permissionGroupRepository, mapper);
    }

    @Bean
    public CreatePermissionUseCase createPermissionUseCase(
            PermissionRepository permissionDomainRepository,
            PlatformTransactionManager txManager) {
        CreatePermissionUseCase pure = new CreatePermissionUseCaseImpl(permissionDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (name, description, permissionGroupId) ->
                tx.execute(status -> pure.execute(name, description, permissionGroupId));
    }

    @Bean
    public UpdatePermissionUseCase updatePermissionUseCase(
            PermissionRepository permissionDomainRepository,
            PlatformTransactionManager txManager) {
        UpdatePermissionUseCase pure = new UpdatePermissionUseCaseImpl(permissionDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (id, name, description, permissionGroupId) ->
                tx.execute(status -> pure.execute(id, name, description, permissionGroupId));
    }

    @Bean
    public DeletePermissionUseCase deletePermissionUseCase(
            PermissionRepository permissionDomainRepository,
            PlatformTransactionManager txManager) {
        DeletePermissionUseCase pure = new DeletePermissionUseCaseImpl(permissionDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return id -> tx.executeWithoutResult(status -> pure.execute(id));
    }

    @Bean
    public FindPermissionsUseCase findPermissionsUseCase(
            PermissionRepository permissionDomainRepository,
            PlatformTransactionManager txManager) {
        FindPermissionsUseCase pure = new FindPermissionsUseCaseImpl(permissionDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return () -> tx.execute(status -> pure.execute());
    }

    @Bean
    public FindPermissionByIdUseCase findPermissionByIdUseCase(
            PermissionRepository permissionDomainRepository,
            PlatformTransactionManager txManager) {
        FindPermissionByIdUseCase pure = new FindPermissionByIdUseCaseImpl(permissionDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return id -> tx.execute(status -> pure.execute(id));
    }
}
