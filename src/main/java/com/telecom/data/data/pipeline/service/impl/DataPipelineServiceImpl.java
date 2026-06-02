package com.telecom.data.data.pipeline.service.impl;

import com.telecom.data.data.pipeline.enums.TransactionStatus;
import com.telecom.data.data.pipeline.model.CleanTransaction;
import com.telecom.data.data.pipeline.model.RawTransaction;
import com.telecom.data.data.pipeline.repository.impl.JdbcDataPipelineRepository;
import com.telecom.data.data.pipeline.service.DataPipelineService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
@RequiredArgsConstructor
public class DataPipelineServiceImpl implements DataPipelineService {
    private final JdbcDataPipelineRepository jdbcDataPipelineRepository;

    @Value("${pipeline.batch-size: 500}")
    private int batchSize;

    @Value("${pipeline.thread-count: 4}")
    private int threadCount;
    @Override
    public void processCsvFile(String filePath) throws Exception {
        List<String[]> allLines = readCsv(filePath); //read all lines from Csv

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount); //делим на части по количеству потоков
        List<Future<List<RawTransaction>>> futures = new ArrayList<>();
        int chunkSize = allLines.size() / threadCount;
        for (int i = 0; i < threadCount; i++) {
            int from = i * chunkSize;
            int to = (i == threadCount - 1) ? allLines.size() : from + chunkSize;
            List<String[]> chunk =  allLines.subList(from, to);
            futures.add(executorService.submit(() -> validateChunk(chunk))); //each thread validates its own part
        }
        List<RawTransaction> allRaw = new ArrayList<>(); //collecting the results of all thread
        for (Future<List<RawTransaction>> future : futures) {
            allRaw.addAll(future.get());
        }
        executorService.shutdown();

        jdbcDataPipelineRepository.saveRawTransactionsBatch(allRaw); //save everything to the db in batches

        //save only clean ones separately
        List<CleanTransaction> cleanTransactions = allRaw.stream().filter(t -> t.getStatus() == TransactionStatus.VALID).map(this::toClean).toList();
        jdbcDataPipelineRepository.saveCleanTransactionsBatch(cleanTransactions);
    }
    private List<String[]> readCsv(String filePath) throws Exception {
        List<String[]> lines = new ArrayList<>();
        try(BufferedReader br = new BufferedReader(new FileReader(filePath))){
            String line;

            br.readLine(); //пропускаем заголовок CSV
            while ((line = br.readLine()) != null) {
                lines.add(line.split(","));
            }
            return lines;
        }
    }
    private List<RawTransaction> validateChunk(List<String[]> chunk) {
        List<RawTransaction> result = new ArrayList<>();
        for(String[] row : chunk){
            RawTransaction tx = RawTransaction.builder().senderAccountId(row[0]).receiverAccountId(row[1]).amount(row[2]).category(row[3]).description(row[4]).transactionDate(row[5]).build();
            //validation
            if(isInvalidAmount(tx.getAmount())){
                tx.setStatus(TransactionStatus.INVALID);
                tx.setErrorMessage("Invalid amount: " + tx.getAmount());
            } else if (isInvalidId(tx.getSenderAccountId())) {
                tx.setStatus(TransactionStatus.INVALID);
                tx.setErrorMessage("Invalid sender account: " + tx.getSenderAccountId());
            }else {
                tx.setStatus(TransactionStatus.VALID);
            }
            result.add(tx);
        }
        return result;
    }

    private boolean isInvalidAmount(String amount) {
        try{
            double val = Double.parseDouble(amount);
            return val < 0;
        } catch (NumberFormatException e) {
            return true;
        }
    }
    private boolean isInvalidId(String id) {
        try{
            Long.parseLong(id);
            return false;
        }catch (NumberFormatException e){ return true; }
    }

    private CleanTransaction toClean(RawTransaction tx) {
        return CleanTransaction.builder().senderAccountId(Long.parseLong(tx.getSenderAccountId().trim())).receiverAccountId(Long.parseLong(tx.getReceiverAccountId().trim())).amount(new BigDecimal(tx.getAmount().trim())).category(tx.getCategory().trim()).description(tx.getDescription().trim()).transactionDate(LocalDateTime.parse(tx.getTransactionDate().trim())).build();
    }
}
