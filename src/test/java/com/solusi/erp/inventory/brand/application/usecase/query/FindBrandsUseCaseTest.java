package com.solusi.erp.inventory.brand.application.usecase.query;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.inventory.brand.domain.model.Brand;
import com.solusi.erp.inventory.brand.domain.repository.BrandRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FindBrandsUseCase Tests")
class FindBrandsUseCaseTest {

    @Mock
    private BrandRepository repository;

    private FindBrandsUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new FindBrandsUseCaseImpl(repository);
    }

    @Test
    @DisplayName("execute delegates keyword and pageable to repository and returns its result")
    void execute_delegatesKeywordAndPageableToRepository() {
        Pageable pageable = Pageable.of(0, 20);
        Brand brand = Brand.createNew("BR-001", "Acme", "note");
        Page<Brand> expectedPage = new Page<>(List.of(brand), 0, 20, 1L);

        when(repository.findAll("abc", pageable)).thenReturn(expectedPage);

        Page<Brand> result = useCase.execute("abc", pageable);

        assertThat(result).isEqualTo(expectedPage);
    }
}
