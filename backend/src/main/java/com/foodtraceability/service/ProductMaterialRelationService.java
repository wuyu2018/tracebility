package com.foodtraceability.service;

import com.foodtraceability.dto.ProductMaterialRelationDTO;
import com.foodtraceability.entity.ProductMaterialRelation;

import java.util.List;

public interface ProductMaterialRelationService {
    ProductMaterialRelation bindMaterialToProduct(Long productId, Long materialId);

    void unbindMaterialFromProduct(Long productId, Long materialId);

    void toggleVisibility(Long relationId, Boolean isHidden);

    List<ProductMaterialRelationDTO> getRelationsByProductId(Long productId);

    boolean isMaterialVisibleToProduct(Long productId, Long materialId);
}
