package com.telecom.data.data.pipeline.repository.impl;

import com.telecom.data.data.pipeline.model.CleanTransaction;
import com.telecom.data.data.pipeline.model.RawTransaction;
import com.telecom.data.data.pipeline.repository.DataPipelineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class JdbcDataPipelineRepository implements DataPipelineRepository {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void saveRawTransactionsBatch(List<RawTransaction> rawTransactions) {
        String sql = """
                insert into raw_transactions_import (sender_account_id, receiver_account_id, amount_amount, category, description, transaction_date, import_status, error_message)
                values (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        jdbcTemplate.batchUpdate(sql, rawTransactions, 500, (ps, t) -> {
            ps.setString(1, t.getSenderAccountId());
            ps.setString(2, t.getReceiverAccountId());
            ps.setString(3, t.getAmount());
            ps.setString(4, t.getCategory());
            ps.setString(5, t.getDescription());
            ps.setString(6, t.getTransactionDate());
            ps.setString(7, t.getStatus().name());
            ps.setString(8, t.getErrorMessage());
        });
    }

    @Override
    public void saveCleanTransactionsBatch(List<CleanTransaction> cleanTransactions) {
        String  sql = """
                insert into clean_transactions (sender_account_id, receiver_account_id, amount, category, description, transaction_date)
                values (?, ?, ?, ?, ?, ?)
        """;
        jdbcTemplate.batchUpdate(sql, cleanTransactions, 500, (ps, t) -> {
            ps.setLong(1, t.getSenderAccountId());
            ps.setLong(2, t.getReceiverAccountId());
            ps.setBigDecimal(3, t.getAmount());
            ps.setString(4, t.getCategory());
            ps.setString(5, t.getDescription());
            ps.setTimestamp(6, java.sql.Timestamp.valueOf(t.getTransactionDate()));
        });
    }
}
