package com.foodtraceability.service;

import com.foodtraceability.dto.QueryRecordDTO;

public interface QueryRecordService {

    QueryRecordDTO recordQuery(String antiFakeCode, String batchNumber, String queryType);

    boolean hasBeenQueriedBefore(String antiFakeCode);

    void cleanupOldRecords();
}
