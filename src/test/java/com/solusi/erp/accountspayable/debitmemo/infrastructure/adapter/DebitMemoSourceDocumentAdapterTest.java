package com.solusi.erp.accountspayable.debitmemo.infrastructure.adapter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.List;
import java.util.Optional;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DebitMemoSourceDocumentAdapterTest {

    @Mock
    private NamedParameterJdbcTemplate jdbcTemplate;

    private DebitMemoSourceDocumentAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new DebitMemoSourceDocumentAdapter(jdbcTemplate);
    }

    @Test
    void findGeneratedGoodsIssueId_should_return_empty_without_purchase_return_id() {
        Optional<Long> result = adapter.findGeneratedGoodsIssueId(null);

        assertThat(result).isEmpty();
        verifyNoInteractions(jdbcTemplate);
    }

    @Test
    void findGeneratedGoodsIssueId_should_return_positive_generated_goods_issue_id() {
        when(jdbcTemplate.query(any(String.class), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(List.of(900L));

        Optional<Long> result = adapter.findGeneratedGoodsIssueId(100L);

        assertThat(result).contains(900L);
        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<MapSqlParameterSource> paramsCaptor = ArgumentCaptor.forClass(MapSqlParameterSource.class);
        verify(jdbcTemplate).query(sqlCaptor.capture(), paramsCaptor.capture(), any(RowMapper.class));
        assertThat(sqlCaptor.getValue()).contains("pur_purchase_returns");
        assertThat(paramsCaptor.getValue().getValue("purchaseReturnId")).isEqualTo(100L);
    }

    @Test
    void findGeneratedGoodsIssueId_should_ignore_null_and_zero_values() {
        when(jdbcTemplate.query(any(String.class), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(Arrays.asList(null, 0L));

        Optional<Long> result = adapter.findGeneratedGoodsIssueId(100L);

        assertThat(result).isEmpty();
    }
}
