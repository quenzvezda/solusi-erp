package com.solusi.erp.accountspayable.debitmemo.infrastructure.adapter;

import com.solusi.erp.accountspayable.debitmemo.domain.port.DebitMemoSourceDocumentPort;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.Optional;

public class DebitMemoSourceDocumentAdapter implements DebitMemoSourceDocumentPort {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public DebitMemoSourceDocumentAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<Long> findGeneratedGoodsIssueId(Long purchaseReturnId) {
        if (purchaseReturnId == null) {
            return Optional.empty();
        }
        return jdbcTemplate.query("""
                        SELECT generated_gi_id
                        FROM pur_purchase_returns
                        WHERE id = :purchaseReturnId
                        """,
                new MapSqlParameterSource("purchaseReturnId", purchaseReturnId),
                (rs, rowNum) -> rs.getLong("generated_gi_id")
        ).stream().filter(id -> id != null && id > 0).findFirst();
    }
}

