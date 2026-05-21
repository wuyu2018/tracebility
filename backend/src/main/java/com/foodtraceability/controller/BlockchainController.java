package com.foodtraceability.controller;

import com.foodtraceability.service.BlockchainMonitorService;
import com.foodtraceability.service.BlockchainService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/blockchain")
public class BlockchainController {

    private final BlockchainService blockchainService;
    private final BlockchainMonitorService blockchainMonitorService;

    public BlockchainController(BlockchainService blockchainService,
                                BlockchainMonitorService blockchainMonitorService) {
        this.blockchainService = blockchainService;
        this.blockchainMonitorService = blockchainMonitorService;
    }

    @GetMapping("/public-key")
    public ResponseEntity<?> getPublicKey() {
        return ResponseEntity.ok(Map.of("publicKey", blockchainService.getPublicKeyBase64()));
    }

    @GetMapping("/monitor/summary")
    public ResponseEntity<Map<String, Object>> getMonitorSummary() {
        return ResponseEntity.ok(blockchainMonitorService.getSummary());
    }

}
