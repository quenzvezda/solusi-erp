package com.solusi.erp.inventory.productcategory.domain.model;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.inventory.model.ProductCategoryType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ProductCategory Domain Model Tests")
class ProductCategoryTest {

    @Test
    @DisplayName("createNew sets all fields with empty metadata (id == null)")
    void createNew_setsAllFields() {
        ProductCategory category = ProductCategory.createNew("CAT-001", "Electronics", ProductCategoryType.STOCK, "Stock items");

        assertThat(category.getCode()).isEqualTo("CAT-001");
        assertThat(category.getName()).isEqualTo("Electronics");
        assertThat(category.getType()).isEqualTo(ProductCategoryType.STOCK);
        assertThat(category.getNote()).isEqualTo("Stock items");
        assertThat(category.getId()).isNull();
    }

    @Test
    @DisplayName("update changes name, type, and note; code remains unchanged")
    void update_changesNameTypeAndNote() {
        ProductCategory category = ProductCategory.createNew("CAT-001", "Electronics", ProductCategoryType.STOCK, "Original note");

        category.update("Services", ProductCategoryType.SERVICE, "Updated note");

        assertThat(category.getName()).isEqualTo("Services");
        assertThat(category.getType()).isEqualTo(ProductCategoryType.SERVICE);
        assertThat(category.getNote()).isEqualTo("Updated note");
        assertThat(category.getCode()).isEqualTo("CAT-001");
    }
}
