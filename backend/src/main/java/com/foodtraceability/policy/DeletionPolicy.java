package com.foodtraceability.policy;

import com.foodtraceability.entity.Material;
import com.foodtraceability.entity.Product;
import com.foodtraceability.entity.ProductionBatch;
import com.foodtraceability.entity.SecurityCode;
import com.foodtraceability.exception.BusinessException;
import com.foodtraceability.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class DeletionPolicy {
    private static final Logger log = LoggerFactory.getLogger(DeletionPolicy.class);

    private final ProductionBatchRepository batchRepository;
    private final SecurityCodeRepository securityCodeRepository;
    private final ProductMaterialRelationRepository pmrRepository;
    private final ProductRepository productRepository;

    public DeletionPolicy(ProductionBatchRepository batchRepository,
                          SecurityCodeRepository securityCodeRepository,
                          ProductMaterialRelationRepository pmrRepository,
                          ProductRepository productRepository) {
        this.batchRepository = batchRepository;
        this.securityCodeRepository = securityCodeRepository;
        this.pmrRepository = pmrRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public void deleteProduct(Product product) {
        Long productId = product.getId();
        log.info("[DeletionPolicy] 删除产品 - ID: {}, 名称: {}", productId, product.getName());

        boolean hasBatches = batchRepository.existsByProductId(productId);
        boolean hasCodes = securityCodeRepository.existsByBatchProductId(productId);

        if (!hasBatches && !hasCodes) {
            physicallyDeleteProduct(product);
        } else {
            softDeleteProduct(product);
        }
    }

    @Transactional
    public void hardDeleteProduct(Product product) {
        Long productId = product.getId();
        boolean hasBatches = batchRepository.existsByProductId(productId);
        boolean hasCodes = securityCodeRepository.existsByBatchProductId(productId);

        if (hasBatches || hasCodes) {
            throw new BusinessException(
                    String.format("产品 ID:%d 有关联的生产批次或防伪码，禁止物理删除", productId));
        }
        physicallyDeleteProduct(product);
    }

    @Transactional
    public void deleteMaterial(Material material) {
        Long materialId = material.getId();
        log.info("[DeletionPolicy] 删除原料品种 - ID: {}, 名称: {}", materialId, material.getName());

        pmrRepository.findByProductId(materialId).stream()
                .filter(r -> r.getMaterial().getId().equals(materialId))
                .forEach(r -> pmrRepository.delete(r));

        material.deactivate();
        log.info("[DeletionPolicy] 原料品种已停用 - ID: {}", materialId);
    }

    private void physicallyDeleteProduct(Product product) {
        Long productId = product.getId();
        log.info("[DeletionPolicy] 物理删除产品 - ID: {}", productId);

        pmrRepository.deleteByProductId(productId);
        productRepository.delete(product);

        log.info("[DeletionPolicy] 产品已物理删除 - ID: {}", productId);
    }

    private void softDeleteProduct(Product product) {
        Long productId = product.getId();
        log.info("[DeletionPolicy] 软删除产品 - ID: {}", productId);

        pmrRepository.deleteByProductId(productId);
        product.softDelete();
        productRepository.save(product);

        log.info("[DeletionPolicy] 产品已软删除 - ID: {}", productId);
    }
}
