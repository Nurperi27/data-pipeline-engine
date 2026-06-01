package com.telecom.data.data.pipeline.repository;

import com.telecom.data.data.pipeline.model.CleanTransaction;
import com.telecom.data.data.pipeline.model.RawTransaction;

import java.util.List;

public interface DataPipelineRepository {
    void saveRawTransactionsBatch(List<RawTransaction> rawTransactions);
    void saveCleanTransactionsBatch(List<CleanTransaction> cleanTransactions);
}
