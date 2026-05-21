package com.foodtraceability.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodtraceability.agent.service.AgentBlockchainService;
import com.foodtraceability.dto.MaterialPurchaseDTO;
import com.foodtraceability.entity.Material;
import com.foodtraceability.entity.MaterialPurchase;
import com.foodtraceability.exception.BusinessException;
import com.foodtraceability.repository.MaterialPurchaseRepository;
import com.foodtraceability.repository.MaterialRepository;
import com.foodtraceability.service.MaterialPurchaseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class MaterialPurchaseServiceImpl implements MaterialPurchaseService {
    private static final Logger log = LoggerFactory.getLogger(MaterialPurchaseServiceImpl.class);

    private final MaterialPurchaseRepository repository;
    private final MaterialRepository materialRepository;
    private final AgentBlockchainService agentBlockchainService;
    private final ObjectMapper objectMapper;

    public MaterialPurchaseServiceImpl(MaterialPurchaseRepository repository,
                                      MaterialRepository materialRepository,
                                      AgentBlockchainService agentBlockchainService) {
        this.repository = repository;
        this.materialRepository = materialRepository;
        this.agentBlockchainService = agentBlockchainService;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    @Transactional
    public MaterialPurchase createMaterialPurchase(MaterialPurchaseDTO dto) {
        Material material = materialRepository.findById(dto.getMaterialId())
                .orElseThrow(() -> new BusinessException("原料品种不存在"));

        MaterialPurchase entity = MaterialPurchase.recordPurchase(
                material, dto.getBatchNumber(), dto.getSupplierName(),
                dto.getProducerName(), dto.getProducerAddress(),
                dto.getPurchaseDate(), dto.getQuantity(), dto.getUnit());
        entity = repository.save(entity);

        MaterialPurchase saved = entity;
        try {
            Map<String, Object> snapshot = new LinkedHashMap<>();
            snapshot.put("id", saved.getId());
            snapshot.put("materialId", saved.getMaterial().getId());
            snapshot.put("materialName", saved.getMaterialName());
            snapshot.put("batchNumber", saved.getBatchNumber());
            snapshot.put("supplierName", saved.getSupplierName());
            snapshot.put("producerName", saved.getProducerName());
            snapshot.put("producerAddress", saved.getProducerAddress());
            snapshot.put("purchaseDate", saved.getPurchaseDate() != null ? saved.getPurchaseDate().toString() : null);
            snapshot.put("quantity", saved.getQuantity());
            snapshot.put("unit", saved.getUnit());
            agentBlockchainService.appendBlockWithConsensus(
                    "MATERIAL", "MATERIAL_PURCHASE", saved.getId(), "CREATE",
                    objectMapper.writeValueAsString(snapshot), null);
        } catch (Exception e) {
            log.error("[Blockchain] Failed to append block for MaterialPurchase CREATE", e);
        }

        return saved;
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
        entity.updatePurchaseDetails(
                dto.getBatchNumber(), dto.getSupplierName(),
                dto.getProducerName(), dto.getProducerAddress(),
                dto.getPurchaseDate(), dto.getQuantity(), dto.getUnit());
        entity = repository.save(entity);

        try {
            Map<String, Object> snapshot = new LinkedHashMap<>();
            snapshot.put("id", entity.getId());
            snapshot.put("materialId", entity.getMaterial().getId());
            snapshot.put("materialName", entity.getMaterialName());
            snapshot.put("batchNumber", entity.getBatchNumber());
            snapshot.put("supplierName", entity.getSupplierName());
            snapshot.put("producerName", entity.getProducerName());
            snapshot.put("producerAddress", entity.getProducerAddress());
            snapshot.put("purchaseDate", entity.getPurchaseDate() != null ? entity.getPurchaseDate().toString() : null);
            snapshot.put("quantity", entity.getQuantity());
            snapshot.put("unit", entity.getUnit());
            agentBlockchainService.appendBlockWithConsensus(
                    "MATERIAL", "MATERIAL_PURCHASE", entity.getId(), "UPDATE",
                    objectMapper.writeValueAsString(snapshot), null);
        } catch (Exception e) {
            log.error("[Blockchain] Failed to append block for MaterialPurchase UPDATE", e);
        }

        return entity;
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
