package com.foodtraceability.controller;

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
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class DataManagementController {

    private static final Logger log = LoggerFactory.getLogger(DataManagementController.class);

    private final ProductService productService;
    private final MaterialService materialService;
    private final MaterialPurchaseService materialPurchaseService;
    private final ProductMaterialRelationService productMaterialRelationService;
    private final ProductionBatchService batchService;
    private final SecurityCodeService securityCodeService;
    private final TraceabilityService traceabilityService;
    private final InspectionRepository inspectionRepository;
    private final StorageRepository storageRepository;
    private final TransportSaleRepository transportSaleRepository;

    public DataManagementController(ProductService productService,
                                   MaterialService materialService,
                                   MaterialPurchaseService materialPurchaseService,
                                   ProductMaterialRelationService productMaterialRelationService,
                                   ProductionBatchService batchService,
                                   SecurityCodeService securityCodeService,
                                   TraceabilityService traceabilityService,
                                   InspectionRepository inspectionRepository,
                                   StorageRepository storageRepository,
                                   TransportSaleRepository transportSaleRepository) {
        this.productService = productService;
        this.materialService = materialService;
        this.materialPurchaseService = materialPurchaseService;
        this.productMaterialRelationService = productMaterialRelationService;
        this.batchService = batchService;
        this.securityCodeService = securityCodeService;
        this.traceabilityService = traceabilityService;
        this.inspectionRepository = inspectionRepository;
        this.storageRepository = storageRepository;
        this.transportSaleRepository = transportSaleRepository;
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

    @DeleteMapping("/products/{id}/hard")
    public ResponseEntity<?> hardDeleteProduct(@PathVariable Long id) {
        log.info("[产品管理] 物理删除产品 - ID: {}", id);
        try {
            productService.hardDeleteProduct(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "物理删除成功"));
        } catch (Exception e) {
            log.error("[产品管理] 物理删除产品失败 - ID: {}, {}", id, e.getMessage());
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

    @GetMapping("/product-detail")
    public ResponseEntity<?> getProductDetail(@RequestParam String antiFakeCode) {
        try {
            return traceabilityService.getTraceInfoByCode(antiFakeCode)
                    .map(result -> ResponseEntity.ok((Object) result))
                    .orElse(ResponseEntity.ok(Map.of("error", "未找到该防伪码对应的产品信息")));
        } catch (Exception e) {
            log.error("[产品详情] 获取失败 - 防伪码: {}, 错误: {}", SecurityCode.maskCode(antiFakeCode), e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/admin/product-detail")
    public ResponseEntity<?> getProductDetailForAdmin(@RequestParam String antiFakeCode) {
        try {
            return traceabilityService.getTraceInfoByCodeForAdmin(antiFakeCode)
                    .map(result -> ResponseEntity.ok((Object) result))
                    .orElse(ResponseEntity.ok(Map.of("error", "未找到该防伪码对应的产品信息")));
        } catch (Exception e) {
            log.error("[管理员产品详情] 获取失败 - 防伪码: {}, 错误: {}", SecurityCode.maskCode(antiFakeCode), e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ============ 原料品种 (Material) ============

    @PostMapping("/material-varieties")
    public ResponseEntity<?> createMaterialVariety(@RequestBody MaterialDTO dto) {
        log.info("[原料品种] 创建 - 名称: {}", dto.getName());
        try {
            com.foodtraceability.entity.Material created = materialService.createMaterial(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            log.error("[原料品种] 创建失败 - {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/material-varieties")
    public ResponseEntity<?> listMaterialVarieties(@RequestParam(required = false, defaultValue = "true") Boolean activeOnly) {
        try {
            if (Boolean.TRUE.equals(activeOnly)) {
                return ResponseEntity.ok(materialService.listAllActiveMaterials());
            }
            return ResponseEntity.ok(materialService.listAllMaterials());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/material-varieties/{id}")
    public ResponseEntity<?> getMaterialVariety(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(materialService.getMaterialById(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/material-varieties/{id}")
    public ResponseEntity<?> updateMaterialVariety(@PathVariable Long id, @RequestBody MaterialDTO dto) {
        log.info("[原料品种] 更新 - ID: {}", id);
        try {
            return ResponseEntity.ok(materialService.updateMaterial(id, dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/material-varieties/{id}")
    public ResponseEntity<?> deleteMaterialVariety(@PathVariable Long id) {
        log.info("[原料品种] 删除 - ID: {}", id);
        try {
            materialService.deleteMaterial(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "删除成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/material-varieties/{id}/deactivate")
    public ResponseEntity<?> deactivateMaterialVariety(@PathVariable Long id) {
        try {
            materialService.deactivateMaterial(id);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/material-varieties/{id}/activate")
    public ResponseEntity<?> activateMaterialVariety(@PathVariable Long id) {
        try {
            materialService.activateMaterial(id);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ============ 产品-原料可见性 (ProductMaterialRelation) ============

    @PostMapping("/product-materials")
    public ResponseEntity<?> bindMaterialToProduct(@RequestBody Map<String, Long> body) {
        try {
            Long productId = body.get("productId");
            Long materialId = body.get("materialId");
            var relation = productMaterialRelationService.bindMaterialToProduct(productId, materialId);
            return ResponseEntity.status(HttpStatus.CREATED).body(relation);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/product-materials")
    public ResponseEntity<?> getProductMaterialRelations(@RequestParam Long productId) {
        try {
            return ResponseEntity.ok(productMaterialRelationService.getRelationsByProductId(productId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/product-materials")
    public ResponseEntity<?> unbindMaterialFromProduct(@RequestParam Long productId, @RequestParam Long materialId) {
        try {
            productMaterialRelationService.unbindMaterialFromProduct(productId, materialId);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/product-materials/{id}/visibility")
    public ResponseEntity<?> toggleVisibility(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        try {
            productMaterialRelationService.toggleVisibility(id, body.get("isHidden"));
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ============ 原料采购批次 (MaterialPurchase) ============

    @PostMapping("/materials")
    public ResponseEntity<?> createMaterialPurchase(@RequestBody MaterialPurchaseDTO dto) {
        log.info("[原材料采购] 创建 - 原料ID: {}, 批次: {}", dto.getMaterialId(), dto.getBatchNumber());
        try {
            MaterialPurchase created = materialPurchaseService.createMaterialPurchase(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            log.error("[原材料采购] 创建失败 - {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/materials/{id}")
    public ResponseEntity<?> updateMaterialPurchase(@PathVariable Long id, @RequestBody MaterialPurchaseDTO dto) {
        log.info("[原材料采购] 更新 - ID: {}", id);
        try {
            MaterialPurchase updated = materialPurchaseService.updateMaterialPurchase(id, dto);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("[原材料采购] 更新失败 - {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/materials/{id}")
    public ResponseEntity<?> deleteMaterialPurchase(@PathVariable Long id) {
        log.info("[原材料采购] 删除 - ID: {}", id);
        try {
            materialPurchaseService.deleteMaterialPurchase(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "删除成功"));
        } catch (Exception e) {
            log.error("[原材料采购] 删除失败 - {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/materials")
    public ResponseEntity<?> listMaterialPurchases(@RequestParam(required = false) Long materialId) {
        if (materialId != null) {
            return ResponseEntity.ok(materialPurchaseService.getMaterialPurchasesByMaterialId(materialId));
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
            
            ProductDTO dto = new ProductDTO();
            dto.setId(product.getId());
            dto.setQrCodeUrl("/qrcode/" + product.getId());
            productService.updateProduct(productId, dto);
            
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
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/insert/products/batch-delete")
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
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/insert/material-purchase")
    public ResponseEntity<?> insertMaterialPurchase(@RequestBody Map<String, Object> request) {
        try {
            Object materialIdObj = request.get("materialId");
            if (materialIdObj == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "原料品种ID不能为空"));
            }
            Long materialId = materialIdObj instanceof Number
                    ? ((Number) materialIdObj).longValue()
                    : Long.parseLong(materialIdObj.toString());

            MaterialPurchaseDTO dto = new MaterialPurchaseDTO();
            dto.setMaterialId(materialId);
            dto.setBatchNumber((String) request.get("batchNumber"));
            dto.setProducerName((String) request.get("producerName"));
            dto.setProducerAddress((String) request.get("producerAddress"));
            dto.setSupplierName((String) request.get("supplierName"));

            MaterialPurchase created = materialPurchaseService.createMaterialPurchase(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            log.error("[原材料管理] 插入原材料失败 - {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/insert/material-purchase/{id}")
    public ResponseEntity<?> deleteInsertMaterialPurchase(@PathVariable Long id) {
        try {
            materialPurchaseService.deleteMaterialPurchase(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "删除成功"));
        } catch (Exception e) {
            log.error("[原材料管理] 删除原材料失败 - {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
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
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
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
                return dto;
            }).toList();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("[检验检测] 获取列表失败 - {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
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
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
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
                return dto;
            }).toList();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("[运输销售] 获取列表失败 - {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}