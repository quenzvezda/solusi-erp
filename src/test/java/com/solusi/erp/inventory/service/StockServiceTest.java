package com.solusi.erp.inventory.service;

import com.solusi.erp.inventory.dto.StockMovementPayload;
import com.solusi.erp.inventory.model.*;
import com.solusi.erp.inventory.repository.InventoryMovementRepository;
import com.solusi.erp.inventory.repository.ProductRepository;
import com.solusi.erp.inventory.repository.StockBalanceRepository;
import com.solusi.erp.inventory.repository.ContainerRepository;
import com.solusi.erp.inventory.service.impl.StockServiceImpl;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StockServiceTest {

    @Mock
    private StockBalanceRepository stockBalanceRepository;

    @Mock
    private InventoryMovementRepository inventoryMovementRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ContainerRepository containerRepository;

    @Mock
    private UomConversionService uomConversionService;

    @Mock
    private ValuationService valuationService;

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private StockServiceImpl stockService;

    private Product product;
    private Container container;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
        product.setCode("P001");

        container = new Container();
        container.setId(1L);
        container.setCode("C001");
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
        when(containerRepository.getReferenceById(1L)).thenReturn(container);
        when(stockBalanceRepository.findByProductIdAndContainerIdAndSerialNumber(1L, 1L, null)).thenReturn(Optional.empty());

        stockService.adjust(payload);

        verify(stockBalanceRepository).save(argThat(sb -> 
            sb.getQuantity().compareTo(BigDecimal.TEN) == 0 &&
            sb.getReservedQuantity().compareTo(BigDecimal.ZERO) == 0
        ));
        verify(inventoryMovementRepository).save(any(InventoryMovement.class));
    }

    @Test
    void shouldThrowExceptionIfStockBecomesNegative() {
        StockMovementPayload payload = StockMovementPayload.builder()
                .productId(1L)
                .containerId(1L)
                .quantity(BigDecimal.TEN)
                .movementType(MovementType.ISSUE)
                .build();

        StockBalance existingBalance = new StockBalance();
        existingBalance.setProduct(product);
        existingBalance.setContainer(container);
        existingBalance.setQuantity(BigDecimal.ONE); // Only 1 on hand, trying to issue 10

        when(productRepository.getReferenceById(1L)).thenReturn(product);
        when(stockBalanceRepository.findByProductIdAndContainerIdAndSerialNumber(1L, 1L, null)).thenReturn(Optional.of(existingBalance));

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

        StockBalance existingBalance = new StockBalance();
        existingBalance.setProduct(product);
        existingBalance.setContainer(container);
        existingBalance.setQuantity(BigDecimal.TEN);
        existingBalance.setReservedQuantity(BigDecimal.ZERO);

        when(productRepository.getReferenceById(1L)).thenReturn(product);
        when(containerRepository.getReferenceById(1L)).thenReturn(container);
        when(stockBalanceRepository.findByProductIdAndContainerIdAndSerialNumber(1L, 1L, null)).thenReturn(Optional.of(existingBalance));

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

        StockBalance existingBalance = new StockBalance();
        existingBalance.setProduct(product);
        existingBalance.setContainer(container);
        existingBalance.setQuantity(BigDecimal.TEN);
        existingBalance.setReservedQuantity(BigDecimal.valueOf(5));

        when(productRepository.getReferenceById(1L)).thenReturn(product);
        when(containerRepository.getReferenceById(1L)).thenReturn(container);
        when(stockBalanceRepository.findByProductIdAndContainerIdAndSerialNumber(1L, 1L, null)).thenReturn(Optional.of(existingBalance));

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

        StockBalance existingBalance = new StockBalance();
        existingBalance.setProduct(product);
        existingBalance.setContainer(container);
        existingBalance.setQuantity(BigDecimal.TEN);

        when(productRepository.getReferenceById(1L)).thenReturn(product);
        when(containerRepository.getReferenceById(1L)).thenReturn(container);
        when(stockBalanceRepository.findByProductIdAndContainerIdAndSerialNumber(1L, 1L, null)).thenReturn(Optional.of(existingBalance));

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

        StockBalance existingBalance = new StockBalance();
        existingBalance.setProduct(product);
        existingBalance.setContainer(container);
        existingBalance.setQuantity(BigDecimal.TEN);
        existingBalance.setReservedQuantity(BigDecimal.valueOf(5));

        when(productRepository.getReferenceById(1L)).thenReturn(product);
        when(containerRepository.getReferenceById(1L)).thenReturn(container);
        when(stockBalanceRepository.findByProductIdAndContainerIdAndSerialNumber(1L, 1L, null)).thenReturn(Optional.of(existingBalance));

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
        when(containerRepository.getReferenceById(1L)).thenReturn(container);
        when(stockBalanceRepository.findByProductIdAndContainerIdAndSerialNumber(1L, 1L, null)).thenReturn(Optional.empty());

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

        StockBalance existingBalance = new StockBalance();
        existingBalance.setProduct(product);
        existingBalance.setContainer(container);
        existingBalance.setQuantity(BigDecimal.TEN);
        existingBalance.setReservedQuantity(BigDecimal.ONE); // Only 1 reserved, trying to release 10

        when(productRepository.getReferenceById(1L)).thenReturn(product);
        when(stockBalanceRepository.findByProductIdAndContainerIdAndSerialNumber(1L, 1L, null)).thenReturn(Optional.of(existingBalance));

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
        when(containerRepository.getReferenceById(1L)).thenReturn(container);
        when(stockBalanceRepository.findByProductIdAndContainerIdAndSerialNumber(eq(1L), eq(1L), anyString())).thenReturn(Optional.empty());

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
        when(containerRepository.getReferenceById(1L)).thenReturn(container);
        when(uomConversionService.convertToBaseUom(1L, 2L, BigDecimal.valueOf(2)))
                .thenReturn(BigDecimal.valueOf(48)); // 2 Boxes * 24 factor
        when(stockBalanceRepository.findByProductIdAndContainerIdAndSerialNumber(1L, 1L, null))
                .thenReturn(Optional.empty());

        stockService.adjust(payload);

        verify(stockBalanceRepository).save(argThat(sb -> 
            sb.getQuantity().compareTo(BigDecimal.valueOf(48)) == 0
        ));
    }
}
