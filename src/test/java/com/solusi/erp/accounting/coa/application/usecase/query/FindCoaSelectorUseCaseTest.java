package com.solusi.erp.accounting.coa.application.usecase.query;

import com.solusi.erp.accounting.coa.domain.model.AccountType;
import com.solusi.erp.accounting.coa.domain.model.ChartOfAccount;
import com.solusi.erp.accounting.coa.domain.repository.CoaRepository;
import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.domain.model.Pageable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FindCoaSelectorUseCase Tests")
class FindCoaSelectorUseCaseTest {

    @Mock
    private CoaRepository repository;

    private FindCoaSelectorUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new FindCoaSelectorUseCaseImpl(repository);
    }

    @Test
    @DisplayName("execute normalizes blank filters and returns root account without parent metadata")
    void execute_normalizesBlankFiltersAndReturnsRootWithoutParentMetadata() {
        ChartOfAccount root = new ChartOfAccount(
                new AuditMetadata(10L, 1L, null, null, null, null),
                "1000", "Assets", null, AccountType.ASSET.getDefaultNormalBalance(),
                null, 1, true, null, true
        );

        Pageable pageable = new Pageable(0, 10);
        when(repository.findAllActiveForSelector(null, null, pageable))
                .thenReturn(new com.solusi.erp.core.domain.model.Page<>(List.of(root), 0, 10, 1L));
        when(repository.findAllActive()).thenReturn(List.of(root));

        com.solusi.erp.core.domain.model.Page<CoaSelectorRow> result = useCase.execute("   ", "   ", pageable);

        assertThat(result.content()).singleElement().satisfies(row -> {
            assertThat(row.id()).isEqualTo(10L);
            assertThat(row.accountType()).isNull();
            assertThat(row.parentId()).isNull();
            assertThat(row.parentCode()).isNull();
            assertThat(row.parentName()).isNull();
        });
        verify(repository).findAllActiveForSelector(null, null, pageable);
    }

    @Test
    @DisplayName("execute trims keyword and uppercases account type")
    void execute_trimsKeywordAndUppercasesAccountType() {
        Pageable pageable = new Pageable(0, 10);
        when(repository.findAllActiveForSelector("cash", "ASSET", pageable))
                .thenReturn(new com.solusi.erp.core.domain.model.Page<>(List.of(), 0, 10, 0L));
        when(repository.findAllActive()).thenReturn(List.of());

        com.solusi.erp.core.domain.model.Page<CoaSelectorRow> result = useCase.execute("  cash  ", " asset ", pageable);

        assertThat(result.content()).isEmpty();
        verify(repository).findAllActiveForSelector("cash", "ASSET", pageable);
    }

    @Test
    @DisplayName("execute returns paged selector rows and keeps parent metadata")
    void execute_returnsPagedSelectorRowsAndKeepsParentMetadata() {
        ChartOfAccount parent = new ChartOfAccount(
                new AuditMetadata(10L, 1L, null, null, null, null),
                "1000", "Assets", AccountType.ASSET, AccountType.ASSET.getDefaultNormalBalance(),
                null, 1, true, null, true
        );

        ChartOfAccount child = new ChartOfAccount(
                new AuditMetadata(11L, 1L, null, null, null, null),
                "1100", "Cash", AccountType.ASSET, AccountType.ASSET.getDefaultNormalBalance(),
                10L, 2, false, null, true
        );

        Pageable pageable = new Pageable(0, 1);
        when(repository.findAllActiveForSelector("cash", "ASSET", pageable))
                .thenReturn(new com.solusi.erp.core.domain.model.Page<>(List.of(child), 0, 1, 1L));
        when(repository.findAllActive()).thenReturn(List.of(parent, child));

        com.solusi.erp.core.domain.model.Page<CoaSelectorRow> result = useCase.execute("cash", "ASSET", pageable);

        assertThat(result.totalElements()).isEqualTo(1);
        assertThat(result.content()).singleElement().satisfies(row -> {
            assertThat(row.id()).isEqualTo(11L);
            assertThat(row.parentId()).isEqualTo(10L);
            assertThat(row.parentCode()).isEqualTo("1000");
            assertThat(row.parentName()).isEqualTo("Assets");
        });
        verify(repository).findAllActiveForSelector("cash", "ASSET", pageable);
    }
}
