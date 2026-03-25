package com.foodtraceability.service;

import com.foodtraceability.dto.MaterialPurchaseDTO;
import com.foodtraceability.entity.MaterialPurchase;

import java.util.List;

public interface MaterialPurchaseService {
    MaterialPurchase createMaterialPurchase(MaterialPurchaseDTO dto);
    MaterialPurchase updateMaterialPurchase(Long id, MaterialPurchaseDTO dto);
    void deleteMaterialPurchase(Long id);
    List<MaterialPurchase> listAllMaterialPurchases();
    List<MaterialPurchase> getMaterialPurchasesByProductId(Long productId);
}