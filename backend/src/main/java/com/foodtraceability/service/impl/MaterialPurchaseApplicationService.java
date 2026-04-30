package com.foodtraceability.service.impl;

import com.foodtraceability.domain.DomainException;
import com.foodtraceability.dto.MaterialPurchaseDTO;
import com.foodtraceability.entity.Material;
import com.foodtraceability.entity.MaterialPurchase;
import com.foodtraceability.repository.MaterialPurchaseRepository;
import com.foodtraceability.repository.MaterialRepository;
import com.foodtraceability.service.MaterialPurchaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MaterialPurchaseApplicationService implements MaterialPurchaseService {

    private final MaterialPurchaseRepository repository;
    private final MaterialRepository materialRepository;

    @Autowired
    public MaterialPurchaseApplicationService(
            MaterialPurchaseRepository repository,
            MaterialRepository materialRepository) {
        this.repository = repository;
        this.materialRepository = materialRepository;
    }

    @Override
    @Transactional
    public MaterialPurchase createMaterialPurchase(MaterialPurchaseDTO dto) {
        Material material = materialRepository.findById(dto.getMaterialId())
                .orElseThrow(() -> new DomainException("原料品种不存在"));

        MaterialPurchase entity = new MaterialPurchase();
        entity.setMaterial(material);
        entity.setBatchNumber(dto.getBatchNumber());
        entity.setSupplierName(dto.getSupplierName());
        entity.setProducerName(dto.getProducerName());
        entity.setProducerAddress(dto.getProducerAddress());
        entity.setPurchaseDate(dto.getPurchaseDate());
        entity.setQuantity(dto.getQuantity());
        entity.setUnit(dto.getUnit());
        entity.setIsDeleted(false);
        return repository.save(entity);
    }

    @Override
    @Transactional
    public MaterialPurchase updateMaterialPurchase(Long id, MaterialPurchaseDTO dto) {
        MaterialPurchase entity = repository.findById(id)
                .orElseThrow(() -> new DomainException("原材料采购记录不存在"));

        if (dto.getMaterialId() != null) {
            Material material = materialRepository.findById(dto.getMaterialId())
                    .orElseThrow(() -> new DomainException("原料品种不存在"));
            entity.setMaterial(material);
        }
        if (dto.getBatchNumber() != null) entity.setBatchNumber(dto.getBatchNumber());
        if (dto.getSupplierName() != null) entity.setSupplierName(dto.getSupplierName());
        if (dto.getProducerName() != null) entity.setProducerName(dto.getProducerName());
        if (dto.getProducerAddress() != null) entity.setProducerAddress(dto.getProducerAddress());
        if (dto.getPurchaseDate() != null) entity.setPurchaseDate(dto.getPurchaseDate());
        if (dto.getQuantity() != null) entity.setQuantity(dto.getQuantity());
        if (dto.getUnit() != null) entity.setUnit(dto.getUnit());

        return repository.save(entity);
    }

    @Override
    @Transactional
    public void deleteMaterialPurchase(Long id) {
        MaterialPurchase entity = repository.findById(id)
                .orElseThrow(() -> new DomainException("原材料采购记录不存在"));
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
