package com.foodtraceability.controller;

import com.foodtraceability.aop.OperationLog;
import com.foodtraceability.dto.*;
import com.foodtraceability.entity.*;
import com.foodtraceability.repository.*;
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
public class DataManagementController {

    private static final Logger log = LoggerFactory.getLogger(DataManagementController.class);

    private final ProductService productService;
    private final MaterialService materialService;
    private final MaterialPurchaseService materialPurchaseService;
    private final ProductMaterialRelationService productMaterialRelationService;
    private final ProductionBatchService batchService;
    private final SecurityCodeService securityCodeService;
    private final InspectionRepository inspectionRepository;
    private final StorageRepository storageRepository;
    private final TransportSaleRepository transportSaleRepository;

    public DataManagementController(ProductService productService,
                                   MaterialService materialService,
                                   MaterialPurchaseService materialPurchaseService,
                                   ProductMaterialRelationService productMaterialRelationService,
                                   ProductionBatchService batchService,
                                   SecurityCodeService securityCodeService,
                                   InspectionRepository inspectionRepository,
                                   StorageRepository storageRepository,
                                   TransportSaleRepository transportSaleRepository) {
        this.productService = productService;
        this.materialService = materialService;
        this.materialPurchaseService = materialPurchaseService;
        this.productMaterialRelationService = productMaterialRelationService;
        this.batchService = batchService;
        this.securityCodeService = securityCodeService;
        this.inspectionRepository = inspectionRepository;
        this.storageRepository = storageRepository;
        this.transportSaleRepository = transportSaleRepository;
    }

    @PostMapping("/products")
    @OperationLog(entityType = "PRODUCT", action = "CREATE")
    public ResponseEntity<?> createProduct(@RequestBody ProductDTO dto) {
        log.info("[产品管理] 创建产品 - 名称: {}", dto.getName());
        try {
            Product created = productService.createProduct(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            log.error("[产品管理] 创建产品失败 - {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", "操作失败"));
        }
    }

    @PutMapping("/products/{id}")
    @OperationLog(entityType = "PRODUCT", action = "UPDATE")
    public ResponseEntity<?> updateProduct(@PathVariable Long id, @RequestBody ProductDTO dto) {
        log.info("[产品管理] 更新产品 - ID: {}", id);
        try {
            Product updated = productService.updateProduct(id, dto);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("[产品管理] 更新产品失败 - ID: {}, {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", "操作失败"));
        }
    }

    @DeleteMapping("/products/{id}")
    @OperationLog(entityType = "PRODUCT", action = "DELETE")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        log.info("[产品管理] 删除产品 - ID: {}", id);
        try {
            productService.deleteProduct(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "删除成功"));
        } catch (Exception e) {
            log.error("[产品管理] 删除产品失败 - ID: {}, {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", "操作失败"));
        }
    }

    @DeleteMapping("/products/{id}/hard")
    @OperationLog(entityType = "PRODUCT", action = "HARD_DELETE")
    public ResponseEntity<?> hardDeleteProduct(@PathVariable Long id) {
        log.info("[产品管理] 物理删除产品 - ID: {}", id);
        try {
            productService.hardDeleteProduct(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "物理删除成功"));
        } catch (Exception e) {
            log.error("[产品管理] 物理删除产品失败 - ID: {}, {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", "操作失败"));
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
            return ResponseEntity.badRequest().body(Map.of("error", "操作失败"));
        }
    }

    @PostMapping("/insert/products/list")
    public ResponseEntity<?> getProductsForInsert() {
        log.info("[数据导入] 获取产品列表");
        try {
            return ResponseEntity.ok(productService.listAllProducts());
        } catch (Exception e) {
            log.error("[数据导入] 获取产品列表失败 - {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", "操作失败"));
        }
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<?> getProduct(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(productService.getProductById(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "操作失败"));
        }
    }

    // ============ 原料品种 (Material) ============

    @GetMapping("/material-varieties")
    public ResponseEntity<?> listMaterialVarieties(@RequestParam(required = false, defaultValue = "true") Boolean activeOnly) {
        try {
            if (Boolean.TRUE.equals(activeOnly)) {
                return ResponseEntity.ok(materialService.listAllActiveMaterials());
            }
            return ResponseEntity.ok(materialService.listAllMaterials());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "操作失败"));
        }
    }

    @GetMapping("/material-varieties/{id}")
    public ResponseEntity<?> getMaterialVariety(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(materialService.getMaterialById(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "操作失败"));
        }
    }

    // ============ 产品-原料可见性 (ProductMaterialRelation) ============

    @PostMapping("/product-materials")
    @OperationLog(entityType = "PRODUCT", action = "UPDATE")
    public ResponseEntity<?> bindMaterialToProduct(@RequestBody Map<String, Long> body) {
        try {
            Long productId = body.get("productId");
            Long materialId = body.get("materialId");
            var relation = productMaterialRelationService.bindMaterialToProduct(productId, materialId);
            return ResponseEntity.status(HttpStatus.CREATED).body(relation);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "操作失败"));
        }
    }

    @GetMapping("/product-materials")
    public ResponseEntity<?> getProductMaterialRelations(@RequestParam Long productId) {
        try {
            return ResponseEntity.ok(productMaterialRelationService.getRelationsByProductId(productId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "操作失败"));
        }
    }

    @DeleteMapping("/product-materials")
    public ResponseEntity<?> unbindMaterialFromProduct(@RequestParam Long productId, @RequestParam Long materialId) {
        try {
            productMaterialRelationService.unbindMaterialFromProduct(productId, materialId);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "操作失败"));
        }
    }

    @PatchMapping("/product-materials/{id}/visibility")
    @OperationLog(entityType = "PRODUCT", action = "UPDATE")
    public ResponseEntity<?> toggleVisibility(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        try {
            productMaterialRelationService.toggleVisibility(id, body.get("isHidden"));
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "操作失败"));
        }
    }

    // ============ 原料采购批次 (MaterialPurchase) ============

    @GetMapping("/materials")
    public ResponseEntity<?> listMaterialPurchases(@RequestParam(required = false) Long materialId) {
        if (materialId != null) {
            return ResponseEntity.ok(materialPurchaseService.getMaterialPurchasesByMaterialId(materialId));
        }
        return ResponseEntity.ok(materialPurchaseService.listAllMaterialPurchases());
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
            return ResponseEntity.badRequest().body(Map.of("error", "操作失败"));
        }
    }

    @GetMapping("/batches/by-number/{batchNumber}")
    public ResponseEntity<?> getBatchByNumber(@PathVariable String batchNumber) {
        try {
            return ResponseEntity.ok(batchService.getBatchByBatchNumber(batchNumber));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "操作失败"));
        }
    }

    @PostMapping("/batches/{id}/security-codes")
    @OperationLog(entityType = "SECURITY_CODE", action = "CREATE")
    public ResponseEntity<?> generateSecurityCodes(@PathVariable Long id, @RequestBody GenerateSecurityCodeRequest request) {
        log.info("[防伪码管理] 生成防伪码 - 批次ID: {}, 数量: {}", id, request.getQuantity());
        try {
            SecurityCodeGenerateResponse response = securityCodeService.generateCodes(id, request.getQuantity());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("[防伪码管理] 生成防伪码失败 - {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", "操作失败"));
        }
    }

    @GetMapping("/batches/{id}/security-codes")
    public ResponseEntity<?> listSecurityCodes(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(securityCodeService.getCodesByBatchId(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "操作失败"));
        }
    }

    @GetMapping("/security-codes/export/{batchId}")
    public ResponseEntity<?> exportSecurityCodes(@PathVariable Long batchId) {
        try {
            return ResponseEntity.ok(securityCodeService.exportCodes(batchId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "操作失败"));
        }
    }

    @PostMapping("/insert/products/{productId}/generate-qrcode")
    @OperationLog(entityType = "SECURITY_CODE", action = "CREATE")
    public ResponseEntity<?> generateQrCodeForProduct(@PathVariable Long productId) {
        log.info("[产品二维码] 为产品生成二维码 - 产品ID: {}", productId);
        try {
            Product product = productService.getProductById(productId);
            if (product == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "产品不存在"));
            }
            ProductionBatch batch = batchService.createQuickBatchForProduct(productId);
            SecurityCodeGenerateResponse response = securityCodeService.generateCodes(batch.getId(), 1);

            ProductDTO dto = new ProductDTO();
            dto.setId(product.getId());
            dto.setQrCodeUrl("/qrcode/" + product.getId());
            productService.updateProduct(productId, dto);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("[产品二维码] 生成失败 - 产品ID: {}, 错误: {}", productId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", "操作失败"));
        }
    }

    @PostMapping("/insert/products/batch-generate-qrcode")
    @OperationLog(entityType = "SECURITY_CODE", action = "CREATE")
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
                        securityCodeService.generateCodes(batch.getId(), 1);

                        ProductDTO dto = new ProductDTO();
                        dto.setId(product.getId());
                        dto.setQrCodeUrl("/qrcode/" + product.getId());
                        productService.updateProduct(productId, dto);
                        
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
            return ResponseEntity.badRequest().body(Map.of("error", "操作失败"));
        }
    }

    @PostMapping("/insert/products/batch-delete")
    @OperationLog(entityType = "PRODUCT", action = "BATCH_DELETE")
    public ResponseEntity<?> batchDeleteProducts(@RequestBody String body) {
        log.info("[产品管理] 批量删除产品 - 请求体: {}", body);
        try {
            // 解析 JSON 字符串
            com.fasterxml.jackson.databind.JsonNode node = new com.fasterxml.jackson.databind.ObjectMapper().readTree(body);
            com.fasterxml.jackson.databind.JsonNode idsNode = node.get("productIds");
            if (idsNode == null || !idsNode.isArray()) {
                return ResponseEntity.badRequest().body(Map.of("error", "产品ID列表格式错误"));
            }
            log.info("[产品管理] 清除产品二维码 - 产品数量: {}", idsNode.size());
            int successCount = 0;
            int failCount = 0;
            for (com.fasterxml.jackson.databind.JsonNode idNode : idsNode) {
                try {
                    Long productId = idNode.asLong();
                    productService.clearQrCode(productId);
                    successCount++;
                } catch (Exception e) {
                    failCount++;
                    log.warn("[产品管理] 清除二维码失败 - 产品ID: {}", idNode.asText());
                }
            }
            return ResponseEntity.ok(Map.of(
                "success", true,
                "successCount", successCount,
                "failCount", failCount,
                "message", String.format("清除完成，成功: %d，失败: %d", successCount, failCount)
            ));
        } catch (Exception e) {
            log.error("[产品管理] 清除二维码失败 - 错误: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", "操作失败"));
        }
    }

    @GetMapping("/insert/material-purchase")
    public ResponseEntity<?> listInsertMaterialPurchases(@RequestParam(required = false) Long materialId) {
        try {
            if (materialId != null) {
                return ResponseEntity.ok(materialPurchaseService.getMaterialPurchasesByMaterialId(materialId));
            }
            return ResponseEntity.ok(materialPurchaseService.listAllMaterialPurchases());
        } catch (Exception e) {
            log.error("[原材料管理] 获取原材料列表失败 - {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", "操作失败"));
        }
    }

    @GetMapping("/insert/materials")
    public ResponseEntity<?> listInsertMaterials(@RequestParam(required = false) Long materialId) {
        return listInsertMaterialPurchases(materialId);
    }

    @GetMapping("/insert/batches")
    public ResponseEntity<?> listInsertBatches(@RequestParam(required = false) Long productId) {
        if (productId != null) {
            return ResponseEntity.ok(batchService.getBatchesByProductId(productId));
        }
        return ResponseEntity.ok(batchService.listAllBatches());
    }

    @GetMapping("/insert/inspections")
    public ResponseEntity<?> listAllInspections() {
        try {
            var inspections = inspectionRepository.findAll();
            var result = inspections.stream().map(i -> {
                var dto = new java.util.HashMap<String, Object>();
                dto.put("id", i.getId());
                dto.put("batchId", i.getBatchId());
                dto.put("sampleName", i.getSampleName());
                dto.put("sampleQuantity", i.getSampleQuantity());
                dto.put("sampleSpecification", i.getSampleSpecification());
                dto.put("imageUrl", i.getImageUrl());
                dto.put("inspectorName", i.getInspectorName());
                dto.put("inspectionTime", i.getInspectionTime());
                return dto;
            }).toList();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("[检验检测] 获取列表失败 - {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", "操作失败"));
        }
    }

    @GetMapping("/insert/storages")
    public ResponseEntity<?> listAllStorages() {
        try {
            var storages = storageRepository.findAll();
            var result = storages.stream().map(s -> {
                var dto = new java.util.HashMap<String, Object>();
                dto.put("id", s.getId());
                dto.put("batchId", s.getBatchId());
                dto.put("storageTime", s.getStorageTime());
                dto.put("outboundTime", s.getOutboundTime());
                dto.put("quantity", s.getQuantity());
                dto.put("unit", s.getUnit());
                dto.put("warehouseLocation", s.getWarehouseLocation());
                return dto;
            }).toList();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("[仓储] 获取列表失败 - {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", "操作失败"));
        }
    }

    @GetMapping("/insert/transport-sales")
    public ResponseEntity<?> listAllTransportSales() {
        try {
            var transportSales = transportSaleRepository.findAll();
            var result = transportSales.stream().map(t -> {
                var dto = new java.util.HashMap<String, Object>();
                dto.put("id", t.getId());
                dto.put("batchId", t.getBatchId());
                dto.put("transportCompany", t.getTransportCompany());
                dto.put("vehicleNumber", t.getVehicleNumber());
                dto.put("salesRegion", t.getSalesRegion());
                dto.put("receiverName", t.getReceiverName());
                dto.put("receiverContact", t.getReceiverContact());
                dto.put("environmentTemperature", t.getEnvironmentTemperature());
                dto.put("productTemperature", t.getProductTemperature());
                dto.put("time", t.getTime());
                dto.put("recorderName", t.getRecorderName());
                return dto;
            }).toList();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("[运输销售] 获取列表失败 - {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", "操作失败"));
        }
    }
}