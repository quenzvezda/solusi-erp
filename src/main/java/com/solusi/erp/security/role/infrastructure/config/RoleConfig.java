package com.solusi.erp.security.role.infrastructure.config;

import com.solusi.erp.security.permission.infrastructure.persistence.PermissionJpaRepository;
import com.solusi.erp.security.role.application.usecase.command.*;
import com.solusi.erp.security.role.application.usecase.query.*;
import com.solusi.erp.security.role.domain.repository.RoleRepository;
import com.solusi.erp.security.role.infrastructure.adapter.RoleRepositoryAdapter;
import com.solusi.erp.security.role.infrastructure.persistence.RoleJpaRepository;
import com.solusi.erp.security.role.infrastructure.persistence.RolePersistenceMapper;
import com.solusi.erp.security.role.domain.port.RoleLookupProvider;
import com.solusi.erp.security.role.infrastructure.adapter.RoleLookupProviderImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
public class RoleConfig {

    @Bean
    public RoleRepository roleDomainRepository(
            RoleJpaRepository roleJpaRepository,
            PermissionJpaRepository permissionRepository,
            RolePersistenceMapper mapper) {
        return new RoleRepositoryAdapter(roleJpaRepository, permissionRepository, mapper);
    }

    @Bean
    public CreateRoleUseCase createRoleUseCase(
            RoleRepository roleDomainRepository,
            PlatformTransactionManager txManager) {
        CreateRoleUseCase pure = new CreateRoleUseCaseImpl(roleDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (name, description, permissionIds) ->
                tx.execute(status -> pure.execute(name, description, permissionIds));
    }

    @Bean
    public UpdateRoleUseCase updateRoleUseCase(
            RoleRepository roleDomainRepository,
            PlatformTransactionManager txManager) {
        UpdateRoleUseCase pure = new UpdateRoleUseCaseImpl(roleDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (id, name, description, permissionIds) ->
                tx.execute(status -> pure.execute(id, name, description, permissionIds));
    }

    @Bean
    public DeleteRoleUseCase deleteRoleUseCase(
            RoleRepository roleDomainRepository,
            PlatformTransactionManager txManager) {
        DeleteRoleUseCase pure = new DeleteRoleUseCaseImpl(roleDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return id -> tx.executeWithoutResult(status -> pure.execute(id));
    }

    @Bean
    public FindRolesUseCase findRolesUseCase(
            RoleRepository roleDomainRepository,
            PlatformTransactionManager txManager) {
        FindRolesUseCase pure = new FindRolesUseCaseImpl(roleDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return () -> tx.execute(status -> pure.execute());
    }

    @Bean
    public FindRoleByIdUseCase findRoleByIdUseCase(
            RoleRepository roleDomainRepository,
            PlatformTransactionManager txManager) {
        FindRoleByIdUseCase pure = new FindRoleByIdUseCaseImpl(roleDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return id -> tx.execute(status -> pure.execute(id));
    }

    @Bean
    public GetRoleLookupUseCase getRoleLookupUseCase(
            RoleRepository roleDomainRepository,
            PlatformTransactionManager txManager) {
        GetRoleLookupUseCaseImpl pure = new GetRoleLookupUseCaseImpl(roleDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return new GetRoleLookupUseCase() {
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

    @Bean
    public RoleLookupProvider roleLookupProvider(RoleJpaRepository roleJpaRepository) {
        return new RoleLookupProviderImpl(roleJpaRepository);
    }
}

