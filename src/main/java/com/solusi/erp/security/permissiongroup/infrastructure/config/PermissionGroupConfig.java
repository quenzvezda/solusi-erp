package com.solusi.erp.security.permissiongroup.infrastructure.config;

import com.solusi.erp.security.permissiongroup.application.usecase.command.*;
import com.solusi.erp.security.permissiongroup.application.usecase.query.*;
import com.solusi.erp.security.permissiongroup.domain.repository.PermissionGroupRepository;
import com.solusi.erp.security.permissiongroup.infrastructure.adapter.PermissionGroupRepositoryAdapter;
import com.solusi.erp.security.permissiongroup.infrastructure.persistence.PermissionGroupJpaRepository;
import com.solusi.erp.security.permissiongroup.infrastructure.persistence.PermissionGroupPersistenceMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
public class PermissionGroupConfig {

    @Bean
    public PermissionGroupRepository permissionGroupDomainRepository(
            PermissionGroupJpaRepository jpaRepository,
            PermissionGroupPersistenceMapper mapper) {
        return new PermissionGroupRepositoryAdapter(jpaRepository, mapper);
    }

    @Bean
    public CreatePermissionGroupUseCase createPermissionGroupUseCase(
            PermissionGroupRepository permissionGroupDomainRepository,
            PlatformTransactionManager txManager) {
        CreatePermissionGroupUseCase pure = new CreatePermissionGroupUseCaseImpl(permissionGroupDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (code, nameId, nameEn, breadcrumbId, breadcrumbEn, urlPath, iconClass, descId, descEn) ->
                tx.execute(status -> pure.execute(code, nameId, nameEn, breadcrumbId, breadcrumbEn, urlPath, iconClass, descId, descEn));
    }

    @Bean
    public UpdatePermissionGroupUseCase updatePermissionGroupUseCase(
            PermissionGroupRepository permissionGroupDomainRepository,
            PlatformTransactionManager txManager) {
        UpdatePermissionGroupUseCase pure = new UpdatePermissionGroupUseCaseImpl(permissionGroupDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (id, nameId, nameEn, breadcrumbId, breadcrumbEn, urlPath, iconClass, descId, descEn) ->
                tx.execute(status -> pure.execute(id, nameId, nameEn, breadcrumbId, breadcrumbEn, urlPath, iconClass, descId, descEn));
    }

    @Bean
    public DeletePermissionGroupUseCase deletePermissionGroupUseCase(
            PermissionGroupRepository permissionGroupDomainRepository,
            PlatformTransactionManager txManager) {
        DeletePermissionGroupUseCase pure = new DeletePermissionGroupUseCaseImpl(permissionGroupDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (id) -> tx.executeWithoutResult(status -> pure.execute(id));
    }

    @Bean
    public FindPermissionGroupsUseCase findPermissionGroupsUseCase(
            PermissionGroupRepository permissionGroupDomainRepository,
            PlatformTransactionManager txManager) {
        FindPermissionGroupsUseCase pure = new FindPermissionGroupsUseCaseImpl(permissionGroupDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return (keyword, pageable) -> tx.execute(status -> pure.execute(keyword, pageable));
    }

    @Bean
    public FindPermissionGroupByIdUseCase findPermissionGroupByIdUseCase(
            PermissionGroupRepository permissionGroupDomainRepository,
            PlatformTransactionManager txManager) {
        FindPermissionGroupByIdUseCase pure = new FindPermissionGroupByIdUseCaseImpl(permissionGroupDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return (id) -> tx.execute(status -> pure.execute(id));
    }
}
