package com.foodtraceability.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record BlockchainMonitorSummary(
    boolean overallHealthy,
    MaterialChainInfo materialChain,
    BatchChainSummary batchChains,
    LocalDateTime lastUpdated
) {
    public record MaterialChainInfo(boolean intact, long blockCount, LocalDate lastAnchorDate) {}
    public record BatchChainSummary(long totalBatches, long intactCount, long brokenCount, long totalBlockCount, LocalDate lastAnchorDate) {}
}
