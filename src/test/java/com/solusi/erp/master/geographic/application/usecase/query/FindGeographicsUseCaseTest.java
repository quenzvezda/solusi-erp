package com.solusi.erp.master.geographic.application.usecase.query;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.master.geographic.domain.model.Geographic;
import com.solusi.erp.master.geographic.domain.repository.GeographicRepository;
import com.solusi.erp.master.model.GeographicType;
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
@DisplayName("FindGeographicsUseCase Tests")
class FindGeographicsUseCaseTest {

    @Mock
    private GeographicRepository repository;

    private FindGeographicsUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new FindGeographicsUseCaseImpl(repository);
    }

    @Test
    @DisplayName("execute delegates keyword, parentId and pageable to repository")
    void execute_delegatesToRepository() {
        Pageable pageable = Pageable.of(0, 20);
        Geographic geo = Geographic.createNew("ID", "Indonesia", GeographicType.COUNTRY,
                null, null, Boolean.TRUE);
        Page<Geographic> expectedPage = new Page<>(List.of(geo), 0, 20, 1L);

        when(repository.findAll("indo", null, pageable)).thenReturn(expectedPage);

        Page<Geographic> result = useCase.execute("indo", null, pageable);

        assertThat(result).isEqualTo(expectedPage);
    }

    @Test
    @DisplayName("execute filters by parentId when provided")
    void execute_filtersByParentId() {
        Pageable pageable = Pageable.of(0, 20);
        Geographic province = Geographic.createNew("DKI", "DKI Jakarta",
                GeographicType.STATE_PROVINCE, 1L, "Indonesia", Boolean.TRUE);
        Page<Geographic> expectedPage = new Page<>(List.of(province), 0, 20, 1L);

        when(repository.findAll(null, 1L, pageable)).thenReturn(expectedPage);

        Page<Geographic> result = useCase.execute(null, 1L, pageable);

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).getParentId()).isEqualTo(1L);
    }
}
