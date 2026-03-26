package com.foodtraceability.controller;

import com.foodtraceability.dto.*;
import com.foodtraceability.entity.*;
import com.foodtraceability.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class DataManagementController {

    private static final Logger log = LoggerFactory.getLogger(DataManagementController.class);

    private final ProductService productService;
    private final MaterialPurchaseService materialPurchaseService;
    private final ProductionBatchService batchService;
    private final SecurityCodeService securityCodeService;

    public DataManagementController(ProductService productService,
                                   MaterialPurchaseService materialPurchaseService,
                                   ProductionBatchService batchService,
                                   SecurityCodeService securityCodeService) {
        this.productService = productService;
        this.materialPurchaseService = materialPurchaseService;
        this.batchService = batchService;
        this.securityCodeService = securityCodeService;
    }

    @PostMapping("/products")
    public ResponseEntity<?> createProduct(@RequestBody ProductDTO dto) {
        log.info("[产品管理] 创建产品 - 名称: {}", dto.getName());
        try {
            Product created = productService.createProduct(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            log.error("[产品管理] 创建产品失败 - {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Long id, @RequestBody ProductDTO dto) {
        log.info("[产品管理] 更新产品 - ID: {}", id);
        try {
            Product updated = productService.updateProduct(id, dto);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("[产品管理] 更新产品失败 - ID: {}, {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        log.info("[产品管理] 删除产品 - ID: {}", id);
        try {
            productService.deleteProduct(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "删除成功"));
        } catch (Exception e) {
            log.error("[产品管理] 删除产品失败 - ID: {}, {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/products")
    public ResponseEntity<?> listProducts(@RequestParam(required = false) String keyword) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            return ResponseEntity.ok(productService.searchProducts(keyword.trim()));
        }
        return ResponseEntity.ok(productService.listAllProducts());
    }

    @GetMapping("/products/select")
    public ResponseEntity<?> selectProducts(@RequestParam(required = false) String keyword,
                                             @RequestParam(required = false, defaultValue = "consumer") String role) {
        log.info("[产品选择] 查询产品 - 关键词: {}, 角色: {}", keyword, role);
        try {
            if (keyword != null && !keyword.trim().isEmpty()) {
                return ResponseEntity.ok(productService.searchProducts(keyword.trim()));
            }
            return ResponseEntity.ok(productService.listAllProducts());
        } catch (Exception e) {
            log.error("[产品选择] 查询失败 - {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/insert/products/list")
    public ResponseEntity<?> getProductsForInsert() {
        log.info("[数据导入] 获取产品列表");
        try {
            return ResponseEntity.ok(productService.listAllProducts());
        } catch (Exception e) {
            log.error("[数据导入] 获取产品列表失败 - {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<?> getProduct(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(productService.getProductById(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/materials")
    public ResponseEntity<?> createMaterial(@RequestBody MaterialPurchaseDTO dto) {
        log.info("[原材料管理] 创建原材料 - 名称: {}, 批次: {}", dto.getMaterialName(), dto.getBatchNumber());
        try {
            MaterialPurchase created = materialPurchaseService.createMaterialPurchase(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            log.error("[原材料管理] 创建原材料失败 - {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/materials/{id}")
    public ResponseEntity<?> updateMaterial(@PathVariable Long id, @RequestBody MaterialPurchaseDTO dto) {
        log.info("[原材料管理] 更新原材料 - ID: {}", id);
        try {
            MaterialPurchase updated = materialPurchaseService.updateMaterialPurchase(id, dto);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("[原材料管理] 更新原材料失败 - {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/materials/{id}")
    public ResponseEntity<?> deleteMaterial(@PathVariable Long id) {
        log.info("[原材料管理] 删除原材料 - ID: {}", id);
        try {
            materialPurchaseService.deleteMaterialPurchase(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "删除成功"));
        } catch (Exception e) {
            log.error("[原材料管理] 删除原材料失败 - {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/materials")
    public ResponseEntity<?> listMaterials(@RequestParam(required = false) Long productId) {
        if (productId != null) {
            return ResponseEntity.ok(materialPurchaseService.getMaterialPurchasesByProductId(productId));
        }
        return ResponseEntity.ok(materialPurchaseService.listAllMaterialPurchases());
    }

    @PostMapping("/batches")
    public ResponseEntity<?> createBatch(@RequestBody ProductionBatchDTO dto) {
        log.info("[生产批次管理] 创建批次");
        try {
            if (dto.getMaterialIds() == null || dto.getMaterialIds().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "至少选择一个原料批次"));
            }
            ProductionBatch created = batchService.createBatch(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            log.error("[生产批次管理] 创建批次失败 - {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/batches")
    public ResponseEntity<?> listBatches(@RequestParam(required = false) Long productId) {
        if (productId != null) {
            return ResponseEntity.ok(batchService.getBatchesByProductId(productId));
        }
        return ResponseEntity.ok(batchService.listAllBatches());
    }

    @GetMapping("/batches/{id}")
    public ResponseEntity<?> getBatch(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(batchService.getBatchById(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/batches/by-number/{batchNumber}")
    public ResponseEntity<?> getBatchByNumber(@PathVariable String batchNumber) {
        try {
            return ResponseEntity.ok(batchService.getBatchByBatchNumber(batchNumber));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/batches/{id}/inspection")
    public ResponseEntity<?> addInspection(@PathVariable Long id, @RequestBody InspectionDTO dto) {
        log.info("[生产批次管理] 添加检测报告 - 批次ID: {}", id);
        try {
            InspectionDTO created = batchService.addInspection(id, dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            log.error("[生产批次管理] 添加检测报告失败 - {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/batches/{id}/storage")
    public ResponseEntity<?> addStorage(@PathVariable Long id, @RequestBody StorageDTO dto) {
        log.info("[生产批次管理] 添加仓储信息 - 批次ID: {}", id);
        try {
            StorageDTO created = batchService.addStorage(id, dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            log.error("[生产批次管理] 添加仓储信息失败 - {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/batches/{id}/transport-sale")
    public ResponseEntity<?> addTransportSale(@PathVariable Long id, @RequestBody TransportSaleDTO dto) {
        log.info("[生产批次管理] 添加运输销售信息 - 批次ID: {}", id);
        try {
            TransportSaleDTO created = batchService.addTransportSale(id, dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            log.error("[生产批次管理] 添加运输销售信息失败 - {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/batches/{id}/security-codes")
    public ResponseEntity<?> generateSecurityCodes(@PathVariable Long id, @RequestBody GenerateSecurityCodeRequest request) {
        log.info("[防伪码管理] 生成防伪码 - 批次ID: {}, 数量: {}", id, request.getQuantity());
        try {
            SecurityCodeGenerateResponse response = securityCodeService.generateCodes(id, request.getQuantity());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("[防伪码管理] 生成防伪码失败 - {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/batches/{id}/security-codes")
    public ResponseEntity<?> listSecurityCodes(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(securityCodeService.getCodesByBatchId(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/security-codes/export/{batchId}")
    public ResponseEntity<?> exportSecurityCodes(@PathVariable Long batchId) {
        try {
            return ResponseEntity.ok(securityCodeService.exportCodes(batchId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/insert/products/{productId}/generate-qrcode")
    public ResponseEntity<?> generateQrCodeForProduct(@PathVariable Long productId) {
        log.info("[产品二维码] 为产品生成二维码 - 产品ID: {}", productId);
        try {
            Product product = productService.getProductById(productId);
            if (product == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "产品不存在"));
            }
            ProductionBatch batch = batchService.createQuickBatchForProduct(productId);
            SecurityCodeGenerateResponse response = securityCodeService.generateCodes(batch.getId(), 100);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("[产品二维码] 生成失败 - 产品ID: {}, 错误: {}", productId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/insert/products/batch-generate-qrcode")
    public ResponseEntity<?> batchGenerateQrCodes(@RequestBody List<Long> productIds) {
        log.info("[产品二维码] 批量生成二维码 - 产品数量: {}", productIds.size());
        try {
            int successCount = 0;
            int failCount = 0;
            for (Long productId : productIds) {
                try {
                    Product product = productService.getProductById(productId);
                    if (product != null) {
                        ProductionBatch batch = batchService.createQuickBatchForProduct(productId);
                        securityCodeService.generateCodes(batch.getId(), 100);
                        successCount++;
                    } else {
                        failCount++;
                    }
                } catch (Exception e) {
                    failCount++;
                    log.warn("[产品二维码] 批量生成失败 - 产品ID: {}", productId);
                }
            }
            return ResponseEntity.ok(Map.of(
                "success", true,
                "successCount", successCount,
                "failCount", failCount,
                "message", String.format("批量生成完成，成功: %d，失败: %d", successCount, failCount)
            ));
        } catch (Exception e) {
            log.error("[产品二维码] 批量生成失败 - 错误: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/insert/products/batch-delete")
    public ResponseEntity<?> batchDeleteProducts(@RequestBody List<Long> productIds) {
        log.info("[产品管理] 批量删除产品 - 产品数量: {}", productIds.size());
        try {
            int successCount = 0;
            int failCount = 0;
            for (Long productId : productIds) {
                try {
                    productService.deleteProduct(productId);
                    successCount++;
                } catch (Exception e) {
                    failCount++;
                    log.warn("[产品管理] 批量删除失败 - 产品ID: {}", productId);
                }
            }
            return ResponseEntity.ok(Map.of(
                "success", true,
                "successCount", successCount,
                "failCount", failCount,
                "message", String.format("批量删除完成，成功: %d，失败: %d", successCount, failCount)
            ));
        } catch (Exception e) {
            log.error("[产品管理] 批量删除失败 - 错误: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}