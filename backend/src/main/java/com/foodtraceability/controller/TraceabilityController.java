package com.foodtraceability.controller;

import com.foodtraceability.dto.TraceInfoDTO;
import com.foodtraceability.dto.VerifyRequest;
import com.foodtraceability.service.TraceabilityService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class TraceabilityController {

    private static final Logger log = LoggerFactory.getLogger(TraceabilityController.class);

    private final TraceabilityService traceabilityService;

    public TraceabilityController(TraceabilityService traceabilityService) {
        this.traceabilityService = traceabilityService;
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verify(@Valid @RequestBody VerifyRequest request) {
        String antiFakeCode = request.getAntiFakeCode();
        String batchNumber = request.getBatchNumber();
        log.info("[防伪验证] POST 请求 - 防伪码: {}, 批次号: {}", maskCode(antiFakeCode), batchNumber != null ? batchNumber : "无");
        long startTime = System.currentTimeMillis();
        
        try {
            if (batchNumber != null && !batchNumber.isBlank()) {
                var result = traceabilityService.verifyAndGetTraceInfoWithBatch(antiFakeCode, batchNumber);
                long duration = System.currentTimeMillis() - startTime;
                
                if (result.isPresent()) {
                    TraceInfoDTO traceInfo = result.get();
                    log.info("[防伪验证] 精确溯源查询成功 - 防伪码: {}, 批次: {}, 产品: {}, 耗时: {}ms", 
                        maskCode(antiFakeCode), batchNumber, traceInfo.getProduct().getName(), duration);
                    return ResponseEntity.ok(Map.of("valid", true, "data", traceInfo));
                } else {
                    log.warn("[防伪验证] 精确溯源查询失败 - 防伪码: {}, 批次: {}, 耗时: {}ms", 
                        maskCode(antiFakeCode), batchNumber, duration);
                    return ResponseEntity.ok(Map.of(
                            "valid", false,
                            "message", "未找到该防伪码和批次对应的产品溯源信息，请核对后重试！"
                    ));
                }
            } else {
                var info = traceabilityService.getPurchaseInfo(antiFakeCode);
                long duration = System.currentTimeMillis() - startTime;
                
                if (info.isPresent()) {
                    PurchaseInfoDTO purchaseInfo = info.get();
                    log.info("[防伪验证] 快速验证成功 - 防伪码: {}, 产品: {}, 耗时: {}ms", 
                        maskCode(antiFakeCode), purchaseInfo.getName(), duration);
                    return ResponseEntity.ok(Map.of(
                        "valid", true, 
                        "productName", purchaseInfo.getName() != null ? purchaseInfo.getName() : "",
                        "specification", purchaseInfo.getSpecification() != null ? purchaseInfo.getSpecification() : "",
                        "message", "产品信息验证通过，如需查看完整溯源信息请提供批次号"
                    ));
                } else {
                    log.warn("[防伪验证] 验证失败 - 防伪码: {}, 耗时: {}ms", maskCode(antiFakeCode), duration);
                    return ResponseEntity.ok(Map.of(
                            "valid", false,
                            "message", "未找到该防伪码对应的产品信息，该产品可能是伪品，请谨慎购买！"
                    ));
                }
            }
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("[防伪验证] 系统错误 - 防伪码: {}, 耗时: {}ms, 错误: {}", 
                maskCode(antiFakeCode), duration, e.getMessage());
            return ResponseEntity.ok(Map.of("valid", false, "message", "系统错误，请稍后重试"));
        }
    }

    @GetMapping("/verify")
    public ResponseEntity<?> verifyByGet(@RequestParam String code) {
        log.info("[防伪验证] GET 请求 - 防伪码: {}", maskCode(code));
        long startTime = System.currentTimeMillis();
        
        try {
            var result = traceabilityService.verifyAndGetTraceInfo(code);
            long duration = System.currentTimeMillis() - startTime;
            
            if (result.isPresent()) {
                TraceInfoDTO traceInfo = result.get();
                log.info("[防伪验证] 验证成功 - 防伪码: {}, 产品: {}, 耗时: {}ms", 
                    maskCode(code), traceInfo.getProduct().getName(), duration);
                return ResponseEntity.ok(Map.of("valid", true, "data", traceInfo));
            } else {
                log.warn("[防伪验证] 验证失败 - 防伪码: {}, 耗时: {}ms", maskCode(code), duration);
                return ResponseEntity.ok(Map.of(
                        "valid", false,
                        "message", "未找到该防伪码对应的产品信息，该产品可能是伪品，请谨慎购买！"
                ));
            }
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("[防伪验证] 系统错误 - 防伪码: {}, 耗时: {}ms, 错误: {}", 
                maskCode(code), duration, e.getMessage());
            return ResponseEntity.ok(Map.of("valid", false, "message", "系统错误，请稍后重试"));
        }
    }

    @PostMapping("/products/list")
    public ResponseEntity<?> listAllProducts() {
        return ResponseEntity.ok(traceabilityService.listAllProducts());
    }

    @PostMapping("/purchase-info")
    public ResponseEntity<?> getPurchaseInfo(@RequestBody Map<String, String> request) {
        String antiFakeCode = request.get("antiFakeCode");
        log.info("[采购信息] 请求 - 防伪码: {}", maskCode(antiFakeCode));
        
        if (antiFakeCode == null || antiFakeCode.isBlank()) {
            log.warn("[采购信息] 参数错误 - 防伪码为空");
            return ResponseEntity.badRequest().body(Map.of("error", "防伪码不能为空"));
        }
        
        try {
            var info = traceabilityService.getPurchaseInfo(antiFakeCode);
            if (info.isPresent()) {
                log.info("[采购信息] 查找成功 - 防伪码: {}", maskCode(antiFakeCode));
                return ResponseEntity.ok(Map.of("valid", true, 
                        "productId", info.get().getProductId(),
                        "name", info.get().getName() != null ? info.get().getName() : "",
                        "specification", info.get().getSpecification() != null ? info.get().getSpecification() : "",
                        "imageUrl", info.get().getImageUrl() != null ? info.get().getImageUrl() : "",
                        "contactPhone", info.get().getContactPhone() != null ? info.get().getContactPhone() : "",
                        "contactEmail", info.get().getContactEmail() != null ? info.get().getContactEmail() : ""));
            } else {
                log.warn("[采购信息] 查找失败 - 防伪码: {}", maskCode(antiFakeCode));
                return ResponseEntity.ok(Map.of(
                        "valid", false,
                        "message", "未找到该产品信息"
                ));
            }
        } catch (Exception e) {
            log.error("[采购信息] 系统错误 - 防伪码: {}, 错误: {}", maskCode(antiFakeCode), e.getMessage());
            return ResponseEntity.ok(Map.of("valid", false, "message", "系统错误，请稍后重试"));
        }
    }

    private String maskCode(String code) {
        if (code == null || code.length() <= 8) {
            return "***";
        }
        return code.substring(0, 4) + "****" + code.substring(code.length() - 4);
    }
}
