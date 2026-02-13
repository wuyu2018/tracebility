package com.foodtraceability.service.impl;

import com.foodtraceability.dto.InsertDataDTO.ProductDTO;
import com.foodtraceability.entity.Product;
import com.foodtraceability.repository.ProductRepository;
import com.foodtraceability.service.ProductService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductServiceImpl implements ProductService {
    @Autowired
    private ProductRepository repository;

    @Override
    @Transactional
    public Product createProduct(ProductDTO dto) {
        // 防伪码唯一性校验
        if (repository.existsByAntiFakeCode(dto.getAntiFakeCode())) {
            throw new RuntimeException("防伪码已存在，请勿重复录入！");
        }
        Product entity = new Product();
        BeanUtils.copyProperties(dto, entity);
        return repository.save(entity);
    }
}