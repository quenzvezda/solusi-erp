package com.solusi.erp.purchasing.supplierpricelist.application.usecase.query;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.purchasing.supplierpricelist.domain.model.SupplierPriceList;
import com.solusi.erp.purchasing.supplierpricelist.domain.repository.SupplierPriceListRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("FindSupplierPriceListsUseCase Tests")
class FindSupplierPriceListsUseCaseTest {

    @Mock
    private SupplierPriceListRepository repository;

    private FindSupplierPriceListsUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new FindSupplierPriceListsUseCaseImpl(repository);
    }

    @Test
    @DisplayName("execute delegates keyword and pageable to repository")
    void execute_delegatesToRepository() {
        Pageable pageable = Pageable.of(0, 20);
        Page<SupplierPriceList> expected = new Page<>(List.of(), 0, 20, 0L);
        when(repository.findAll("test", pageable)).thenReturn(expected);

        Page<SupplierPriceList> result = useCase.execute("test", pageable);

        assertThat(result).isEqualTo(expected);
        verify(repository).findAll("test", pageable);
    }
}
