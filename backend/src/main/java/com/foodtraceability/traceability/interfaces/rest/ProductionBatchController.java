package com.foodtraceability.traceability.interfaces.rest;

import com.foodtraceability.traceability.application.service.ProductionBatchApplicationService;
import com.foodtraceability.traceability.interfaces.dto.BatchResponse;
import com.foodtraceability.traceability.interfaces.dto.CreateBatchRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v2")
public class ProductionBatchController {

    private static final Logger log = LoggerFactory.getLogger(ProductionBatchController.class);

    private final ProductionBatchApplicationService appService;

    public ProductionBatchController(ProductionBatchApplicationService appService) {
        this.appService = appService;
    }

    @PostMapping("/batches")
    public ResponseEntity<?> createBatch(@RequestBody CreateBatchRequest req) {
        log.info("[v2] 创建批次 productId={}", req.getProductId());
        try {
            var result = appService.createBatch(req.toAppRequest());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new BatchResponse(result.id(), result.batchNumber(), result.productName()));
        } catch (Exception e) {
            log.error("[v2] 创建批次失败 - {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
