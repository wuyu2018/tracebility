package com.foodtraceability.service.impl;

import com.foodtraceability.dto.ProductDTO;
import com.foodtraceability.entity.Product;
import com.foodtraceability.repository.ProductRepository;
import com.foodtraceability.service.ProductService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ProductServiceImpl implements ProductService {
    @Autowired
    private ProductRepository repository;

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
        // 只清除防伪码和二维码，不删除产品
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
    public Optional<Product> getProductByAntiFakeCode(String antiFakeCode) {
        return repository.findByAntiFakeCode(antiFakeCode);
    }
}