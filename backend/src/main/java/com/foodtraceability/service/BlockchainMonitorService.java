package com.foodtraceability.service;

import com.foodtraceability.dto.BlockchainMonitorSummary;
import com.foodtraceability.repository.BlockchainAnchorRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Service
public class BlockchainMonitorService {

    private final BlockchainService blockchainService;
    private final BlockchainAnchorRepository anchorRepo;

    public BlockchainMonitorService(BlockchainService blockchainService, BlockchainAnchorRepository anchorRepo) {
        this.blockchainService = blockchainService;
        this.anchorRepo = anchorRepo;
    }

    public BlockchainMonitorSummary getSummary() {
        BlockchainService.IntegrityReport materialReport = blockchainService.verifyMaterialChain();

        Map<Long, BlockchainService.IntegrityReport> batchReports = blockchainService.verifyAllBatchChains();
        long totalBatches = batchReports.size();
        long intactCount = batchReports.values().stream().filter(BlockchainService.IntegrityReport::intact).count();
        long brokenCount = totalBatches - intactCount;
        long totalBlocks = batchReports.values().stream()
                .mapToLong(r -> r.blockResults().size())
                .sum();

        LocalDate materialAnchorDate = anchorRepo.findLatestAnchorDateByChainType("MATERIAL").orElse(null);
        LocalDate batchAnchorDate = anchorRepo.findLatestAnchorDateByChainType("BATCH").orElse(null);

        boolean overallHealthy = materialReport.intact() && brokenCount == 0;

        return new BlockchainMonitorSummary(
                overallHealthy,
                new BlockchainMonitorSummary.MaterialChainInfo(
                        materialReport.intact(),
                        materialReport.blockResults().size(),
                        materialAnchorDate
                ),
                new BlockchainMonitorSummary.BatchChainSummary(
                        totalBatches, intactCount, brokenCount, totalBlocks, batchAnchorDate
                ),
                LocalDateTime.now()
        );
    }
}
