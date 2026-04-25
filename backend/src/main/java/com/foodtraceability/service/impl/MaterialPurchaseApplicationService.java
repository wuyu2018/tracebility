package com.foodtraceability.service.impl;

import com.foodtraceability.domain.DomainException;
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
public class MaterialPurchaseApplicationService implements MaterialPurchaseService {

    private final MaterialPurchaseRepository repository;
    private final ProductRepository productRepository;

    @Autowired
    public MaterialPurchaseApplicationService(
            MaterialPurchaseRepository repository,
            ProductRepository productRepository) {
        this.repository = repository;
        this.productRepository = productRepository;
    }

    @Override
    @Transactional
    public MaterialPurchase createMaterialPurchase(MaterialPurchaseDTO dto) {
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new DomainException("产品不存在"));

        MaterialPurchase entity = MaterialPurchase.create(
                product,
                dto.getMaterialName(),
                dto.getBatchNumber()
        );
        BeanUtils.copyProperties(dto, entity);
        return repository.save(entity);
    }

    @Override
    @Transactional
    public MaterialPurchase updateMaterialPurchase(Long id, MaterialPurchaseDTO dto) {
        MaterialPurchase entity = repository.findById(id)
                .orElseThrow(() -> new DomainException("原材料采购记录不存在"));

        if (dto.getProductId() != null && !dto.getProductId().equals(entity.getProduct().getId())) {
            Product product = productRepository.findById(dto.getProductId())
                    .orElseThrow(() -> new DomainException("产品不存在"));
            entity.setProduct(product);
        }

        if (dto.getSupplierName() != null || dto.getProducerName() != null || dto.getProducerAddress() != null) {
            entity.updateSupplier(
                    dto.getSupplierName() != null ? dto.getSupplierName() : entity.getSupplierName(),
                    dto.getProducerName() != null ? dto.getProducerName() : entity.getProducerName(),
                    dto.getProducerAddress() != null ? dto.getProducerAddress() : entity.getProducerAddress()
            );
        }

        if (dto.getQuantity() != null || dto.getUnit() != null) {
            entity.updateQuantity(
                    dto.getQuantity() != null ? dto.getQuantity() : entity.getQuantity(),
                    dto.getUnit() != null ? dto.getUnit() : entity.getUnit()
            );
        }

        BeanUtils.copyProperties(dto, entity);
        return repository.save(entity);
    }

    @Override
    @Transactional
    public void deleteMaterialPurchase(Long id) {
        MaterialPurchase entity = repository.findById(id)
                .orElseThrow(() -> new DomainException("原材料采购记录不存在"));
        entity.softDelete();
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
