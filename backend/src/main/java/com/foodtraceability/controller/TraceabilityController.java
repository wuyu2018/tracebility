package com.foodtraceability.controller;

import com.foodtraceability.dto.TraceInfoDTO;
import com.foodtraceability.service.TraceabilityService;
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

    @GetMapping("/verify")
    public ResponseEntity<?> verify(@RequestParam String code) {
        log.info("[防伪验证] GET 请求 - 防伪码: {}", maskCode(code));
        long startTime = System.currentTimeMillis();

        try {
            var result = traceabilityService.getTraceInfoByCode(code);
            long duration = System.currentTimeMillis() - startTime;

            if (result.isPresent()) {
                TraceInfoDTO traceInfo = result.get();
                log.info("[防伪验证] 查询成功 - 防伪码: {}, 产品: {}, 耗时: {}ms",
                        maskCode(code), traceInfo.getProduct().getName(), duration);
                return ResponseEntity.ok(Map.of("valid", true, "data", traceInfo));
            } else {
                log.warn("[防伪验证] 验证失败 - 防伪码: {}, 耗时: {}ms", maskCode(code), duration);
                return ResponseEntity.ok(Map.of("valid", false, "message", "未找到该防伪码对应的产品信息，该产品可能是伪品，请谨慎购买！"));
            }
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("[防伪验证] 系统错误 - 防伪码: {}, 耗时: {}ms, 错误: {}",
                    maskCode(code), duration, e.getMessage());
            return ResponseEntity.ok(Map.of("valid", false, "message", "系统错误，请稍后重试"));
        }
    }

    @GetMapping("/trace/batch/{batchNumber}")
    public ResponseEntity<?> traceByBatchNumber(@PathVariable String batchNumber) {
        log.info("[批次追溯] GET 请求 - 批次号: {}", batchNumber);
        long startTime = System.currentTimeMillis();

        try {
            var result = traceabilityService.getTraceInfoByBatchNumber(batchNumber);
            long duration = System.currentTimeMillis() - startTime;

            if (result.isPresent()) {
                TraceInfoDTO traceInfo = result.get();
                log.info("[批次追溯] 查询成功 - 批次号: {}, 产品: {}, 耗时: {}ms",
                        batchNumber, traceInfo.getProduct().getName(), duration);
                return ResponseEntity.ok(Map.of("valid", true, "data", traceInfo));
            } else {
                log.warn("[批次追溯] 查询失败 - 批次号: {}, 耗时: {}ms", batchNumber, duration);
                return ResponseEntity.ok(Map.of("valid", false, "message", "未找到该批次对应的产品信息"));
            }
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("[批次追溯] 系统错误 - 批次号: {}, 耗时: {}ms, 错误: {}",
                    batchNumber, duration, e.getMessage());
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