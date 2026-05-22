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
    private final ProductMaterialRelationService productMaterialRelationService;
    private final ProductionBatchService batchService;
    private final SecurityCodeService securityCodeService;
    private final com.foodtraceability.util.SecurityUtils securityUtils;
    private final ProductRepository productRepository;
    private final ProductMaterialRelationRepository productMaterialRelationRepository;

    public DataManagementController(ProductService productService,
                                   ProductMaterialRelationService productMaterialRelationService,
                                   ProductionBatchService batchService,
                                   SecurityCodeService securityCodeService,
                                   com.foodtraceability.util.SecurityUtils securityUtils,
                                   ProductRepository productRepository,
                                   ProductMaterialRelationRepository productMaterialRelationRepository) {
        this.productService = productService;
        this.productMaterialRelationService = productMaterialRelationService;
        this.batchService = batchService;
        this.securityCodeService = securityCodeService;
        this.securityUtils = securityUtils;
        this.productRepository = productRepository;
        this.productMaterialRelationRepository = productMaterialRelationRepository;
    }

    @PostMapping("/products")
    @OperationLog(entityType = "PRODUCT", action = "CREATE")
    public ResponseEntity<?> createProduct(@RequestBody ProductDTO dto) {
        log.info("[产品管理] 创建产品 - 名称: {}", dto.getName());
        try {
            Long companyId = securityUtils.getCurrentCompanyId();
            if (companyId != null) {
                dto.setCompanyId(companyId);
            }
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
        Long companyId = securityUtils.getCurrentCompanyId();
        if (keyword != null && !keyword.trim().isEmpty()) {
            if (companyId != null) {
                return ResponseEntity.ok(productRepository.findByNameContainingAndCompanyIdAndIsDeletedFalse(keyword.trim(), companyId));
            }
            return ResponseEntity.ok(productService.searchProducts(keyword.trim()));
        }
        if (companyId != null) {
            return ResponseEntity.ok(productRepository.findByCompanyIdAndIsDeletedFalse(companyId));
        }
        return ResponseEntity.ok(productService.listAllProducts());
    }

    @GetMapping("/products/select")
    public ResponseEntity<?> selectProducts(@RequestParam(required = false) String keyword,
                                             @RequestParam(required = false, defaultValue = "consumer") String role) {
        log.info("[产品选择] 查询产品 - 关键词: {}, 角色: {}", keyword, role);
        try {
            Long companyId = securityUtils.getCurrentCompanyId();
            if (companyId != null) {
                if (keyword != null && !keyword.trim().isEmpty()) {
                    return ResponseEntity.ok(productRepository.findByNameContainingAndCompanyIdAndIsDeletedFalse(keyword.trim(), companyId));
                }
                return ResponseEntity.ok(productRepository.findByCompanyIdAndIsDeletedFalse(companyId));
            }
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
            Long companyId = securityUtils.getCurrentCompanyId();
            if (companyId != null) {
                return ResponseEntity.ok(productRepository.findByCompanyIdAndIsDeletedFalse(companyId));
            }
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
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/product-materials")
    public ResponseEntity<?> getProductMaterialRelations(@RequestParam(required = false) Long productId) {
        try {
            Long companyId = securityUtils.getCurrentCompanyId();
            if (productId != null) {
                return ResponseEntity.ok(productMaterialRelationService.getRelationsByProductId(productId));
            }
            if (companyId != null) {
                var relations = productMaterialRelationRepository.findByCompanyId(companyId);
                return ResponseEntity.ok(relations.stream().map(r -> {
                    var dto = new com.foodtraceability.dto.ProductMaterialRelationDTO();
                    dto.setId(r.getId());
                    dto.setProductId(r.getProduct().getId());
                    dto.setProductName(r.getProduct().getName());
                    dto.setMaterialId(r.getMaterial().getId());
                    dto.setMaterialName(r.getMaterial().getName());
                    dto.setIsHidden(r.getIsHidden());
                    return dto;
                }).toList());
            }
            return ResponseEntity.ok(productMaterialRelationService.listAllRelations());
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
    @OperationLog(entityType = "PRODUCT", action = "UPDATE")
    public ResponseEntity<?> toggleVisibility(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        try {
            productMaterialRelationService.toggleVisibility(id, body.get("isHidden"));
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ============ 防伪码 & 二维码 ============

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
}
