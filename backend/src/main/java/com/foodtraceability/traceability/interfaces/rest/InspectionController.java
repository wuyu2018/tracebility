package com.foodtraceability.traceability.interfaces.rest;

import com.foodtraceability.traceability.application.service.InspectionApplicationService;
import com.foodtraceability.traceability.interfaces.dto.CompleteInspectionRequest;
import com.foodtraceability.traceability.interfaces.dto.CompleteInspectionResponse;
import com.foodtraceability.util.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v2")
public class InspectionController {

    private static final Logger log = LoggerFactory.getLogger(InspectionController.class);

    private final InspectionApplicationService appService;
    private final SecurityUtils securityUtils;

    public InspectionController(InspectionApplicationService appService, SecurityUtils securityUtils) {
        this.appService = appService;
        this.securityUtils = securityUtils;
    }

    @PostMapping("/inspections")
    public ResponseEntity<?> completeInspection(@RequestBody CompleteInspectionRequest req) {
        log.info("[v2] 检验完成 batchId={}, qualified={}", req.getBatchId(), req.getQualified());
        try {
            Long companyId = securityUtils.getCurrentCompanyId();
            var result = appService.completeInspection(req.toAppRequest(companyId));
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    new CompleteInspectionResponse(result.id(), result.batchId(), result.resultStatus()));
        } catch (Exception e) {
            log.error("[v2] 检验完成失败 - {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
