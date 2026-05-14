package com.solusi.erp.accountspayable.vendorbill.infrastructure.adapter;

import com.solusi.erp.accountspayable.vendorbill.domain.port.BillableApReference;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GoodsReceiptBillableReferenceProviderTest {

    @Mock
    private NamedParameterJdbcTemplate jdbcTemplate;

    private GoodsReceiptBillableReferenceProvider provider;

    @BeforeEach
    void setUp() {
        provider = new GoodsReceiptBillableReferenceProvider(jdbcTemplate);
    }

    @Test
    @SuppressWarnings("unchecked")
    void findBillableReferences_should_use_currency_alias_column() {
        when(jdbcTemplate.query(any(String.class), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(List.of());

        List<BillableApReference> result = provider.findBillableReferences(10L, 1L);

        assertThat(result).isEmpty();
        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<MapSqlParameterSource> paramCaptor = ArgumentCaptor.forClass(MapSqlParameterSource.class);
        verify(jdbcTemplate).query(sqlCaptor.capture(), paramCaptor.capture(), any(RowMapper.class));
        assertThat(sqlCaptor.getValue()).contains("c.alias AS currency_code");
        assertThat(sqlCaptor.getValue()).doesNotContain("c.code");
        assertThat(paramCaptor.getValue().getValue("vendorId")).isEqualTo(10L);
        assertThat(paramCaptor.getValue().getValue("currencyId")).isEqualTo(1L);
    }
}
