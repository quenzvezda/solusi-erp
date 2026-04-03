package com.solusi.erp.inventory.stock.infrastructure.service;

import com.solusi.erp.inventory.stock.application.dto.StockMovementPayload;
import com.solusi.erp.inventory.stock.domain.model.MovementType;
import com.solusi.erp.inventory.stock.domain.model.ReferenceType;
import com.solusi.erp.inventory.stock.domain.model.StockBalance;
import com.solusi.erp.inventory.stock.domain.repository.StockBalanceRepository;
import com.solusi.erp.inventory.stock.domain.service.FifoValuationService;
import com.solusi.erp.inventory.stock.infrastructure.persistence.InventoryMovementEntity;
import com.solusi.erp.inventory.stock.infrastructure.persistence.InventoryMovementJpaRepository;
import com.solusi.erp.inventory.product.infrastructure.persistence.ProductEntity;
import com.solusi.erp.inventory.product.infrastructure.persistence.JpaProductRepository;
import com.solusi.erp.inventory.uomconversion.domain.port.UomConversionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StockServiceTest {

    @Mock
    private StockBalanceRepository stockBalanceRepository;

    @Mock
    private InventoryMovementJpaRepository inventoryMovementRepository;

    @Mock
    private JpaProductRepository productRepository;

    @Mock
    private UomConversionService uomConversionService;

    @Mock
    private FifoValuationService fifoValuationService;

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private StockServiceImpl stockService;

    private ProductEntity product;

    @BeforeEach
    void setUp() {
        product = new ProductEntity();
        product.setId(1L);
        product.setCode("P001");
    }

    @Test
    void shouldIncreaseOnHandAndAvailableForReceipt() {
        StockMovementPayload payload = StockMovementPayload.builder()
                .productId(1L)
                .containerId(1L)
                .quantity(BigDecimal.TEN)
                .movementType(MovementType.RECEIPT)
                .referenceType(ReferenceType.GOODS_RECEIPT)
                .referenceId(100L)
                .referenceCode("GR-001")
                .build();

        product.setIsSerialized(false);
        when(productRepository.getReferenceById(1L)).thenReturn(product);
        when(stockBalanceRepository.findByProductContainerSerial(1L, 1L, null)).thenReturn(Optional.empty());
        when(stockBalanceRepository.save(any(StockBalance.class))).thenAnswer(i -> i.getArgument(0));

        stockService.adjust(payload);

        verify(stockBalanceRepository).save(argThat(sb ->
            sb.getQuantity().compareTo(BigDecimal.TEN) == 0 &&
            sb.getReservedQuantity().compareTo(BigDecimal.ZERO) == 0
        ));
        verify(inventoryMovementRepository).save(any(InventoryMovementEntity.class));
    }

    @Test
    void shouldThrowExceptionIfStockBecomesNegative() {
        StockMovementPayload payload = StockMovementPayload.builder()
                .productId(1L)
                .containerId(1L)
                .quantity(BigDecimal.TEN)
                .movementType(MovementType.ISSUE)
                .build();

        StockBalance existingBalance = StockBalance.createNew(1L, 1L, null);
        existingBalance.applyMovement(MovementType.RECEIPT, BigDecimal.ONE); // Only 1 on hand

        when(productRepository.getReferenceById(1L)).thenReturn(product);
        when(stockBalanceRepository.findByProductContainerSerial(1L, 1L, null)).thenReturn(Optional.of(existingBalance));
        when(messageSource.getMessage(anyString(), any(), any())).thenReturn("Insufficient stock");

        assertThrows(RuntimeException.class, () -> stockService.adjust(payload));
    }

    @Test
    void shouldDecreaseAvailableAndIncreaseReservedForReserve() {
        StockMovementPayload payload = StockMovementPayload.builder()
                .productId(1L)
                .containerId(1L)
                .quantity(BigDecimal.valueOf(5))
                .movementType(MovementType.RESERVE)
                .build();

        StockBalance existingBalance = StockBalance.createNew(1L, 1L, null);
        existingBalance.applyMovement(MovementType.RECEIPT, BigDecimal.TEN);

        when(productRepository.getReferenceById(1L)).thenReturn(product);
        when(stockBalanceRepository.findByProductContainerSerial(1L, 1L, null)).thenReturn(Optional.of(existingBalance));
        when(stockBalanceRepository.save(any(StockBalance.class))).thenAnswer(i -> i.getArgument(0));

        stockService.adjust(payload);

        verify(stockBalanceRepository).save(argThat(sb ->
            sb.getQuantity().compareTo(BigDecimal.TEN) == 0 &&
            sb.getReservedQuantity().compareTo(BigDecimal.valueOf(5)) == 0
        ));
    }

    @Test
    void shouldDecreaseOnHandAndReservedForIssueReserved() {
        StockMovementPayload payload = StockMovementPayload.builder()
                .productId(1L)
                .containerId(1L)
                .quantity(BigDecimal.valueOf(5))
                .movementType(MovementType.ISSUE_RESERVED)
                .build();

        StockBalance existingBalance = StockBalance.createNew(1L, 1L, null);
        existingBalance.applyMovement(MovementType.RECEIPT, BigDecimal.TEN);
        existingBalance.applyMovement(MovementType.RESERVE, BigDecimal.valueOf(5));

        when(productRepository.getReferenceById(1L)).thenReturn(product);
        when(stockBalanceRepository.findByProductContainerSerial(1L, 1L, null)).thenReturn(Optional.of(existingBalance));
        when(stockBalanceRepository.save(any(StockBalance.class))).thenAnswer(i -> i.getArgument(0));

        stockService.adjust(payload);

        verify(stockBalanceRepository).save(argThat(sb ->
            sb.getQuantity().compareTo(BigDecimal.valueOf(5)) == 0 &&
            sb.getReservedQuantity().compareTo(BigDecimal.ZERO) == 0
        ));
    }

    @Test
    void shouldDecreaseOnHandForTransferOut() {
        StockMovementPayload payload = StockMovementPayload.builder()
                .productId(1L)
                .containerId(1L)
                .quantity(BigDecimal.valueOf(3))
                .movementType(MovementType.TRANSFER_OUT)
                .build();

        StockBalance existingBalance = StockBalance.createNew(1L, 1L, null);
        existingBalance.applyMovement(MovementType.RECEIPT, BigDecimal.TEN);

        when(productRepository.getReferenceById(1L)).thenReturn(product);
        when(stockBalanceRepository.findByProductContainerSerial(1L, 1L, null)).thenReturn(Optional.of(existingBalance));
        when(stockBalanceRepository.save(any(StockBalance.class))).thenAnswer(i -> i.getArgument(0));

        stockService.adjust(payload);

        verify(stockBalanceRepository).save(argThat(sb ->
            sb.getQuantity().compareTo(BigDecimal.valueOf(7)) == 0
        ));
    }

    @Test
    void shouldDecreaseReservedForRelease() {
        StockMovementPayload payload = StockMovementPayload.builder()
                .productId(1L)
                .containerId(1L)
                .quantity(BigDecimal.valueOf(2))
                .movementType(MovementType.RELEASE)
                .build();

        StockBalance existingBalance = StockBalance.createNew(1L, 1L, null);
        existingBalance.applyMovement(MovementType.RECEIPT, BigDecimal.TEN);
        existingBalance.applyMovement(MovementType.RESERVE, BigDecimal.valueOf(5));

        when(productRepository.getReferenceById(1L)).thenReturn(product);
        when(stockBalanceRepository.findByProductContainerSerial(1L, 1L, null)).thenReturn(Optional.of(existingBalance));
        when(stockBalanceRepository.save(any(StockBalance.class))).thenAnswer(i -> i.getArgument(0));

        stockService.adjust(payload);

        verify(stockBalanceRepository).save(argThat(sb ->
            sb.getReservedQuantity().compareTo(BigDecimal.valueOf(3)) == 0
        ));
    }

    @Test
    void shouldVerifyInventoryMovementDetails() {
        LocalDateTime now = LocalDateTime.now();
        StockMovementPayload payload = StockMovementPayload.builder()
                .productId(1L)
                .containerId(1L)
                .quantity(BigDecimal.TEN)
                .movementType(MovementType.ADJUSTMENT)
                .referenceType(ReferenceType.STOCK_OPNAME)
                .referenceId(500L)
                .referenceCode("OPN-2026-001")
                .transactionDate(now)
                .build();

        product.setIsSerialized(false);
        when(productRepository.getReferenceById(1L)).thenReturn(product);
        when(stockBalanceRepository.findByProductContainerSerial(1L, 1L, null)).thenReturn(Optional.empty());
        when(stockBalanceRepository.save(any(StockBalance.class))).thenAnswer(i -> i.getArgument(0));

        stockService.adjust(payload);

        verify(inventoryMovementRepository).save(argThat(mov ->
            mov.getQuantity().compareTo(BigDecimal.TEN) == 0 &&
            mov.getMovementType() == MovementType.ADJUSTMENT &&
            mov.getReferenceType() == ReferenceType.STOCK_OPNAME &&
            mov.getReferenceId().equals(500L) &&
            mov.getReferenceCode().equals("OPN-2026-001") &&
            mov.getTransactionDate().equals(now)
        ));
    }

    @Test
    void shouldThrowExceptionIfReservedBecomesNegative() {
        StockMovementPayload payload = StockMovementPayload.builder()
                .productId(1L)
                .containerId(1L)
                .quantity(BigDecimal.TEN)
                .movementType(MovementType.RELEASE)
                .build();

        StockBalance existingBalance = StockBalance.createNew(1L, 1L, null);
        existingBalance.applyMovement(MovementType.RECEIPT, BigDecimal.TEN);
        existingBalance.applyMovement(MovementType.RESERVE, BigDecimal.ONE); // Only 1 reserved

        when(productRepository.getReferenceById(1L)).thenReturn(product);
        when(stockBalanceRepository.findByProductContainerSerial(1L, 1L, null)).thenReturn(Optional.of(existingBalance));
        when(messageSource.getMessage(anyString(), any(), any())).thenReturn("Insufficient reserved");

        assertThrows(RuntimeException.class, () -> stockService.adjust(payload));
    }

    @Test
    void shouldGenerateSerialNumberIfProductIsSerializedAndNoSNProvided() {
        StockMovementPayload payload = StockMovementPayload.builder()
                .productId(1L)
                .containerId(1L)
                .quantity(BigDecimal.ONE)
                .movementType(MovementType.RECEIPT)
                .build();

        product.setIsSerialized(true);
        when(productRepository.getReferenceById(1L)).thenReturn(product);
        when(stockBalanceRepository.findByProductContainerSerial(eq(1L), eq(1L), anyString())).thenReturn(Optional.empty());
        when(stockBalanceRepository.save(any(StockBalance.class))).thenAnswer(i -> i.getArgument(0));

        stockService.adjust(payload);

        verify(stockBalanceRepository).save(argThat(sb ->
            sb.getSerialNumber() != null && sb.getSerialNumber().startsWith("SN-")
        ));
        verify(inventoryMovementRepository).save(argThat(mov ->
            mov.getSerialNumber() != null && mov.getSerialNumber().startsWith("SN-")
        ));
    }

    @Test
    void shouldConvertUomBeforeProcessing() {
        StockMovementPayload payload = StockMovementPayload.builder()
                .productId(1L)
                .containerId(1L)
                .quantity(BigDecimal.valueOf(2)) // 2 Boxes
                .uomId(2L) // BOX
                .movementType(MovementType.RECEIPT)
                .build();

        product.setIsSerialized(false);
        when(productRepository.getReferenceById(1L)).thenReturn(product);
        when(uomConversionService.convertToBaseUom(1L, 2L, BigDecimal.valueOf(2)))
                .thenReturn(BigDecimal.valueOf(48)); // 2 Boxes * 24 factor
        when(stockBalanceRepository.findByProductContainerSerial(1L, 1L, null))
                .thenReturn(Optional.empty());
        when(stockBalanceRepository.save(any(StockBalance.class))).thenAnswer(i -> i.getArgument(0));

        stockService.adjust(payload);

        verify(stockBalanceRepository).save(argThat(sb ->
            sb.getQuantity().compareTo(BigDecimal.valueOf(48)) == 0
        ));
    }
}
