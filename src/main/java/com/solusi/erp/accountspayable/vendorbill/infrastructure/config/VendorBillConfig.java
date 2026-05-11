package com.solusi.erp.accountspayable.vendorbill.infrastructure.config;

import com.solusi.erp.accounting.journal.application.usecase.command.PostJournalForEventUseCase;
import com.solusi.erp.accounting.period.application.usecase.query.EnsureOpenPeriodForDateUseCase;
import com.solusi.erp.accountspayable.vendorbill.application.usecase.command.*;
import com.solusi.erp.accountspayable.vendorbill.application.usecase.query.*;
import com.solusi.erp.accountspayable.vendorbill.domain.port.BillableGrQueryPort;
import com.solusi.erp.accountspayable.vendorbill.domain.repository.VendorBillRepository;
import com.solusi.erp.accountspayable.vendorbill.infrastructure.adapter.BillableGrQueryAdapter;
import com.solusi.erp.accountspayable.vendorbill.infrastructure.adapter.VendorBillRepositoryImpl;
import com.solusi.erp.accountspayable.vendorbill.infrastructure.persistence.VendorBillJpaRepository;
import com.solusi.erp.accountspayable.vendorbill.infrastructure.persistence.VendorBillPersistenceMapper;
import com.solusi.erp.core.infrastructure.sequence.SequenceGeneratorService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
public class VendorBillConfig {

    @Bean
    public VendorBillRepository vendorBillRepository(VendorBillJpaRepository jpaRepository,
                                                     VendorBillPersistenceMapper mapper) {
        return new VendorBillRepositoryImpl(jpaRepository, mapper);
    }

    @Bean
    public BillableGrQueryPort billableGrQueryPort(NamedParameterJdbcTemplate jdbcTemplate) {
        return new BillableGrQueryAdapter(jdbcTemplate);
    }

    @Bean
    public CreateVendorBillUseCase createVendorBillUseCase(VendorBillRepository repository,
                                                           SequenceGeneratorService sequenceGeneratorService,
                                                           PlatformTransactionManager txManager) {
        CreateVendorBillUseCase pure = new CreateVendorBillUseCaseImpl(repository, sequenceGeneratorService);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (vendorId, vendorInvoiceNumber, billDate, dueDate, currencyId, notes, grIds, lines) ->
                tx.execute(status -> pure.execute(
                        vendorId, vendorInvoiceNumber, billDate, dueDate, currencyId, notes, grIds, lines));
    }

    @Bean
    public UpdateVendorBillUseCase updateVendorBillUseCase(VendorBillRepository repository,
                                                           PlatformTransactionManager txManager) {
        UpdateVendorBillUseCase pure = new UpdateVendorBillUseCaseImpl(repository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (id, vendorId, vendorInvoiceNumber, billDate, dueDate, currencyId, notes, grIds, lines) ->
                tx.execute(status -> pure.execute(
                        id, vendorId, vendorInvoiceNumber, billDate, dueDate, currencyId, notes, grIds, lines));
    }

    @Bean
    public DeleteVendorBillUseCase deleteVendorBillUseCase(VendorBillRepository repository,
                                                           PlatformTransactionManager txManager) {
        DeleteVendorBillUseCase pure = new DeleteVendorBillUseCaseImpl(repository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return id -> tx.executeWithoutResult(status -> pure.execute(id));
    }

    @Bean
    public CancelVendorBillUseCase cancelVendorBillUseCase(VendorBillRepository repository,
                                                           PlatformTransactionManager txManager) {
        CancelVendorBillUseCase pure = new CancelVendorBillUseCaseImpl(repository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return id -> tx.executeWithoutResult(status -> pure.execute(id));
    }

    @Bean
    public ConfirmVendorBillUseCase confirmVendorBillUseCase(VendorBillRepository repository,
                                                             BillableGrQueryPort billableGrQueryPort,
                                                             EnsureOpenPeriodForDateUseCase ensureOpenPeriod,
                                                             PostJournalForEventUseCase postJournalForEventUseCase,
                                                             PlatformTransactionManager txManager) {
        ConfirmVendorBillUseCase pure = new ConfirmVendorBillUseCaseImpl(
                repository, billableGrQueryPort, ensureOpenPeriod, postJournalForEventUseCase);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return id -> tx.executeWithoutResult(status -> pure.execute(id));
    }

    @Bean
    public FindVendorBillsUseCase findVendorBillsUseCase(VendorBillRepository repository,
                                                         PlatformTransactionManager txManager) {
        FindVendorBillsUseCase pure = new FindVendorBillsUseCaseImpl(repository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return (keyword, vendorId, status, pageable) ->
                tx.execute(txStatus -> pure.execute(keyword, vendorId, status, pageable));
    }

    @Bean
    public GetVendorBillDetailUseCase getVendorBillDetailUseCase(VendorBillRepository repository,
                                                                 PlatformTransactionManager txManager) {
        GetVendorBillDetailUseCase pure = new GetVendorBillDetailUseCaseImpl(repository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return id -> tx.execute(status -> pure.execute(id));
    }

    @Bean
    public GetVendorBillCreateViewUseCase getVendorBillCreateViewUseCase(BillableGrQueryPort billableGrQueryPort,
                                                                          PlatformTransactionManager txManager) {
        GetVendorBillCreateViewUseCase pure = new GetVendorBillCreateViewUseCaseImpl(billableGrQueryPort);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return (vendorId, currencyId) -> tx.execute(status -> pure.execute(vendorId, currencyId));
    }

    @Bean
    public FindBillableGrLinesUseCase findBillableGrLinesUseCase(BillableGrQueryPort billableGrQueryPort,
                                                                  PlatformTransactionManager txManager) {
        FindBillableGrLinesUseCase pure = new FindBillableGrLinesUseCaseImpl(billableGrQueryPort);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return grId -> tx.execute(status -> pure.execute(grId));
    }
}
