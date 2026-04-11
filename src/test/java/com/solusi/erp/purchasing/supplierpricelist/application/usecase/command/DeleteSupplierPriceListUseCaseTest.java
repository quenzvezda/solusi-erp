package com.solusi.erp.purchasing.supplierpricelist.application.usecase.command;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.purchasing.supplierpricelist.domain.model.SupplierPriceList;
import com.solusi.erp.purchasing.supplierpricelist.domain.repository.SupplierPriceListRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeleteSupplierPriceListUseCase Tests")
class DeleteSupplierPriceListUseCaseTest {

    @Mock
    private SupplierPriceListRepository repository;

    private DeleteSupplierPriceListUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new DeleteSupplierPriceListUseCaseImpl(repository);
    }

    @Test
    @DisplayName("execute deactivates existing price list (soft delete)")
    void execute_deactivatesExistingPriceList() {
        AuditMetadata metadata = new AuditMetadata(1L, 1L, null, null, null, null);
        SupplierPriceList existing = new SupplierPriceList(metadata, "SPL-001",
            1L, 2L, 3L, 4L,
            new BigDecimal("100.0000"), new BigDecimal("1.0000"),
            LocalDate.of(2026, 7, 1), null, null, true);

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(SupplierPriceList.class))).thenAnswer(inv -> inv.getArgument(0));

        useCase.execute(1L);

        verify(repository).save(any(SupplierPriceList.class));
    }

    @Test
    @DisplayName("execute throws DomainException when not found")
    void execute_throwsWhenNotFound() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(999L))
            .isInstanceOf(DomainException.class)
            .hasMessageContaining("msg.error.spl.notfound");
    }
}
