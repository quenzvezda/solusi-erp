package com.solusi.erp.purchasing.supplierpricelist.application.usecase.query;

import com.solusi.erp.core.domain.model.AuditMetadata;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetSupplierPriceListEditViewUseCase Tests")
class GetSupplierPriceListEditViewUseCaseTest {

    @Mock
    private SupplierPriceListRepository repository;

    private GetSupplierPriceListEditViewUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetSupplierPriceListEditViewUseCaseImpl(repository);
    }

    @Test
    @DisplayName("execute returns price list when found")
    void execute_returnsPriceListWhenFound() {
        AuditMetadata metadata = new AuditMetadata(1L, 1L, null, null, null, null);
        SupplierPriceList spl = new SupplierPriceList(metadata, "SPL-001",
            1L, 2L, 3L, 4L,
            new BigDecimal("100.0000"), new BigDecimal("1.0000"),
            LocalDate.of(2026, 7, 1), null, null, true);

        when(repository.findById(1L)).thenReturn(Optional.of(spl));

        Optional<SupplierPriceList> result = useCase.execute(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getCode()).isEqualTo("SPL-001");
    }

    @Test
    @DisplayName("execute returns empty when not found")
    void execute_returnsEmptyWhenNotFound() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        Optional<SupplierPriceList> result = useCase.execute(999L);

        assertThat(result).isEmpty();
    }
}
