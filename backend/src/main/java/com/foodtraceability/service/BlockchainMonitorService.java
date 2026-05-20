package com.foodtraceability.service;

import com.foodtraceability.dto.BlockchainMonitorSummary;
import com.foodtraceability.repository.BlockchainAnchorRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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

        List<BlockchainMonitorSummary.BrokenBlockDetail> allBroken = new ArrayList<>();
        List<Long> brokenBatchIds = new ArrayList<>();

        // 原材料链异常区块
        for (var result : materialReport.blockResults()) {
            if (!result.passed()) {
                allBroken.add(toBrokenDetail(null, result));
            }
        }

        // 批次链异常区块
        for (var entry : batchReports.entrySet()) {
            Long batchId = entry.getKey();
            BlockchainService.IntegrityReport report = entry.getValue();
            if (!report.intact()) {
                brokenBatchIds.add(batchId);
            }
            for (var result : report.blockResults()) {
                if (!result.passed()) {
                    allBroken.add(toBrokenDetail(batchId, result));
                }
            }
        }

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
                        totalBatches, intactCount, brokenCount, totalBlocks, batchAnchorDate, brokenBatchIds
                ),
                allBroken,
                LocalDateTime.now()
        );
    }

    private BlockchainMonitorSummary.BrokenBlockDetail toBrokenDetail(Long batchId, BlockchainService.BlockCheckResult r) {
        return new BlockchainMonitorSummary.BrokenBlockDetail(
                r.blockId(), batchId, r.entityType(), r.entityId(), r.action(), r.errors()
        );
    }
}
