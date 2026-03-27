package com.foodtraceability.controller;

import com.foodtraceability.dto.VerifyRequest;
import com.foodtraceability.service.TraceabilityService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class TraceabilityController {

    private final TraceabilityService traceabilityService;

    public TraceabilityController(TraceabilityService traceabilityService) {
        this.traceabilityService = traceabilityService;
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verify(@Valid @RequestBody VerifyRequest request) {
        return traceabilityService.verifyAndGetTraceInfo(request.getAntiFakeCode())
                .map(info -> ResponseEntity.ok(Map.of("valid", true, "data", info)))
                .orElse(ResponseEntity.ok(Map.of(
                        "valid", false,
                        "message", "未找到该防伪码对应的产品信息，该产品可能是伪品，请谨慎购买！"
                )));
    }

    @GetMapping("/verify")
    public ResponseEntity<?> verifyByGet(@RequestParam String code) {
        return traceabilityService.verifyAndGetTraceInfo(code)
                .map(info -> ResponseEntity.ok(Map.of("valid", true, "data", info)))
                .orElse(ResponseEntity.ok(Map.of(
                        "valid", false,
                        "message", "未找到该防伪码对应的产品信息，该产品可能是伪品，请谨慎购买！"
                )));
    }

    @PostMapping("/products/list")
    public ResponseEntity<?> listAllProducts() {
        return ResponseEntity.ok(traceabilityService.listAllProducts());
    }

    @PostMapping("/purchase-info")
    public ResponseEntity<?> getPurchaseInfo(@RequestBody Map<String, String> request) {
        String antiFakeCode = request.get("antiFakeCode");
        if (antiFakeCode == null || antiFakeCode.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "防伪码不能为空"));
        }
        return traceabilityService.getPurchaseInfo(antiFakeCode)
                .map(info -> ResponseEntity.ok(Map.of("valid", true, "productId", info.getProductId(),
                        "name", info.getName() != null ? info.getName() : "",
                        "specification", info.getSpecification() != null ? info.getSpecification() : "",
                        "imageUrl", info.getImageUrl() != null ? info.getImageUrl() : "",
                        "contactPhone", info.getContactPhone() != null ? info.getContactPhone() : "",
                        "contactEmail", info.getContactEmail() != null ? info.getContactEmail() : "")))
                .orElse(ResponseEntity.ok(Map.of(
                        "valid", false,
                        "message", "未找到该产品信息"
                )));
    }

    @GetMapping("/batches")
    public ResponseEntity<?> listAllBatches() {
        return ResponseEntity.ok(traceabilityService.listAllBatches());
    }

    @GetMapping("/materials")
    public ResponseEntity<?> listAllMaterials() {
        return ResponseEntity.ok(traceabilityService.listAllMaterials());
    }

    @GetMapping("/product-detail")
    public ResponseEntity<?> getProductDetail(@RequestParam Long productId) {
        return traceabilityService.getProductDetail(productId)
                .map(product -> ResponseEntity.ok(product))
                .orElse(ResponseEntity.ok(Map.of("error", "产品不存在")));
    }
}
