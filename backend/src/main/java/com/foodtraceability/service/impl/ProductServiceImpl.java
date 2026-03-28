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
        Product entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("产品不存在"));
        
        List<ProductionBatch> batches = batchRepository.findByProductIdAndIsDeletedFalse(id);
        log.info("[产品删除] 找到 {} 个关联批次", batches.size());
        
        for (ProductionBatch batch : batches) {
            log.info("[产品删除] 删除批次 ID: {}", batch.getId());
            securityCodeRepository.deleteAll(securityCodeRepository.findByBatchId(batch.getId()));
            inspectionRepository.deleteAll(inspectionRepository.findByBatch(batch));
            storageRepository.deleteAll(storageRepository.findByBatch(batch));
            transportSaleRepository.deleteAll(transportSaleRepository.findByBatch(batch));
            relationRepository.deleteAll(relationRepository.findByBatchId(batch.getId()));
            batchRepository.delete(batch);
        }
        
        log.info("[产品删除] 删除产品 ID: {}", id);
        repository.delete(entity);
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