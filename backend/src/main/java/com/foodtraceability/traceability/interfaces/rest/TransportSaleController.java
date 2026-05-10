package com.foodtraceability.traceability.interfaces.rest;

import com.foodtraceability.traceability.application.service.TransportSaleApplicationService;
import com.foodtraceability.traceability.interfaces.dto.RecordTransportSaleRequest;
import com.foodtraceability.traceability.interfaces.dto.TransportSaleResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v2")
public class TransportSaleController {

    private static final Logger log = LoggerFactory.getLogger(TransportSaleController.class);

    private final TransportSaleApplicationService appService;

    public TransportSaleController(TransportSaleApplicationService appService) {
        this.appService = appService;
    }

    @PostMapping("/transport-sales")
    public ResponseEntity<?> recordTransportSale(@RequestBody RecordTransportSaleRequest req) {
        log.info("[v2] 录入运输销售 batchId={}", req.getBatchId());
        try {
            var result = appService.recordTransportSale(req.toAppRequest());
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    new TransportSaleResponse(result.id(), result.batchId(),
                            result.transportCompany(), result.salesRegion()));
        } catch (Exception e) {
            log.error("[v2] 录入运输销售失败 - {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
