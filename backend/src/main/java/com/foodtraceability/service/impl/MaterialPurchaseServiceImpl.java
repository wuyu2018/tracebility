package com.foodtraceability.service.impl;

import com.foodtraceability.dto.InsertDataDTO.MaterialPurchaseDTO;
import com.foodtraceability.entity.MaterialPurchase;
import com.foodtraceability.repository.MaterialPurchaseRepository;
import com.foodtraceability.service.MaterialPurchaseService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MaterialPurchaseServiceImpl implements MaterialPurchaseService {
    @Autowired
    private MaterialPurchaseRepository repository;

    @Override
    @Transactional
    public MaterialPurchase createMaterialPurchase(MaterialPurchaseDTO dto) {
        MaterialPurchase entity = new MaterialPurchase();
        BeanUtils.copyProperties(dto, entity);
        return repository.save(entity);
    }
}