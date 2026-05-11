package com.foodtraceability.traceability.interfaces.rest;

import com.foodtraceability.traceability.application.service.MaterialPurchaseApplicationService;
import com.foodtraceability.traceability.interfaces.dto.CreateMaterialPurchaseRequest;
import com.foodtraceability.traceability.interfaces.dto.MaterialPurchaseResponse;
import com.foodtraceability.traceability.interfaces.dto.UpdateMaterialPurchaseRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v2/material-purchases")
public class MaterialPurchaseController {

    private static final Logger log = LoggerFactory.getLogger(MaterialPurchaseController.class);

    private final MaterialPurchaseApplicationService appService;

    public MaterialPurchaseController(MaterialPurchaseApplicationService appService) {
        this.appService = appService;
    }

    @PostMapping
    public ResponseEntity<?> createMaterialPurchase(@RequestBody CreateMaterialPurchaseRequest req) {
        log.info("[V2] 创建采购单: materialId={}", req.getMaterialId());
        try {
            var result = appService.createMaterialPurchase(req.toAppRequest());
            return ResponseEntity.status(HttpStatus.CREATED).body(MaterialPurchaseResponse.from(result));
        } catch (Exception e) {
            log.error("[V2] 创建采购单失败 - {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> listMaterialPurchases(
            @RequestParam(required = false) Long materialId) {
        try {
            var results = appService.listMaterialPurchases(materialId);
            return ResponseEntity.ok(results.stream().map(MaterialPurchaseResponse::from).toList());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getMaterialPurchase(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(MaterialPurchaseResponse.from(appService.getMaterialPurchase(id)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateMaterialPurchase(@PathVariable Long id,
                                                     @RequestBody UpdateMaterialPurchaseRequest req) {
        log.info("[V2] 更新采购单: id={}", id);
        try {
            var result = appService.updateMaterialPurchase(id, req.toAppRequest());
            return ResponseEntity.ok(MaterialPurchaseResponse.from(result));
        } catch (Exception e) {
            log.error("[V2] 更新采购单失败 - {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMaterialPurchase(@PathVariable Long id) {
        log.info("[V2] 删除采购单: id={}", id);
        try {
            appService.deleteMaterialPurchase(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("[V2] 删除采购单失败 - {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
