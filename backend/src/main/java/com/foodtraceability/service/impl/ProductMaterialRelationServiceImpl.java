package com.foodtraceability.service.impl;

import com.foodtraceability.dto.ProductMaterialRelationDTO;
import com.foodtraceability.entity.Material;
import com.foodtraceability.entity.Product;
import com.foodtraceability.entity.ProductMaterialRelation;
import com.foodtraceability.exception.BusinessException;
import com.foodtraceability.repository.MaterialRepository;
import com.foodtraceability.repository.ProductMaterialRelationRepository;
import com.foodtraceability.repository.ProductRepository;
import com.foodtraceability.service.ProductMaterialRelationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductMaterialRelationServiceImpl implements ProductMaterialRelationService {
    private static final Logger log = LoggerFactory.getLogger(ProductMaterialRelationServiceImpl.class);

    private final ProductMaterialRelationRepository repository;
    private final ProductRepository productRepository;
    private final MaterialRepository materialRepository;

    public ProductMaterialRelationServiceImpl(ProductMaterialRelationRepository repository,
                                              ProductRepository productRepository,
                                              MaterialRepository materialRepository) {
        this.repository = repository;
        this.productRepository = productRepository;
        this.materialRepository = materialRepository;
    }

    @Override
    @Transactional
    public ProductMaterialRelation bindMaterialToProduct(Long productId, Long materialId) {
        if (repository.existsByProductIdAndMaterialId(productId, materialId)) {
            throw new BusinessException("该产品已绑定此原料品种");
        }
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException("产品不存在: " + productId));
        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new BusinessException("原料品种不存在: " + materialId));
        ProductMaterialRelation relation = ProductMaterialRelation.create(product, material);
        ProductMaterialRelation saved = repository.save(relation);
        log.info("[产品-原料关联] 绑定 - 产品ID: {}, 原料ID: {}", productId, materialId);
        return saved;
    }

    @Override
    @Transactional
    public void unbindMaterialFromProduct(Long productId, Long materialId) {
        ProductMaterialRelation relation = repository.findByProductIdAndMaterialId(productId, materialId)
                .orElseThrow(() -> new BusinessException("该产品未绑定此原料品种"));
        repository.delete(relation);
        log.info("[产品-原料关联] 解绑 - 产品ID: {}, 原料ID: {}", productId, materialId);
    }

    @Override
    @Transactional
    public void toggleVisibility(Long relationId, Boolean isHidden) {
        ProductMaterialRelation relation = repository.findById(relationId)
                .orElseThrow(() -> new BusinessException("关联记录不存在: " + relationId));
        if (Boolean.TRUE.equals(isHidden)) {
            relation.hide();
        } else {
            relation.show();
        }
        repository.save(relation);
        log.info("[产品-原料关联] 切换可见性 - ID: {}, is_hidden: {}", relationId, isHidden);
    }

    @Override
    public List<ProductMaterialRelationDTO> getRelationsByProductId(Long productId) {
        return repository.findByProductId(productId).stream()
                .map(r -> {
                    ProductMaterialRelationDTO dto = new ProductMaterialRelationDTO();
                    dto.setId(r.getId());
                    dto.setProductId(r.getProduct().getId());
                    dto.setMaterialId(r.getMaterial().getId());
                    dto.setMaterialName(r.getMaterial().getName());
                    dto.setIsHidden(r.getIsHidden());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public boolean isMaterialVisibleToProduct(Long productId, Long materialId) {
        return repository.findByProductIdAndMaterialId(productId, materialId)
                .map(r -> !r.isHidden())
                .orElse(false);
    }
}
