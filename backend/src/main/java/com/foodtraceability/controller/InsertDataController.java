package com.foodtraceability.controller;

import com.foodtraceability.dto.InsertDataDTO.*;
import com.foodtraceability.entity.*;
import com.foodtraceability.service.*;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/insert")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class InsertDataController {

    private static final Logger log = LoggerFactory.getLogger(InsertDataController.class);

    private final ProductService productService;
    private final MaterialPurchaseService materialPurchaseService;
    private final InspectionService inspectionService;
    private final StorageService storageService;
    private final TransportSaleService transportSaleService;

    public InsertDataController(ProductService productService,
                               MaterialPurchaseService materialPurchaseService,
                               InspectionService inspectionService,
                               StorageService storageService,
                               TransportSaleService transportSaleService) {
        this.productService = productService;
        this.materialPurchaseService = materialPurchaseService;
        this.inspectionService = inspectionService;
        this.storageService = storageService;
        this.transportSaleService = transportSaleService;
    }

    @PostMapping("/products")
    public ResponseEntity<?> createProduct(@Valid @RequestBody ProductDTO dto) {
        log.debug("[数据录入] 创建产品 - 名称: {}, 批次: {}", dto.getName(), dto.getBatchNumber());
        long startTime = System.currentTimeMillis();
        
        try {
            Product created = productService.createProduct(dto);
            long duration = System.currentTimeMillis() - startTime;
            log.info("[数据录入] 产品创建成功 - ID: {}, 名称: {}, 耗时: {}ms", 
                created.getId(), created.getName(), duration);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            long duration = System.currentTimeMillis() - startTime;
            log.warn("[数据录入] 产品创建失败 - 名称: {}, 错误: {}, 耗时: {}ms", 
                dto.getName(), e.getMessage(), duration);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("[数据录入] 产品创建失败 - 名称: {}, 错误: {}, 耗时: {}ms", 
                dto.getName(), e.getMessage(), duration);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "产品创建失败：" + e.getMessage()));
        }
    }

    @PostMapping("/products/{id}/generate-qrcode")
    public ResponseEntity<Product> generateQrCode(@PathVariable Long id) {
        log.debug("[二维码生成] 产品ID: {}", id);
        
        try {
            Product product = productService.generateQrCode(id);
            log.debug("[二维码生成] 生成成功 - 产品ID: {}", id);
            return ResponseEntity.ok(product);
        } catch (Exception e) {
            log.error("[二维码生成] 生成失败 - 产品ID: {}, 错误: {}", id, e.getMessage());
            throw e;
        }
    }

    @PostMapping("/products/batch-generate-qrcode")
    public ResponseEntity<?> batchGenerateQrCodes(@RequestBody Map<String, List<Long>> request) {
        List<Long> productIds = request.get("productIds");
        if (productIds == null || productIds.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "产品ID列表不能为空"));
        }
        log.debug("[批量二维码生成] 产品数量: {}", productIds.size());
        
        try {
            List<Product> products = productService.batchGenerateQrCodes(productIds);
            log.info("[批量二维码生成] 成功 - 数量: {}", products.size());
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            log.error("[批量二维码生成] 失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/products/batch-delete")
    public ResponseEntity<?> batchDeleteProducts(@RequestBody Map<String, List<Long>> request) {
        List<Long> productIds = request.get("productIds");
        if (productIds == null || productIds.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "产品ID列表不能为空"));
        }
        log.debug("[批量删除产品] 产品数量: {}", productIds.size());
        
        try {
            productService.batchDeleteProducts(productIds);
            log.info("[批量删除产品] 成功 - 数量: {}", productIds.size());
            return ResponseEntity.ok(Map.of("message", "删除成功", "count", productIds.size()));
        } catch (Exception e) {
            log.error("[批量删除产品] 失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/products/list")
    public ResponseEntity<List<Product>> listAllProducts() {
        return ResponseEntity.ok(productService.listAllProducts());
    }

    @PostMapping("/material-purchase")
    public ResponseEntity<MaterialPurchase> createMaterialPurchase(@Valid @RequestBody MaterialPurchaseDTO dto) {
        log.debug("[原料采购] 录入采购记录 - 产品名称: {}, 原料名称: {}", dto.getProductName(), dto.getMaterialName());
        
        try {
            MaterialPurchase created = materialPurchaseService.createMaterialPurchase(dto);
            log.info("[原料采购] 录入成功 - ID: {}", created.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            log.error("[原料采购] 录入失败 - 产品名称: {}, 错误: {}", dto.getProductName(), e.getMessage());
            throw e;
        }
    }

    @PostMapping("/inspection")
    public ResponseEntity<Inspection> createInspection(@Valid @RequestBody InspectionDTO dto) {
        log.debug("[出厂检验] 录入检验记录 - 产品名称: {}, 批次: {}", dto.getProductName(), dto.getBatchNumber());
        
        try {
            Inspection created = inspectionService.createInspection(dto);
            log.info("[出厂检验] 录入成功 - ID: {}", created.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            log.error("[出厂检验] 录入失败 - 产品名称: {}, 错误: {}", dto.getProductName(), e.getMessage());
            throw e;
        }
    }

    @PostMapping("/storage")
    public ResponseEntity<Storage> createStorage(@Valid @RequestBody StorageDTO dto) {
        log.debug("[贮存记录] 录入贮存记录 - 产品名称: {}", dto.getProductName());
        
        try {
            Storage created = storageService.createStorage(dto);
            log.info("[贮存记录] 录入成功 - ID: {}", created.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            log.error("[贮存记录] 录入失败 - 产品名称: {}, 错误: {}", dto.getProductName(), e.getMessage());
            throw e;
        }
    }

    @PostMapping("/transport-sale")
    public ResponseEntity<TransportSale> createTransportSale(@Valid @RequestBody TransportSaleDTO dto) {
        log.debug("[储运销售] 录入储运记录 - 产品名称: {}", dto.getProductName());
        
        try {
            TransportSale created = transportSaleService.createTransportSale(dto);
            log.info("[储运销售] 录入成功 - ID: {}", created.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            log.error("[储运销售] 录入失败 - 产品名称: {}, 错误: {}", dto.getProductName(), e.getMessage());
            throw e;
        }
    }
}
