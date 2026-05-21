package com.foodtraceability.traceability.interfaces.rest;

import com.foodtraceability.traceability.application.service.StorageApplicationService;
import com.foodtraceability.traceability.interfaces.dto.RecordStorageRequest;
import com.foodtraceability.traceability.interfaces.dto.StorageResponse;
import com.foodtraceability.util.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v2")
public class StorageController {

    private static final Logger log = LoggerFactory.getLogger(StorageController.class);

    private final StorageApplicationService appService;
    private final SecurityUtils securityUtils;

    public StorageController(StorageApplicationService appService, SecurityUtils securityUtils) {
        this.appService = appService;
        this.securityUtils = securityUtils;
    }

    @PostMapping("/storage")
    public ResponseEntity<?> recordStorage(@RequestBody RecordStorageRequest req) {
        log.info("[v2] 录入仓储 batchId={}", req.getBatchId());
        try {
            Long companyId = securityUtils.getCurrentCompanyId();
            var result = appService.recordStorage(req.toAppRequest(companyId));
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    new StorageResponse(result.id(), result.batchId(), null, null,
                            null, null, result.warehouseLocation()));
        } catch (Exception e) {
            log.error("[v2] 录入仓储失败 - {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
