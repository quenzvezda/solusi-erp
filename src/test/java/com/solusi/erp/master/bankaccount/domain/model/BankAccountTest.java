package com.solusi.erp.master.bankaccount.domain.model;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.master.shared.model.PaymentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("BankAccount Domain Model Tests")
class BankAccountTest {

    @Test
    @DisplayName("createNew sets all fields with empty metadata (id == null)")
    void createNew_setsAllFieldsWithEmptyMetadata() {
        BankAccount account = BankAccount.createNew(
                "BA-001", "Bank BCA", "Sudirman", "John Doe", "1234567890",
                PaymentType.BANK_TRANSFER, "Test note", 1L, 2L, true, 1L, 10L);

        assertThat(account.getCode()).isEqualTo("BA-001");
        assertThat(account.getBankName()).isEqualTo("Bank BCA");
        assertThat(account.getBranch()).isEqualTo("Sudirman");
        assertThat(account.getAccountName()).isEqualTo("John Doe");
        assertThat(account.getAccountNo()).isEqualTo("1234567890");
        assertThat(account.getAccountType()).isEqualTo(PaymentType.BANK_TRANSFER);
        assertThat(account.getNote()).isEqualTo("Test note");
        assertThat(account.getCityId()).isEqualTo(1L);
        assertThat(account.getPartyId()).isEqualTo(2L);
        assertThat(account.getIsActive()).isTrue();
        assertThat(account.getCurrencyId()).isEqualTo(1L);
        assertThat(account.getCoaId()).isEqualTo(10L);
        assertThat(account.getId()).isNull();
    }

    @Test
    @DisplayName("Full constructor preserves all fields including metadata")
    void constructor_preservesAllFields() {
        AuditMetadata metadata = new AuditMetadata(10L, 1L, null, null, null, null);
        BankAccount account = new BankAccount(metadata, "BA-001", "Bank BCA", "Sudirman",
                "John Doe", "1234567890", PaymentType.BANK_TRANSFER, "Note",
                1L, "Jakarta", 2L, "PT ABC", true, 1L, 10L);

        assertThat(account.getId()).isEqualTo(10L);
        assertThat(account.getCode()).isEqualTo("BA-001");
        assertThat(account.getBankName()).isEqualTo("Bank BCA");
        assertThat(account.getCityName()).isEqualTo("Jakarta");
        assertThat(account.getPartyName()).isEqualTo("PT ABC");
        assertThat(account.getCurrencyId()).isEqualTo(1L);
        assertThat(account.getCoaId()).isEqualTo(10L);
        assertThat(account.getMetadata()).isEqualTo(metadata);
    }

    @Test
    @DisplayName("update changes mutable fields; code remains unchanged")
    void update_changesMutableFields() {
        BankAccount account = BankAccount.createNew(
                "BA-001", "Old Bank", "Old Branch", "Old Name", "000",
                PaymentType.CASH, "Old Note", 1L, 2L, true, 1L, null);

        account.update("New Bank", "New Branch", "New Name", "999",
                PaymentType.BANK_TRANSFER, "New Note", 3L, 4L, false, 2L, 10L);

        assertThat(account.getBankName()).isEqualTo("New Bank");
        assertThat(account.getBranch()).isEqualTo("New Branch");
        assertThat(account.getAccountName()).isEqualTo("New Name");
        assertThat(account.getAccountNo()).isEqualTo("999");
        assertThat(account.getAccountType()).isEqualTo(PaymentType.BANK_TRANSFER);
        assertThat(account.getNote()).isEqualTo("New Note");
        assertThat(account.getCityId()).isEqualTo(3L);
        assertThat(account.getPartyId()).isEqualTo(4L);
        assertThat(account.getIsActive()).isFalse();
        assertThat(account.getCurrencyId()).isEqualTo(2L);
        assertThat(account.getCoaId()).isEqualTo(10L);
        assertThat(account.getCode()).isEqualTo("BA-001");
    }
}
