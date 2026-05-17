package com.solusi.erp.accountspayable.vendorpayment.infrastructure.config;

import com.solusi.erp.accounting.journal.application.usecase.command.PostJournalForEventUseCase;
import com.solusi.erp.accountspayable.vendorpayment.application.usecase.command.*;
import com.solusi.erp.accountspayable.vendorpayment.application.usecase.query.*;
import com.solusi.erp.accountspayable.vendorpayment.domain.port.PayableVendorBillQueryPort;
import com.solusi.erp.accountspayable.vendorpayment.domain.port.VendorBillPaymentUpdatePort;
import com.solusi.erp.accountspayable.vendorpayment.domain.repository.VendorPaymentRepository;
import com.solusi.erp.accountspayable.vendorpayment.infrastructure.adapter.PayableVendorBillQueryAdapter;
import com.solusi.erp.accountspayable.vendorpayment.infrastructure.adapter.VendorBillPaymentUpdateAdapter;
import com.solusi.erp.accountspayable.vendorpayment.infrastructure.adapter.VendorPaymentRepositoryImpl;
import com.solusi.erp.accountspayable.vendorpayment.infrastructure.persistence.VendorPaymentJpaRepository;
import com.solusi.erp.accountspayable.vendorpayment.infrastructure.persistence.VendorPaymentPersistenceMapper;
import com.solusi.erp.core.infrastructure.sequence.SequenceGeneratorService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
public class VendorPaymentConfig {

    @Bean
    public VendorPaymentRepository vendorPaymentRepository(VendorPaymentJpaRepository jpaRepository,
                                                            VendorPaymentPersistenceMapper mapper) {
        return new VendorPaymentRepositoryImpl(jpaRepository, mapper);
    }

    @Bean
    public PayableVendorBillQueryPort payableVendorBillQueryPort(NamedParameterJdbcTemplate jdbcTemplate) {
        return new PayableVendorBillQueryAdapter(jdbcTemplate);
    }

    @Bean
    public VendorBillPaymentUpdatePort vendorBillPaymentUpdatePort(NamedParameterJdbcTemplate jdbcTemplate) {
        return new VendorBillPaymentUpdateAdapter(jdbcTemplate);
    }

    @Bean
    public CreateVendorPaymentUseCase createVendorPaymentUseCase(
            VendorPaymentRepository repository,
            SequenceGeneratorService sequenceGeneratorService,
            PlatformTransactionManager txManager) {
        CreateVendorPaymentUseCase pure = new CreateVendorPaymentUseCaseImpl(repository, sequenceGeneratorService);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (vendorId, currencyId, bankAccountId, paymentDate, exchangeRate, paymentAmount, reference, notes, lines) ->
                tx.execute(status -> pure.execute(vendorId, currencyId, bankAccountId, paymentDate, exchangeRate, paymentAmount, reference, notes, lines));
    }

    @Bean
    public UpdateVendorPaymentUseCase updateVendorPaymentUseCase(
            VendorPaymentRepository repository,
            PlatformTransactionManager txManager) {
        UpdateVendorPaymentUseCase pure = new UpdateVendorPaymentUseCaseImpl(repository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (id, vendorId, currencyId, bankAccountId, paymentDate, exchangeRate, paymentAmount, reference, notes, lines) ->
                tx.execute(status -> pure.execute(id, vendorId, currencyId, bankAccountId, paymentDate, exchangeRate, paymentAmount, reference, notes, lines));
    }

    @Bean
    public ConfirmVendorPaymentUseCase confirmVendorPaymentUseCase(
            VendorPaymentRepository repository,
            PostJournalForEventUseCase postJournalForEventUseCase,
            VendorBillPaymentUpdatePort vendorBillPaymentUpdatePort,
            PlatformTransactionManager txManager) {
        ConfirmVendorPaymentUseCaseImpl pure = new ConfirmVendorPaymentUseCaseImpl(
                repository, postJournalForEventUseCase, vendorBillPaymentUpdatePort);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return id -> tx.executeWithoutResult(status -> pure.execute(id));
    }

    @Bean
    public CancelVendorPaymentUseCase cancelVendorPaymentUseCase(
            VendorPaymentRepository repository,
            PlatformTransactionManager txManager) {
        CancelVendorPaymentUseCase pure = new CancelVendorPaymentUseCaseImpl(repository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return id -> tx.executeWithoutResult(status -> pure.execute(id));
    }

    @Bean
    public DeleteVendorPaymentUseCase deleteVendorPaymentUseCase(
            VendorPaymentRepository repository,
            PlatformTransactionManager txManager) {
        DeleteVendorPaymentUseCase pure = new DeleteVendorPaymentUseCaseImpl(repository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return id -> tx.executeWithoutResult(status -> pure.execute(id));
    }

    @Bean
    public GetVendorPaymentListUseCase getVendorPaymentListUseCase(
            VendorPaymentRepository repository,
            PlatformTransactionManager txManager) {
        GetVendorPaymentListUseCase pure = new GetVendorPaymentListUseCaseImpl(repository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return (keyword, vendorId, status, pageable) ->
                tx.execute(txStatus -> pure.execute(keyword, vendorId, status, pageable));
    }

    @Bean
    public GetVendorPaymentDetailUseCase getVendorPaymentDetailUseCase(
            VendorPaymentRepository repository,
            PlatformTransactionManager txManager) {
        GetVendorPaymentDetailUseCase pure = new GetVendorPaymentDetailUseCaseImpl(repository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return id -> tx.execute(status -> pure.execute(id));
    }

    @Bean
    public GetPayableVendorBillsUseCase getPayableVendorBillsUseCase(
            PayableVendorBillQueryPort payableVendorBillQueryPort,
            PlatformTransactionManager txManager) {
        GetPayableVendorBillsUseCase pure = new GetPayableVendorBillsUseCaseImpl(payableVendorBillQueryPort);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return (vendorId, currencyId) -> tx.execute(status -> pure.execute(vendorId, currencyId));
    }
}
