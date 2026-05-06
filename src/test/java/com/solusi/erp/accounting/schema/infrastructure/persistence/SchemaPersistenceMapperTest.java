package com.solusi.erp.accounting.schema.infrastructure.persistence;

import com.solusi.erp.accounting.schema.domain.model.SchemaEventType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class SchemaPersistenceMapperTest {

    private final SchemaPersistenceMapper mapper = new SchemaPersistenceMapper();

    @Test
    void toDomain_allowsPersistedSchemaWithoutLines() {
        AccountingSchema entity = new AccountingSchema();
        entity.setId(2L);
        entity.setEventType(SchemaEventType.VENDOR_BILL.name());
        entity.setDescription("Vendor bill schema");
        entity.setIsActive(true);

        assertThatCode(() -> mapper.toDomain(entity)).doesNotThrowAnyException();
        assertThat(mapper.toDomain(entity).getLines()).isEmpty();
    }
}
