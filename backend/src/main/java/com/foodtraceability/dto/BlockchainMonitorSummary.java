package com.foodtraceability.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record BlockchainMonitorSummary(
    boolean overallHealthy,
    MaterialChainInfo materialChain,
    BatchChainSummary batchChains,
    List<BrokenBlockDetail> brokenBlocks,
    LocalDateTime lastUpdated
) {
    public record MaterialChainInfo(boolean intact, long blockCount, LocalDate lastAnchorDate) {}

    public record BatchChainSummary(
        long totalBatches, long intactCount, long brokenCount,
        long totalBlockCount, LocalDate lastAnchorDate,
        List<Long> brokenBatchIds
    ) {}

    public record BrokenBlockDetail(
        Long blockId, Long batchId,
        String entityType, Long entityId, String action,
        List<String> errors
    ) {}
}
