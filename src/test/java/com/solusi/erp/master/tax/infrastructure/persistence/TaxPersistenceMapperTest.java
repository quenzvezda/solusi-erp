package com.solusi.erp.master.tax.infrastructure.persistence;

import com.solusi.erp.master.tax.domain.model.TaxCalculationMode;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class TaxPersistenceMapperTest {

    private final TaxPersistenceMapper mapper = Mappers.getMapper(TaxPersistenceMapper.class);

    @Test
    void toDomain_mapsCalculationModeFromEntity() {
        Tax entity = new Tax();
        entity.setCode("PPN");
        entity.setName("PPN 11%");
        entity.setRate(BigDecimal.valueOf(11));
        entity.setIsSubtract(false);
        entity.setIsActive(true);
        entity.setCalculationMode(TaxCalculationMode.INCLUSIVE);

        com.solusi.erp.master.tax.domain.model.Tax domain = mapper.toDomain(entity);

        assertThat(domain.getCalculationMode()).isEqualTo(TaxCalculationMode.INCLUSIVE);
    }

    @Test
    void toEntity_mapsCalculationModeFromDomain() {
        com.solusi.erp.master.tax.domain.model.Tax domain =
                com.solusi.erp.master.tax.domain.model.Tax.createNew(
                        "PPN",
                        "PPN 11%",
                        BigDecimal.valueOf(11),
                        null,
                        false,
                        true,
                        TaxCalculationMode.INCLUSIVE
                );

        Tax entity = mapper.toEntity(domain);

        assertThat(entity.getCalculationMode()).isEqualTo(TaxCalculationMode.INCLUSIVE);
    }
}
