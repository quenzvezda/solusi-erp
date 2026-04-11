package com.solusi.erp.accounting.schema.infrastructure.config;

import com.solusi.erp.accounting.schema.application.usecase.command.*;
import com.solusi.erp.accounting.schema.application.usecase.query.*;
import com.solusi.erp.accounting.schema.domain.port.SchemaInUseChecker;
import com.solusi.erp.accounting.schema.domain.repository.SchemaRepository;
import com.solusi.erp.accounting.schema.infrastructure.adapter.SchemaInUseCheckerImpl;
import com.solusi.erp.accounting.schema.infrastructure.adapter.SchemaRepositoryImpl;
import com.solusi.erp.accounting.schema.infrastructure.persistence.SchemaJpaRepository;
import com.solusi.erp.accounting.schema.infrastructure.persistence.SchemaPersistenceMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
public class SchemaConfig {

    @Bean
    public SchemaRepository schemaDomainRepository(SchemaJpaRepository jpaRepository,
                                                    SchemaPersistenceMapper mapper) {
        return new SchemaRepositoryImpl(jpaRepository, mapper);
    }

    @Bean
    public SchemaInUseChecker schemaInUseChecker() {
        return new SchemaInUseCheckerImpl();
    }

    @Bean
    public CreateSchemaUseCase createSchemaUseCase(SchemaRepository schemaDomainRepository,
                                                    PlatformTransactionManager txManager) {
        CreateSchemaUseCase pure = new CreateSchemaUseCaseImpl(schemaDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (eventType, description, debitAccountId, creditAccountId, isActive) ->
                tx.execute(status -> pure.execute(eventType, description, debitAccountId, creditAccountId, isActive));
    }

    @Bean
    public UpdateSchemaUseCase updateSchemaUseCase(SchemaRepository schemaDomainRepository,
                                                    PlatformTransactionManager txManager) {
        UpdateSchemaUseCase pure = new UpdateSchemaUseCaseImpl(schemaDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (id, description, debitAccountId, creditAccountId, isActive) ->
                tx.execute(status -> pure.execute(id, description, debitAccountId, creditAccountId, isActive));
    }

    @Bean
    public DeleteSchemaUseCase deleteSchemaUseCase(SchemaRepository schemaDomainRepository,
                                                    SchemaInUseChecker schemaInUseChecker,
                                                    PlatformTransactionManager txManager) {
        DeleteSchemaUseCase pure = new DeleteSchemaUseCaseImpl(schemaDomainRepository, schemaInUseChecker);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (id) -> tx.execute(status -> pure.execute(id));
    }

    @Bean
    public FindSchemasUseCase findSchemasUseCase(SchemaRepository schemaDomainRepository,
                                                  PlatformTransactionManager txManager) {
        FindSchemasUseCase pure = new FindSchemasUseCaseImpl(schemaDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return (keyword, pageable) -> tx.execute(status -> pure.execute(keyword, pageable));
    }

    @Bean
    public GetSchemaEditViewUseCase getSchemaEditViewUseCase(SchemaRepository schemaDomainRepository,
                                                              PlatformTransactionManager txManager) {
        GetSchemaEditViewUseCase pure = new GetSchemaEditViewUseCaseImpl(schemaDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return (id) -> tx.execute(status -> pure.execute(id));
    }
}
