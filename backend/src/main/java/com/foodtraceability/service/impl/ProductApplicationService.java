package com.foodtraceability.service.impl;

import com.foodtraceability.domain.DomainException;
import com.foodtraceability.dto.ProductDTO;
import com.foodtraceability.entity.Product;
import com.foodtraceability.repository.ProductRepository;
import com.foodtraceability.service.ProductService;
import com.foodtraceability.service.domain.ProductDeletionService;
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
    private final ProductDeletionService deletionService;

    @Autowired
    public ProductApplicationService(ProductRepository repository, ProductDeletionService deletionService) {
        this.repository = repository;
        this.deletionService = deletionService;
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

        if (dto.getName() != null) {
            entity.setName(dto.getName());
        }
        if (dto.getSpecification() != null) {
            entity.setSpecification(dto.getSpecification());
        }
        if (dto.getShelfLife() != null) {
            entity.setShelfLife(dto.getShelfLife());
        }
        if (dto.getImageUrl() != null) {
            entity.setImageUrl(dto.getImageUrl());
        }
        if (dto.getContactPhone() != null) {
            entity.setContactPhone(dto.getContactPhone());
        }
        if (dto.getContactEmail() != null) {
            entity.setContactEmail(dto.getContactEmail());
        }
        if (dto.getQrCodeUrl() != null) {
            entity.setQrCodeUrl(dto.getQrCodeUrl());
        }
        if (dto.getAntiFakeCode() != null) {
            entity.setAntiFakeCode(dto.getAntiFakeCode());
        }

        return repository.save(entity);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        Product product = repository.findById(id)
                .orElseThrow(() -> new DomainException("产品不存在"));
        deletionService.deleteProduct(product);
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
}
