package com.solusi.erp.purchasing.purchasereturn.infrastructure.config;

import com.solusi.erp.accountspayable.debitmemo.application.usecase.command.CreateDebitMemoFromPurchaseReturnUseCase;
import com.solusi.erp.purchasing.purchasereturn.domain.repository.PurchaseReturnRepository;
import com.solusi.erp.accounting.period.application.usecase.query.EnsureOpenPeriodForDateUseCase;
import com.solusi.erp.inventory.goodsissue.application.usecase.command.CompleteGoodsIssueUseCase;
import com.solusi.erp.inventory.goodsissue.domain.port.PurchaseReturnGoodsIssueSourcePort;
import com.solusi.erp.inventory.goodsissue.domain.repository.GoodsIssueRepository;
import com.solusi.erp.inventory.goodsissue.infrastructure.service.GoodsIssueSourceResolverRegistry;
import com.solusi.erp.inventory.stock.domain.port.InventoryReservationService;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.query.FindEligiblePurchaseReturnGoodsReceiptsUseCase;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.query.FindPurchaseReturnGrLineSlicesUseCase;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.query.FindPurchaseReturnSerialsUseCase;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.query.GetPurchaseReturnCreateViewUseCase;
import com.solusi.erp.core.infrastructure.sequence.SequenceGeneratorService;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.command.CancelDraftPurchaseReturnUseCase;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.command.CancelApprovedPurchaseReturnUseCase;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.command.CancelPurchaseReturnSubmissionUseCase;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.command.CreatePurchaseReturnUseCase;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.command.ConfirmPurchaseReturnUseCase;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.command.SubmitPurchaseReturnUseCase;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.command.UpdatePurchaseReturnUseCase;
import com.solusi.erp.purchasing.purchasereturn.domain.port.PurchaseReturnApprovalCancellationPort;
import com.solusi.erp.purchasing.purchasereturn.domain.port.PurchaseReturnEventPublisher;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.query.FindPurchaseReturnsUseCase;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.query.GetPurchaseReturnEditViewUseCase;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.query.GetPurchaseReturnUseCase;
import com.solusi.erp.purchasing.purchasereturn.infrastructure.persistence.PurchaseReturnJpaRepository;
import com.solusi.erp.purchasing.purchasereturn.infrastructure.persistence.PurchaseReturnPersistenceMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {PurchaseReturnConfig.class, PurchaseReturnConfigTest.MocksConfig.class})
class PurchaseReturnConfigTest {

    @Autowired
    private PurchaseReturnRepository purchaseReturnRepository;

    @Autowired
    private FindEligiblePurchaseReturnGoodsReceiptsUseCase findEligibleGoodsReceiptsUseCase;

    @Autowired
    private GetPurchaseReturnCreateViewUseCase getPurchaseReturnCreateViewUseCase;

    @Autowired
    private FindPurchaseReturnGrLineSlicesUseCase findPurchaseReturnGrLineSlicesUseCase;

    @Autowired
    private FindPurchaseReturnSerialsUseCase findPurchaseReturnSerialsUseCase;

    @Autowired
    private CreatePurchaseReturnUseCase createPurchaseReturnUseCase;

    @Autowired
    private UpdatePurchaseReturnUseCase updatePurchaseReturnUseCase;

    @Autowired
    private CancelDraftPurchaseReturnUseCase cancelDraftPurchaseReturnUseCase;

    @Autowired
    private SubmitPurchaseReturnUseCase submitPurchaseReturnUseCase;

    @Autowired
    private CancelPurchaseReturnSubmissionUseCase cancelPurchaseReturnSubmissionUseCase;

    @Autowired
    private CancelApprovedPurchaseReturnUseCase cancelApprovedPurchaseReturnUseCase;

    @Autowired
    private ConfirmPurchaseReturnUseCase confirmPurchaseReturnUseCase;

    @Autowired
    private PurchaseReturnGoodsIssueSourcePort purchaseReturnGoodsIssueSourcePort;

    @Autowired
    private FindPurchaseReturnsUseCase findPurchaseReturnsUseCase;

    @Autowired
    private GetPurchaseReturnUseCase getPurchaseReturnUseCase;

    @Autowired
    private GetPurchaseReturnEditViewUseCase getPurchaseReturnEditViewUseCase;

    @Test
    void wiresPurchaseReturnRepository() {
        assertThat(purchaseReturnRepository).isNotNull();
        assertThat(findEligibleGoodsReceiptsUseCase).isNotNull();
        assertThat(getPurchaseReturnCreateViewUseCase).isNotNull();
        assertThat(findPurchaseReturnGrLineSlicesUseCase).isNotNull();
        assertThat(findPurchaseReturnSerialsUseCase).isNotNull();
        assertThat(createPurchaseReturnUseCase).isNotNull();
        assertThat(updatePurchaseReturnUseCase).isNotNull();
        assertThat(cancelDraftPurchaseReturnUseCase).isNotNull();
        assertThat(submitPurchaseReturnUseCase).isNotNull();
        assertThat(cancelPurchaseReturnSubmissionUseCase).isNotNull();
        assertThat(cancelApprovedPurchaseReturnUseCase).isNotNull();
        assertThat(confirmPurchaseReturnUseCase).isNotNull();
        assertThat(purchaseReturnGoodsIssueSourcePort).isNotNull();
        assertThat(findPurchaseReturnsUseCase).isNotNull();
        assertThat(getPurchaseReturnUseCase).isNotNull();
        assertThat(getPurchaseReturnEditViewUseCase).isNotNull();
    }

    @Configuration
    static class MocksConfig {

        @Bean
        PurchaseReturnJpaRepository purchaseReturnJpaRepository() {
            return mock(PurchaseReturnJpaRepository.class);
        }

        @Bean
        PurchaseReturnPersistenceMapper purchaseReturnPersistenceMapper() {
            return mock(PurchaseReturnPersistenceMapper.class);
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
        PlatformTransactionManager platformTransactionManager() {
            return mock(PlatformTransactionManager.class);
        }

        @Bean
        InventoryReservationService inventoryReservationService() {
            return mock(InventoryReservationService.class);
        }

        @Bean
        PurchaseReturnEventPublisher purchaseReturnEventPublisher() {
            return mock(PurchaseReturnEventPublisher.class);
        }

        @Bean
        PurchaseReturnApprovalCancellationPort purchaseReturnApprovalCancellationPort() {
            return mock(PurchaseReturnApprovalCancellationPort.class);
        }

        @Bean
        GoodsIssueSourceResolverRegistry goodsIssueSourceResolverRegistry() {
            return mock(GoodsIssueSourceResolverRegistry.class);
        }

        @Bean
        GoodsIssueRepository goodsIssueRepository() {
            return mock(GoodsIssueRepository.class);
        }

        @Bean
        CompleteGoodsIssueUseCase completeGoodsIssueUseCase() {
            return mock(CompleteGoodsIssueUseCase.class);
        }

        @Bean
        EnsureOpenPeriodForDateUseCase ensureOpenPeriodForDateUseCase() {
            return mock(EnsureOpenPeriodForDateUseCase.class);
        }

        @Bean
        CreateDebitMemoFromPurchaseReturnUseCase createDebitMemoFromPurchaseReturnUseCase() {
            return mock(CreateDebitMemoFromPurchaseReturnUseCase.class);
        }
    }
}
