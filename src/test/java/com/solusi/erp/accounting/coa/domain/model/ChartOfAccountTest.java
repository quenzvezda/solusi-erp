package com.solusi.erp.accounting.coa.domain.model;

import com.solusi.erp.core.domain.model.AuditMetadata;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ChartOfAccount Domain Model Tests")
class ChartOfAccountTest {

    @Test
    @DisplayName("createNew sets all fields with empty metadata (id == null)")
    void createNew_setsAllFieldsWithEmptyMetadata() {
        ChartOfAccount coa = ChartOfAccount.createNew(
                "1000", "Cash", AccountType.ASSET, null, 1, false, "Note", true);

        assertThat(coa.getId()).isNull();
        assertThat(coa.getCode()).isEqualTo("1000");
        assertThat(coa.getName()).isEqualTo("Cash");
        assertThat(coa.getAccountType()).isEqualTo(AccountType.ASSET);
        assertThat(coa.getNormalBalance()).isEqualTo(NormalBalance.DEBIT);
        assertThat(coa.getParentId()).isNull();
        assertThat(coa.getLevel()).isEqualTo(1);
        assertThat(coa.getIsHeader()).isFalse();
        assertThat(coa.getNote()).isEqualTo("Note");
        assertThat(coa.getIsActive()).isTrue();
    }

    @Test
    @DisplayName("createNew derives CREDIT normal balance for LIABILITY")
    void createNew_derivesNormalBalanceForLiability() {
        ChartOfAccount coa = ChartOfAccount.createNew(
                "2000", "Payable", AccountType.LIABILITY, null, 1, false, null, true);
        assertThat(coa.getNormalBalance()).isEqualTo(NormalBalance.CREDIT);
    }

    @Test
    @DisplayName("createNew derives CREDIT normal balance for EQUITY")
    void createNew_derivesNormalBalanceForEquity() {
        ChartOfAccount coa = ChartOfAccount.createNew(
                "3000", "Capital", AccountType.EQUITY, null, 1, false, null, true);
        assertThat(coa.getNormalBalance()).isEqualTo(NormalBalance.CREDIT);
    }

    @Test
    @DisplayName("createNew derives CREDIT normal balance for REVENUE")
    void createNew_derivesNormalBalanceForRevenue() {
        ChartOfAccount coa = ChartOfAccount.createNew(
                "4000", "Sales", AccountType.REVENUE, null, 1, false, null, true);
        assertThat(coa.getNormalBalance()).isEqualTo(NormalBalance.CREDIT);
    }

    @Test
    @DisplayName("createNew derives DEBIT normal balance for EXPENSE")
    void createNew_derivesNormalBalanceForExpense() {
        ChartOfAccount coa = ChartOfAccount.createNew(
                "5000", "Cost", AccountType.EXPENSE, null, 1, false, null, true);
        assertThat(coa.getNormalBalance()).isEqualTo(NormalBalance.DEBIT);
    }

    @Test
    @DisplayName("createNew defaults level=1 when null")
    void createNew_defaultsLevelWhenNull() {
        ChartOfAccount coa = ChartOfAccount.createNew(
                "1000", "Cash", AccountType.ASSET, null, null, null, null, null);
        assertThat(coa.getLevel()).isEqualTo(1);
        assertThat(coa.getIsHeader()).isFalse();
        assertThat(coa.getIsActive()).isTrue();
    }

    @Test
    @DisplayName("createNew with parent sets parentId")
    void createNew_withParent_setsParentId() {
        ChartOfAccount coa = ChartOfAccount.createNew(
                "1010", "Cash in Bank", AccountType.ASSET, 1L, 2, false, null, true);
        assertThat(coa.getParentId()).isEqualTo(1L);
        assertThat(coa.getLevel()).isEqualTo(2);
    }

    @Test
    @DisplayName("full constructor preserves all fields including metadata")
    void constructor_preservesAllFields() {
        AuditMetadata metadata = new AuditMetadata(1L, 1L, null, null, null, null);
        ChartOfAccount coa = new ChartOfAccount(metadata, "1000", "Cash",
                AccountType.ASSET, NormalBalance.DEBIT, null, 1, false, "Note", true);
        assertThat(coa.getId()).isEqualTo(1L);
        assertThat(coa.getMetadata()).isEqualTo(metadata);
        assertThat(coa.getCode()).isEqualTo("1000");
    }

    @Test
    @DisplayName("update changes name, accountType, and recalculates normalBalance")
    void update_changesFieldsAndRecalculatesNormalBalance() {
        ChartOfAccount coa = ChartOfAccount.createNew(
                "1000", "Cash", AccountType.ASSET, null, 1, false, null, true);
        coa.update("Revenue Account", AccountType.REVENUE, null, 1, false, "Updated", true);
        assertThat(coa.getName()).isEqualTo("Revenue Account");
        assertThat(coa.getAccountType()).isEqualTo(AccountType.REVENUE);
        assertThat(coa.getNormalBalance()).isEqualTo(NormalBalance.CREDIT);
        assertThat(coa.getNote()).isEqualTo("Updated");
        assertThat(coa.getCode()).isEqualTo("1000"); // code unchanged
    }

    @Test
    @DisplayName("softDelete sets isActive to false")
    void softDelete_setsIsActiveFalse() {
        ChartOfAccount coa = ChartOfAccount.createNew(
                "1000", "Cash", AccountType.ASSET, null, 1, false, null, true);
        coa.softDelete();
        assertThat(coa.getIsActive()).isFalse();
    }
}
