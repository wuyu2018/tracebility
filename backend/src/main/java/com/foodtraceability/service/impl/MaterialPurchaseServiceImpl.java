package com.foodtraceability.service.impl;

import com.foodtraceability.dto.MaterialPurchaseDTO;
import com.foodtraceability.entity.MaterialPurchase;
import com.foodtraceability.entity.Product;
import com.foodtraceability.repository.MaterialPurchaseRepository;
import com.foodtraceability.repository.ProductRepository;
import com.foodtraceability.service.MaterialPurchaseService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MaterialPurchaseServiceImpl implements MaterialPurchaseService {
    @Autowired
    private MaterialPurchaseRepository repository;

    @Autowired
    private ProductRepository productRepository;

    @Override
    @Transactional
    public MaterialPurchase createMaterialPurchase(MaterialPurchaseDTO dto) {
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new RuntimeException("产品不存在"));

        MaterialPurchase entity = new MaterialPurchase();
        BeanUtils.copyProperties(dto, entity);
        entity.setProduct(product);
        entity.setIsDeleted(false);
        return repository.save(entity);
    }

    @Override
    @Transactional
    public MaterialPurchase updateMaterialPurchase(Long id, MaterialPurchaseDTO dto) {
        MaterialPurchase entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("原材料采购记录不存在"));

        if (dto.getProductId() != null) {
            Product product = productRepository.findById(dto.getProductId())
                    .orElseThrow(() -> new RuntimeException("产品不存在"));
            entity.setProduct(product);
        }
        BeanUtils.copyProperties(dto, entity);
        return repository.save(entity);
    }

    @Override
    @Transactional
    public void deleteMaterialPurchase(Long id) {
        MaterialPurchase entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("原材料采购记录不存在"));
        entity.setIsDeleted(true);
        repository.save(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MaterialPurchase> listAllMaterialPurchases() {
        return repository.findByIsDeletedFalse();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MaterialPurchase> getMaterialPurchasesByProductId(Long productId) {
        return repository.findByProductIdAndIsDeletedFalse(productId);
    }
}