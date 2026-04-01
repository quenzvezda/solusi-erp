package com.solusi.erp.inventory.product.application.usecase.query;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.inventory.product.domain.model.Product;
import com.solusi.erp.inventory.product.domain.repository.ProductRepository;
import com.solusi.erp.inventory.uom.infrastructure.persistence.UomEntity;
import com.solusi.erp.inventory.uom.infrastructure.persistence.UomJpaRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class GetProductLookupUseCaseImplTest {

    @Test
    void getById_returnsLookupDtoWithUomMetadata() {
        ProductRepository productRepository = mock(ProductRepository.class);
        UomJpaRepository uomRepository = mock(UomJpaRepository.class);
        GetProductLookupUseCaseImpl useCase = new GetProductLookupUseCaseImpl(productRepository, uomRepository);

        Product product = Product.createNew(
                "PRD-001", "Widget", "BAR-001", null,
                1L, 15L, 1L, null, true, false,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                1L, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 1L
        );
        product = new Product(
                new AuditMetadata(1L, 1L, null, null, null, null),
                product.getCode(), product.getName(), product.getBarcode(), null,
                1L, 15L, 1L, null, true, false,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                1L, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 1L
        );

        UomEntity uom = new UomEntity();
        uom.setId(15L);
        uom.setCode("PCS");
        uom.setName("Pieces");

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(uomRepository.findById(15L)).thenReturn(Optional.of(uom));

        LookupDto result = useCase.getById(1L);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("Widget");
        assertThat(result.subText()).isEqualTo("PRD-001");
        assertThat(result.payload()).containsEntry("uomId", 15L);
        assertThat(result.payload()).containsEntry("uomName", "Pieces");
        assertThat(result.payload()).containsEntry("uomCode", "PCS");
        assertThat(result.payload()).containsEntry("isSerialized", false);
    }

    @Test
    void search_returnsLookupDtoWithUomMetadata() {
        ProductRepository productRepository = mock(ProductRepository.class);
        UomJpaRepository uomRepository = mock(UomJpaRepository.class);
        GetProductLookupUseCaseImpl useCase = new GetProductLookupUseCaseImpl(productRepository, uomRepository);

        Product product = Product.createNew(
                "PRD-002", "Gadget", "BAR-002", null,
                1L, 16L, 1L, null, true, true,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                1L, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 1L
        );
        product = new Product(
                new AuditMetadata(2L, 1L, null, null, null, null),
                product.getCode(), product.getName(), product.getBarcode(), null,
                1L, 16L, 1L, null, true, true,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                1L, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 1L
        );

        UomEntity uom = new UomEntity();
        uom.setId(16L);
        uom.setCode("BOX");
        uom.setName("Box");

        when(productRepository.findAll(eq("gad"), any())).thenReturn(new Page<>(List.of(product), 0, 10, 1L));
        when(uomRepository.findById(16L)).thenReturn(Optional.of(uom));

        List<LookupDto> result = useCase.search("gad", 10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).payload()).containsEntry("uomId", 16L);
        assertThat(result.get(0).payload()).containsEntry("uomName", "Box");
        assertThat(result.get(0).payload()).containsEntry("uomCode", "BOX");
        assertThat(result.get(0).payload()).containsEntry("isSerialized", true);
        verify(uomRepository, atLeastOnce()).findById(16L);
    }
}
