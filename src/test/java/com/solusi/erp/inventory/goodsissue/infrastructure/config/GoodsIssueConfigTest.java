package com.solusi.erp.inventory.goodsissue.infrastructure.config;

import com.solusi.erp.accounting.journal.application.usecase.command.PostJournalForEventUseCase;
import com.solusi.erp.accounting.journal.application.usecase.command.ReversePostedJournalUseCase;
import com.solusi.erp.accounting.journal.domain.repository.JournalEntryRepository;
import com.solusi.erp.accounting.period.application.usecase.query.EnsureOpenPeriodForDateUseCase;
import com.solusi.erp.core.infrastructure.sequence.SequenceGeneratorService;
import com.solusi.erp.inventory.container.domain.port.ContainerLookupProvider;
import com.solusi.erp.inventory.facility.domain.port.FacilityLookupProvider;
import com.solusi.erp.inventory.goodsissue.application.usecase.command.CancelGoodsIssueUseCase;
import com.solusi.erp.inventory.goodsissue.application.usecase.command.CompleteGoodsIssueUseCase;
import com.solusi.erp.inventory.goodsissue.application.usecase.command.CreateGoodsIssueUseCase;
import com.solusi.erp.inventory.goodsissue.application.usecase.query.GetGoodsIssueCancelViewUseCase;
import com.solusi.erp.inventory.goodsissue.domain.port.PurchaseReturnGoodsIssueSourcePort;
import com.solusi.erp.inventory.product.domain.port.ProductLookupProvider;
import com.solusi.erp.inventory.goodsissue.infrastructure.persistence.GoodsIssueJpaRepository;
import com.solusi.erp.inventory.goodsissue.infrastructure.persistence.GoodsIssuePersistenceMapper;
import com.solusi.erp.inventory.stock.domain.port.InventoryReservationService;
import com.solusi.erp.inventory.stock.domain.port.StockMovementReversalService;
import com.solusi.erp.inventory.stock.domain.port.StockService;
import com.solusi.erp.inventory.stock.infrastructure.persistence.InventoryMovementJpaRepository;
import com.solusi.erp.inventory.uom.domain.port.UomLookupProvider;
import com.solusi.erp.inventory.uomconversion.domain.port.UomConversionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.PlatformTransactionManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {GoodsIssueConfig.class, GoodsIssueConfigTest.MocksConfig.class})
class GoodsIssueConfigTest {

    @Autowired
    private CreateGoodsIssueUseCase createGoodsIssueUseCase;

    @Autowired
    private CompleteGoodsIssueUseCase completeGoodsIssueUseCase;

    @Autowired
    private CancelGoodsIssueUseCase cancelGoodsIssueUseCase;

    @Autowired
    private GetGoodsIssueCancelViewUseCase getGoodsIssueCancelViewUseCase;

    @Test
    void wiresGoodsIssueCommandUseCases() {
        assertThat(createGoodsIssueUseCase).isNotNull();
        assertThat(completeGoodsIssueUseCase).isNotNull();
        assertThat(cancelGoodsIssueUseCase).isNotNull();
        assertThat(getGoodsIssueCancelViewUseCase).isNotNull();
    }

    @Configuration
    static class MocksConfig {
        @Bean GoodsIssueJpaRepository goodsIssueJpaRepository() { return mock(GoodsIssueJpaRepository.class); }
        @Bean GoodsIssuePersistenceMapper goodsIssuePersistenceMapper() { return mock(GoodsIssuePersistenceMapper.class); }
        @Bean SequenceGeneratorService sequenceGeneratorService() { return mock(SequenceGeneratorService.class); }
        @Bean EnsureOpenPeriodForDateUseCase ensureOpenPeriodForDateUseCase() { return mock(EnsureOpenPeriodForDateUseCase.class); }
        @Bean StockService stockService() { return mock(StockService.class); }
        @Bean StockMovementReversalService stockMovementReversalService() { return mock(StockMovementReversalService.class); }
        @Bean InventoryMovementJpaRepository inventoryMovementJpaRepository() { return mock(InventoryMovementJpaRepository.class); }
        @Bean UomConversionService uomConversionService() { return mock(UomConversionService.class); }
        @Bean PostJournalForEventUseCase postJournalForEventUseCase() { return mock(PostJournalForEventUseCase.class); }
        @Bean ReversePostedJournalUseCase reversePostedJournalUseCase() { return mock(ReversePostedJournalUseCase.class); }
        @Bean JournalEntryRepository journalEntryRepository() { return mock(JournalEntryRepository.class); }
        @Bean InventoryReservationService inventoryReservationService() { return mock(InventoryReservationService.class); }
        @Bean PurchaseReturnGoodsIssueSourcePort purchaseReturnGoodsIssueSourcePort() { return mock(PurchaseReturnGoodsIssueSourcePort.class); }
        @Bean ProductLookupProvider productLookupProvider() { return mock(ProductLookupProvider.class); }
        @Bean UomLookupProvider uomLookupProvider() { return mock(UomLookupProvider.class); }
        @Bean ContainerLookupProvider containerLookupProvider() { return mock(ContainerLookupProvider.class); }
        @Bean FacilityLookupProvider facilityLookupProvider() { return mock(FacilityLookupProvider.class); }
        @Bean PlatformTransactionManager platformTransactionManager() { return mock(PlatformTransactionManager.class); }
    }
}
