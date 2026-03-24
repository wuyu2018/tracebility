package com.foodtraceability.service.impl;

import com.foodtraceability.dto.InsertDataDTO.MaterialPurchaseDTO;
import com.foodtraceability.entity.MaterialPurchase;
import com.foodtraceability.repository.MaterialPurchaseRepository;
import com.foodtraceability.repository.ProductRepository;
import com.foodtraceability.service.MaterialPurchaseService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MaterialPurchaseServiceImpl implements MaterialPurchaseService {
    @Autowired
    private MaterialPurchaseRepository repository;

    @Autowired
    private ProductRepository productRepository;

    @Override
    @Transactional
    public MaterialPurchase createMaterialPurchase(MaterialPurchaseDTO dto) {
        if (!productRepository.existsByAntiFakeCode(dto.getAntiFakeCode())) {
            throw new IllegalArgumentException("防伪码不存在，请先录入产品信息");
        }
        MaterialPurchase entity = new MaterialPurchase();
        BeanUtils.copyProperties(dto, entity);
        return repository.save(entity);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}