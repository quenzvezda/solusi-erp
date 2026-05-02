package com.solusi.erp.accounting.schema.domain.model;

import com.solusi.erp.core.domain.model.AuditMetadata;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AccountingSchema Domain Model Tests")
class AccountingSchemaTest {

    @Test
    @DisplayName("createNew sets all fields with empty metadata")
    void createNew_setsAllFields() {
        AccountingSchema schema = AccountingSchema.createNew(
                SchemaEventType.GOODS_RECEIPT, "GR schema", 1L, 2L, 3L, true);
        assertThat(schema.getId()).isNull();
        assertThat(schema.getEventType()).isEqualTo(SchemaEventType.GOODS_RECEIPT);
        assertThat(schema.getDescription()).isEqualTo("GR schema");
        assertThat(schema.getDebitAccountId()).isEqualTo(1L);
        assertThat(schema.getCreditAccountId()).isEqualTo(2L);
        assertThat(schema.getTaxAccountId()).isEqualTo(3L);
        assertThat(schema.getIsActive()).isTrue();
    }

    @Test
    @DisplayName("createNew accepts optional taxAccountId")
    void createNew_acceptsOptionalTaxAccountId() {
        AccountingSchema schema = AccountingSchema.createNew(
                SchemaEventType.VENDOR_BILL, "desc", 1L, 2L, null, true);

        assertThat(schema.getTaxAccountId()).isNull();
    }

    @Test
    @DisplayName("createNew defaults isActive to true when null")
    void createNew_defaultsIsActiveWhenNull() {
        AccountingSchema schema = AccountingSchema.createNew(
                SchemaEventType.VENDOR_BILL, "desc", 1L, 2L, null, null);
        assertThat(schema.getIsActive()).isTrue();
    }

    @Test
    @DisplayName("full constructor preserves metadata")
    void constructor_preservesMetadata() {
        AuditMetadata meta = new AuditMetadata(5L, 2L, null, null, null, null);
        AccountingSchema schema = new AccountingSchema(meta, SchemaEventType.VENDOR_PAYMENT,
                "desc", 10L, 20L, 30L, true);
        assertThat(schema.getId()).isEqualTo(5L);
        assertThat(schema.getMetadata()).isEqualTo(meta);
    }

    @Test
    @DisplayName("update changes description, accounts, tax account, and isActive")
    void update_changesTaxAccountId() {
        AccountingSchema schema = AccountingSchema.createNew(
                SchemaEventType.GOODS_RECEIPT, "old", 1L, 2L, null, true);
        schema.update("new desc", 10L, 20L, 30L, false);
        assertThat(schema.getDescription()).isEqualTo("new desc");
        assertThat(schema.getDebitAccountId()).isEqualTo(10L);
        assertThat(schema.getCreditAccountId()).isEqualTo(20L);
        assertThat(schema.getTaxAccountId()).isEqualTo(30L);
        assertThat(schema.getIsActive()).isFalse();
        assertThat(schema.getEventType()).isEqualTo(SchemaEventType.GOODS_RECEIPT);
    }

    @Test
    @DisplayName("softDelete sets isActive to false")
    void softDelete_setsIsActiveFalse() {
        AccountingSchema schema = AccountingSchema.createNew(
                SchemaEventType.GOODS_RECEIPT, "desc", 1L, 2L, null, true);
        schema.softDelete();
        assertThat(schema.getIsActive()).isFalse();
    }
}
