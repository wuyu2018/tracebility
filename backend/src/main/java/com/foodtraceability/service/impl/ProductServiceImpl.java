package com.foodtraceability.service.impl;

import com.foodtraceability.dto.ProductDTO;
import com.foodtraceability.entity.Product;
import com.foodtraceability.exception.BusinessException;
import com.foodtraceability.policy.DeletionPolicy;
import com.foodtraceability.repository.ProductRepository;
import com.foodtraceability.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {
    private static final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);

    private final ProductRepository repository;
    private final DeletionPolicy deletionPolicy;

    public ProductServiceImpl(ProductRepository repository,
                             DeletionPolicy deletionPolicy) {
        this.repository = repository;
        this.deletionPolicy = deletionPolicy;
    }

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
                .orElseThrow(() -> new BusinessException("产品不存在"));
        BeanUtils.copyProperties(dto, entity);
        return repository.save(entity);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        Product product = repository.findById(id)
                .orElseThrow(() -> new BusinessException("产品不存在: " + id));
        deletionPolicy.deleteProduct(product);
        log.info("[产品管理] 删除产品 - ID: {}", id);
    }

    @Override
    @Transactional
    public void hardDeleteProduct(Long id) {
        Product product = repository.findById(id)
                .orElseThrow(() -> new BusinessException("产品不存在: " + id));
        deletionPolicy.hardDeleteProduct(product);
        log.info("[产品管理] 物理删除产品 - ID: {}", id);
    }

    @Override
    @Transactional
    public void clearQrCode(Long id) {
        Product entity = repository.findById(id)
                .orElseThrow(() -> new BusinessException("产品不存在"));
        entity.clearQrCode();
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
                .orElseThrow(() -> new BusinessException("产品不存在"));
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.Optional<Product> getProductByAntiFakeCode(String antiFakeCode) {
        return repository.findByAntiFakeCode(antiFakeCode);
    }
}
