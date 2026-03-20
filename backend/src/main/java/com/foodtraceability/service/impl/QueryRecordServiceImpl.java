package com.foodtraceability.service.impl;

import com.foodtraceability.dto.QueryRecordDTO;
import com.foodtraceability.entity.QueryRecord;
import com.foodtraceability.repository.QueryRecordRepository;
import com.foodtraceability.service.QueryRecordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
public class QueryRecordServiceImpl implements QueryRecordService {

    private static final Logger log = LoggerFactory.getLogger(QueryRecordServiceImpl.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private QueryRecordRepository queryRecordRepository;

    private static final int RETENTION_DAYS = 7;

    @Override
    @Transactional
    public QueryRecordDTO recordQuery(String antiFakeCode, String batchNumber, String queryType) {
        Optional<QueryRecord> existingRecord = queryRecordRepository.findByAntiFakeCodeAndIsDeletedFalse(antiFakeCode);
        boolean isQueriedBefore = existingRecord.isPresent();

        QueryRecord record = new QueryRecord();
        record.setAntiFakeCode(antiFakeCode);
        record.setBatchNumber(batchNumber);
        record.setQueryTime(LocalDateTime.now());
        record.setQueryType(queryType);
        record.setIsQueriedBefore(isQueriedBefore);
        record.setIsBackedUp(false);
        record.setIsDeleted(false);

        queryRecordRepository.save(record);

        QueryRecordDTO dto = new QueryRecordDTO();
        dto.setIsQueriedBefore(isQueriedBefore);
        dto.setQueryTime(existingRecord.map(r -> r.getQueryTime().format(FORMATTER)).orElse(null));

        return dto;
    }

    @Override
    public boolean hasBeenQueriedBefore(String antiFakeCode) {
        return queryRecordRepository.findByAntiFakeCodeAndIsDeletedFalse(antiFakeCode).isPresent();
    }

    @Override
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cleanupOldRecords() {
        log.info("[查询记录清理] 开始清理 {} 天前的查询记录", RETENTION_DAYS);

        LocalDateTime cutoffTime = LocalDateTime.now().minusDays(RETENTION_DAYS);

        var recordsToClean = queryRecordRepository.findByIsDeletedFalseAndQueryTimeBefore(cutoffTime);

        for (QueryRecord record : recordsToClean) {
            record.setIsBackedUp(true);
            record.setIsDeleted(true);
            queryRecordRepository.save(record);
        }

        log.info("[查询记录清理] 已清理 {} 条记录", recordsToClean.size());
    }
}
