package com.solusi.erp.accountspayable.vendorbill.infrastructure.config;

import com.solusi.erp.accounting.journal.application.usecase.command.PostJournalForEventUseCase;
import com.solusi.erp.accounting.period.application.usecase.query.EnsureOpenPeriodForDateUseCase;
import com.solusi.erp.accountspayable.vendorbill.application.usecase.command.ConfirmVendorBillUseCase;
import com.solusi.erp.accountspayable.vendorbill.application.usecase.command.CreateVendorBillUseCase;
import com.solusi.erp.accountspayable.vendorbill.application.usecase.query.FindVendorBillsUseCase;
import com.solusi.erp.accountspayable.vendorbill.domain.port.BillableGrQueryPort;
import com.solusi.erp.accountspayable.vendorbill.domain.port.VendorBillSettlementSummaryPort;
import com.solusi.erp.accountspayable.vendorbill.domain.repository.VendorBillRepository;
import com.solusi.erp.accountspayable.vendorbill.infrastructure.persistence.VendorBillJpaRepository;
import com.solusi.erp.accountspayable.vendorbill.infrastructure.persistence.VendorBillPersistenceMapper;
import com.solusi.erp.core.infrastructure.sequence.SequenceGeneratorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.PlatformTransactionManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {VendorBillConfig.class, VendorBillConfigTest.MocksConfig.class})
class VendorBillConfigTest {

    @Autowired
    private VendorBillRepository vendorBillRepository;

    @Autowired
    private BillableGrQueryPort billableGrQueryPort;

    @Autowired
    private VendorBillSettlementSummaryPort vendorBillSettlementSummaryPort;

    @Autowired
    private CreateVendorBillUseCase createVendorBillUseCase;

    @Autowired
    private ConfirmVendorBillUseCase confirmVendorBillUseCase;

    @Autowired
    private FindVendorBillsUseCase findVendorBillsUseCase;

    @Test
    void should_register_vendor_bill_beans() {
        assertThat(vendorBillRepository).isNotNull();
        assertThat(billableGrQueryPort).isNotNull();
        assertThat(vendorBillSettlementSummaryPort).isNotNull();
        assertThat(createVendorBillUseCase).isNotNull();
        assertThat(confirmVendorBillUseCase).isNotNull();
        assertThat(findVendorBillsUseCase).isNotNull();
    }

    @Configuration
    static class MocksConfig {
        @Bean
        VendorBillJpaRepository vendorBillJpaRepository() {
            return mock(VendorBillJpaRepository.class);
        }

        @Bean
        VendorBillPersistenceMapper vendorBillPersistenceMapper() {
            return mock(VendorBillPersistenceMapper.class);
        }

        @Bean
        NamedParameterJdbcTemplate namedParameterJdbcTemplate() {
            return mock(NamedParameterJdbcTemplate.class);
        }

        @Bean
        SequenceGeneratorService sequenceGeneratorService() {
            return mock(SequenceGeneratorService.class);
        }

        @Bean
        EnsureOpenPeriodForDateUseCase ensureOpenPeriodForDateUseCase() {
            return mock(EnsureOpenPeriodForDateUseCase.class);
        }

        @Bean
        PostJournalForEventUseCase postJournalForEventUseCase() {
            return mock(PostJournalForEventUseCase.class);
        }

        @Bean
        PlatformTransactionManager platformTransactionManager() {
            return mock(PlatformTransactionManager.class);
        }
    }
}
