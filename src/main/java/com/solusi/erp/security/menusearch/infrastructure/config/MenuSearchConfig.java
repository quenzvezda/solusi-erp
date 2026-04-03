package com.solusi.erp.security.menusearch.infrastructure.config;

import com.solusi.erp.security.menusearch.application.port.MenuQueryPort;
import com.solusi.erp.security.menusearch.application.usecase.*;
import com.solusi.erp.security.menusearch.infrastructure.adapter.MenuQueryPortAdapter;
import com.solusi.erp.security.permissiongroup.infrastructure.persistence.PermissionGroupJpaRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
public class MenuSearchConfig {

    @Bean
    public MenuQueryPort menuQueryPort(PermissionGroupJpaRepository repository) {
        return new MenuQueryPortAdapter(repository);
    }

    @Bean
    public SearchMenusUseCase searchMenusUseCase(
            MenuQueryPort menuQueryPort,
            PlatformTransactionManager txManager) {
        SearchMenusUseCaseImpl pure = new SearchMenusUseCaseImpl(menuQueryPort);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return (keyword, limit, authorities) -> tx.execute(status -> pure.execute(keyword, limit, authorities));
    }

    @Bean
    public BuildMenuTreeUseCase buildMenuTreeUseCase(
            MenuQueryPort menuQueryPort,
            PlatformTransactionManager txManager) {
        BuildMenuTreeUseCaseImpl pure = new BuildMenuTreeUseCaseImpl(menuQueryPort);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return authorities -> tx.execute(status -> pure.execute(authorities));
    }
}
