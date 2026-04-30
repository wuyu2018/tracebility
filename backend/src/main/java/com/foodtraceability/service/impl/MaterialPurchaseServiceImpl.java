package com.foodtraceability.service.impl;

import com.foodtraceability.dto.MaterialPurchaseDTO;
import com.foodtraceability.entity.Material;
import com.foodtraceability.entity.MaterialPurchase;
import com.foodtraceability.exception.BusinessException;
import com.foodtraceability.repository.MaterialPurchaseRepository;
import com.foodtraceability.repository.MaterialRepository;
import com.foodtraceability.service.MaterialPurchaseService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MaterialPurchaseServiceImpl implements MaterialPurchaseService {
    private final MaterialPurchaseRepository repository;
    private final MaterialRepository materialRepository;

    public MaterialPurchaseServiceImpl(MaterialPurchaseRepository repository,
                                      MaterialRepository materialRepository) {
        this.repository = repository;
        this.materialRepository = materialRepository;
    }

    @Override
    @Transactional
    public MaterialPurchase createMaterialPurchase(MaterialPurchaseDTO dto) {
        Material material = materialRepository.findById(dto.getMaterialId())
                .orElseThrow(() -> new BusinessException("原料品种不存在"));

        MaterialPurchase entity = new MaterialPurchase();
        entity.setMaterial(material);
        BeanUtils.copyProperties(dto, entity);
        entity.setIsDeleted(false);
        return repository.save(entity);
    }

    @Override
    @Transactional
    public MaterialPurchase updateMaterialPurchase(Long id, MaterialPurchaseDTO dto) {
        MaterialPurchase entity = repository.findById(id)
                .orElseThrow(() -> new BusinessException("原料采购记录不存在"));

        if (dto.getMaterialId() != null) {
            Material material = materialRepository.findById(dto.getMaterialId())
                    .orElseThrow(() -> new BusinessException("原料品种不存在"));
            entity.setMaterial(material);
        }
        BeanUtils.copyProperties(dto, entity);
        return repository.save(entity);
    }

    @Override
    @Transactional
    public void deleteMaterialPurchase(Long id) {
        MaterialPurchase entity = repository.findById(id)
                .orElseThrow(() -> new BusinessException("原料采购记录不存在"));
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
    public List<MaterialPurchase> getMaterialPurchasesByMaterialId(Long materialId) {
        return repository.findByMaterialIdAndIsDeletedFalse(materialId);
    }
}
