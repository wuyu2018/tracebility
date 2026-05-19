package com.foodtraceability.controller;

import com.foodtraceability.dto.BlockchainMonitorSummary;
import com.foodtraceability.service.BlockchainMonitorService;
import com.foodtraceability.service.BlockchainService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/blockchain")
public class BlockchainController {

    private final BlockchainService blockchainService;
    private final BlockchainMonitorService monitorService;

    public BlockchainController(BlockchainService blockchainService, BlockchainMonitorService monitorService) {
        this.blockchainService = blockchainService;
        this.monitorService = monitorService;
    }

    @GetMapping("/verify/material")
    public ResponseEntity<?> verifyMaterialChain() {
        var report = blockchainService.verifyMaterialChain();
        return ResponseEntity.ok(Map.of(
                "chainType", "MATERIAL",
                "intact", report.intact(),
                "blockCount", report.blockResults().size(),
                "blocks", report.blockResults()));
    }

    @GetMapping("/verify/batch")
    public ResponseEntity<?> verifyBatchChain(@RequestParam Long batchId) {
        var report = blockchainService.verifyBatchChain(batchId);
        return ResponseEntity.ok(Map.of(
                "chainType", "BATCH",
                "batchId", batchId,
                "intact", report.intact(),
                "blockCount", report.blockResults().size(),
                "blocks", report.blockResults()));
    }

    @GetMapping("/verify/all")
    public ResponseEntity<?> verifyAllBatchChains() {
        var reports = blockchainService.verifyAllBatchChains();
        long intactCount = reports.values().stream().filter(BlockchainService.IntegrityReport::intact).count();
        return ResponseEntity.ok(Map.of(
                "totalBatches", reports.size(),
                "intactCount", intactCount,
                "brokenCount", reports.size() - intactCount,
                "reports", reports));
    }

    @GetMapping("/public-key")
    public ResponseEntity<?> getPublicKey() {
        return ResponseEntity.ok(Map.of("publicKey", blockchainService.getPublicKeyBase64()));
    }

    @GetMapping("/monitor/summary")
    public ResponseEntity<BlockchainMonitorSummary> getMonitorSummary() {
        return ResponseEntity.ok(monitorService.getSummary());
    }
}
