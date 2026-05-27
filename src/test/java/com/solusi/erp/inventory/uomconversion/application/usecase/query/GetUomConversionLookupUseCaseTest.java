package com.solusi.erp.inventory.uomconversion.application.usecase.query;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.inventory.product.infrastructure.persistence.JpaProductRepository;
import com.solusi.erp.inventory.product.infrastructure.persistence.ProductEntity;
import com.solusi.erp.inventory.uom.domain.model.UomType;
import com.solusi.erp.inventory.uom.infrastructure.persistence.UomEntity;
import com.solusi.erp.inventory.uom.infrastructure.persistence.UomJpaRepository;
import com.solusi.erp.inventory.uomconversion.domain.model.UomConversion;
import com.solusi.erp.inventory.uomconversion.domain.repository.UomConversionRepository;
import com.solusi.erp.inventory.uomconversion.web.dto.UomConversionLookupData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetUomConversionLookupUseCase Tests")
class GetUomConversionLookupUseCaseTest {

    @Mock
    private UomConversionRepository repository;
    @Mock
    private JpaProductRepository productRepo;
    @Mock
    private UomJpaRepository uomRepo;

    private GetUomConversionLookupUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetUomConversionLookupUseCaseImpl(repository, productRepo, uomRepo);
    }

    @Test
    void getConversionsForProduct_throwsWhenProductNotFound() {
        when(productRepo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.getConversionsForProduct(99L))
                .isInstanceOf(DomainException.class)
                .hasMessage("msg.error.product.not-found");
    }

    @Test
    void getConversionsForProduct_throwsWhenBaseUomNotFound() {
        ProductEntity product = product(1L);
        when(productRepo.findById(10L)).thenReturn(Optional.of(product));
        when(uomRepo.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.getConversionsForProduct(10L))
                .isInstanceOf(DomainException.class)
                .hasMessage("msg.error.uom.not-found");
    }

    @Test
    void getConversionsForProduct_returnsBaseAndConversionRows() {
        ProductEntity product = product(1L);
        UomEntity base = uom(1L, "PCS", "Pieces");
        UomEntity carton = uom(2L, "CTN", "Carton");
        UomConversion conversion = new UomConversion(
                new AuditMetadata(100L, 1L, null, null, null, null),
                10L, "P001", "Product", 2L, "Carton", 1L, "Pieces",
                new BigDecimal("12.00")
        );

        when(productRepo.findById(10L)).thenReturn(Optional.of(product));
        when(uomRepo.findById(1L)).thenReturn(Optional.of(base));
        when(repository.findByProductId(10L)).thenReturn(List.of(conversion));
        when(uomRepo.findById(2L)).thenReturn(Optional.of(carton));

        List<UomConversionLookupData> result = useCase.getConversionsForProduct(10L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).isBase()).isTrue();
        assertThat(result.get(0).uomCode()).isEqualTo("PCS");
        assertThat(result.get(0).conversionFactor()).isEqualByComparingTo("1");
        assertThat(result.get(1).isBase()).isFalse();
        assertThat(result.get(1).uomCode()).isEqualTo("CTN");
        assertThat(result.get(1).conversionFactor()).isEqualByComparingTo("12.00");
    }

    @Test
    void getConversionsForProduct_usesBlankCodeWhenConversionUomMissing() {
        ProductEntity product = product(1L);
        UomEntity base = uom(1L, "PCS", "Pieces");
        UomConversion conversion = new UomConversion(
                new AuditMetadata(100L, 1L, null, null, null, null),
                10L, "P001", "Product", 2L, "Carton", 1L, "Pieces",
                new BigDecimal("12.00")
        );

        when(productRepo.findById(10L)).thenReturn(Optional.of(product));
        when(uomRepo.findById(1L)).thenReturn(Optional.of(base));
        when(repository.findByProductId(10L)).thenReturn(List.of(conversion));
        when(uomRepo.findById(2L)).thenReturn(Optional.empty());

        List<UomConversionLookupData> result = useCase.getConversionsForProduct(10L);

        assertThat(result.get(1).uomCode()).isEmpty();
        assertThat(result.get(1).uomName()).isEqualTo("Carton");
    }

    private ProductEntity product(Long baseUomId) {
        ProductEntity entity = new ProductEntity();
        entity.setId(10L);
        entity.setCode("P001");
        entity.setName("Product");
        entity.setCategoryId(1L);
        entity.setUomId(baseUomId);
        return entity;
    }

    private UomEntity uom(Long id, String code, String name) {
        UomEntity entity = new UomEntity();
        entity.setId(id);
        entity.setCode(code);
        entity.setName(name);
        entity.setType(UomType.UNIT);
        return entity;
    }

}
