package com.solusi.erp.inventory.report.application.usecase;

import com.solusi.erp.inventory.report.application.usecase.query.GetStockCardUseCaseImpl;
import com.solusi.erp.inventory.report.web.dto.InventoryMovementResponse;
import com.solusi.erp.inventory.report.web.dto.StockCardFilter;
import com.solusi.erp.inventory.report.web.mapper.InventoryMovementMapper;
import com.solusi.erp.inventory.stock.domain.model.MovementType;
import com.solusi.erp.inventory.stock.domain.model.ReferenceType;
import com.solusi.erp.inventory.stock.infrastructure.persistence.InventoryMovementEntity;
import com.solusi.erp.inventory.stock.infrastructure.persistence.InventoryMovementJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetStockCardUseCase Tests")
class GetStockCardUseCaseTest {

    @Mock
    private InventoryMovementJpaRepository inventoryMovementRepository;

    @Mock
    private InventoryMovementMapper inventoryMovementMapper;

    private GetStockCardUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetStockCardUseCaseImpl(inventoryMovementRepository, inventoryMovementMapper);
    }

    @Test
    @DisplayName("execute forwards all stock-card filters to repository")
    void execute_forwardsAllFiltersToRepository() {
        Pageable pageable = PageRequest.of(0, 20);
        StockCardFilter filter = new StockCardFilter();
        filter.setProductId(10L);
        filter.setContainerId(20L);
        filter.setStartDate(LocalDate.of(2026, 5, 1));
        filter.setEndDate(LocalDate.of(2026, 5, 31));
        filter.setMovementType(MovementType.RECEIPT);
        filter.setReferenceType(ReferenceType.GOODS_RECEIPT);
        filter.setKeyword("GR-2026");

        InventoryMovementEntity entity = new InventoryMovementEntity();
        InventoryMovementResponse response = InventoryMovementResponse.builder().referenceCode("GR-2026-001").build();
        Page<InventoryMovementEntity> repositoryPage = new PageImpl<>(List.of(entity), pageable, 1);

        when(inventoryMovementRepository.search(
                10L,
                20L,
                LocalDateTime.of(2026, 5, 1, 0, 0),
                LocalDateTime.of(LocalDate.of(2026, 5, 31), LocalTime.MAX),
                MovementType.RECEIPT,
                ReferenceType.GOODS_RECEIPT,
                "GR-2026",
                pageable)).thenReturn(repositoryPage);
        when(inventoryMovementMapper.toResponse(entity)).thenReturn(response);

        Page<InventoryMovementResponse> result = useCase.execute(filter, pageable);

        assertThat(result.getContent()).containsExactly(response);
        verify(inventoryMovementRepository).search(
                10L,
                20L,
                LocalDateTime.of(2026, 5, 1, 0, 0),
                LocalDateTime.of(LocalDate.of(2026, 5, 31), LocalTime.MAX),
                MovementType.RECEIPT,
                ReferenceType.GOODS_RECEIPT,
                "GR-2026",
                pageable);
    }

    @Test
    @DisplayName("execute normalizes blank keyword to null")
    void execute_normalizesBlankKeywordToNull() {
        Pageable pageable = PageRequest.of(0, 20);
        StockCardFilter filter = new StockCardFilter();
        filter.setKeyword("   ");
        Page<InventoryMovementEntity> repositoryPage = new PageImpl<>(List.of(), pageable, 0);

        when(inventoryMovementRepository.search(null, null, null, null, null, null, null, pageable))
                .thenReturn(repositoryPage);

        Page<InventoryMovementResponse> result = useCase.execute(filter, pageable);

        assertThat(result.getContent()).isEmpty();
        verify(inventoryMovementRepository).search(null, null, null, null, null, null, null, pageable);
    }
}
