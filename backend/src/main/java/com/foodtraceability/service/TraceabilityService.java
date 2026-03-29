package com.foodtraceability.service;

import com.foodtraceability.dto.TraceInfoDTO;

import java.util.Optional;

public interface TraceabilityService {
    Optional<TraceInfoDTO> getTraceInfoByCode(String code);
    Optional<TraceInfoDTO> getTraceInfoByBatchNumber(String batchNumber);
    Optional<TraceInfoDTO> getTraceInfoByCodeForAdmin(String code);
}