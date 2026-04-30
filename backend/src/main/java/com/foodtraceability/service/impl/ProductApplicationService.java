package com.foodtraceability.service.impl;

import com.foodtraceability.domain.DomainException;
import com.foodtraceability.dto.ProductDTO;
import com.foodtraceability.entity.Product;
import com.foodtraceability.policy.DeletionPolicy;
import com.foodtraceability.repository.ProductRepository;
import com.foodtraceability.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ProductApplicationService implements ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductApplicationService.class);

    private final ProductRepository repository;
    private final DeletionPolicy deletionPolicy;

    @Autowired
    public ProductApplicationService(ProductRepository repository, DeletionPolicy deletionPolicy) {
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
                .orElseThrow(() -> new DomainException("产品不存在"));
        copyNonNullProperties(dto, entity);
        return repository.save(entity);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        Product product = repository.findById(id)
                .orElseThrow(() -> new DomainException("产品不存在"));
        deletionPolicy.deleteProduct(product);
    }

    @Override
    @Transactional
    public void hardDeleteProduct(Long id) {
        Product product = repository.findById(id)
                .orElseThrow(() -> new DomainException("产品不存在"));
        deletionPolicy.hardDeleteProduct(product);
    }

    @Override
    @Transactional
    public void clearQrCode(Long id) {
        Product entity = repository.findById(id)
                .orElseThrow(() -> new DomainException("产品不存在"));
        entity.clearQrCode();
        repository.save(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> listAllProducts() {
        return repository.findByIsDeletedFalse();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> searchProducts(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return listAllProducts();
        }
        return repository.findByNameContainingAndIsDeletedFalse(keyword.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public Product getProductById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new DomainException("产品不存在"));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Product> getProductByAntiFakeCode(String antiFakeCode) {
        return repository.findByAntiFakeCode(antiFakeCode);
    }

    private void copyNonNullProperties(ProductDTO source, Product target) {
        if (source.getName() != null) target.setName(source.getName());
        if (source.getSpecification() != null) target.setSpecification(source.getSpecification());
        if (source.getShelfLife() != null) target.setShelfLife(source.getShelfLife());
        if (source.getImageUrl() != null) target.setImageUrl(source.getImageUrl());
        if (source.getContactPhone() != null) target.setContactPhone(source.getContactPhone());
        if (source.getContactEmail() != null) target.setContactEmail(source.getContactEmail());
        if (source.getQrCodeUrl() != null) target.setQrCodeUrl(source.getQrCodeUrl());
    }
}
