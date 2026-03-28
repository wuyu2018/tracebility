package com.foodtraceability.service.impl;

import com.foodtraceability.dto.ProductDTO;
import com.foodtraceability.entity.*;
import com.foodtraceability.repository.*;
import com.foodtraceability.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {
    private static final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);
    
    @Autowired
    private ProductRepository repository;

    @Autowired
    private ProductionBatchRepository batchRepository;

    @Autowired
    private InspectionRepository inspectionRepository;

    @Autowired
    private StorageRepository storageRepository;

    @Autowired
    private TransportSaleRepository transportSaleRepository;

    @Autowired
    private BatchMaterialRelationRepository relationRepository;

    @Autowired
    private SecurityCodeRepository securityCodeRepository;

    @Autowired
    private MaterialPurchaseRepository materialPurchaseRepository;

    @Override
    @Transactional
    public Product createProduct(ProductDTO dto) {
        Product entity = new Product();
        BeanUtils.copyProperties(dto, entity);
        entity.setIsDeleted(false);
        return repository.save(entity);
    }

    @Override
    @Transactional
    public Product updateProduct(Long id, ProductDTO dto) {
        Product entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("产品不存在"));
        BeanUtils.copyProperties(dto, entity);
        return repository.save(entity);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        log.info("[产品删除] 开始删除产品 ID: {}", id);
        
        // 1. 先删除 batch_material_relation（指向 material_purchase）
        List<ProductionBatch> batches = batchRepository.findAll().stream()
            .filter(b -> b.getProduct() != null && b.getProduct().getId().equals(id)).toList();
        for (ProductionBatch batch : batches) {
            relationRepository.deleteAll(relationRepository.findByBatchId(batch.getId()));
        }
        
        // 2. 再删除直接关联的原材料记录
        materialPurchaseRepository.deleteAll(materialPurchaseRepository.findByProductIdAndIsDeletedFalse(id));
        
        // 3. 按依赖关系删除其他关联数据
        securityCodeRepository.deleteAll(securityCodeRepository.findAll().stream()
            .filter(sc -> sc.getBatch() != null && sc.getBatch().getProduct() != null 
                && sc.getBatch().getProduct().getId().equals(id)).toList());
        
        inspectionRepository.deleteAll(inspectionRepository.findAll().stream()
            .filter(i -> i.getBatch() != null && i.getBatch().getProduct() != null 
                && i.getBatch().getProduct().getId().equals(id)).toList());
        
        storageRepository.deleteAll(storageRepository.findAll().stream()
            .filter(s -> s.getBatch() != null && s.getBatch().getProduct() != null 
                && s.getBatch().getProduct().getId().equals(id)).toList());
        
        transportSaleRepository.deleteAll(transportSaleRepository.findAll().stream()
            .filter(t -> t.getBatch() != null && t.getBatch().getProduct() != null 
                && t.getBatch().getProduct().getId().equals(id)).toList());
        
        // 4. 删除批次
        batchRepository.deleteAll(batches);
        
        // 5. 最后删除产品
        repository.deleteById(id);
        log.info("[产品删除] 删除完成");
    }

    @Override
    @Transactional
    public void clearQrCode(Long id) {
        Product entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("产品不存在"));
        entity.setAntiFakeCode(null);
        entity.setQrCodeUrl(null);
        repository.save(entity);
    }

    @Override
    public List<Product> listAllProducts() {
        return repository.findByIsDeletedFalse();
    }

    @Override
    public List<Product> searchProducts(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return listAllProducts();
        }
        return repository.findByNameContainingAndIsDeletedFalse(keyword.trim());
    }

    @Override
    public Product getProductById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("产品不存在"));
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.Optional<Product> getProductByAntiFakeCode(String antiFakeCode) {
        return repository.findByAntiFakeCode(antiFakeCode);
    }
}