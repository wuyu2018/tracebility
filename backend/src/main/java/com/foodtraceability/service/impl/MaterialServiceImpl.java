package com.foodtraceability.service.impl;

import com.foodtraceability.dto.MaterialDTO;
import com.foodtraceability.entity.Material;
import com.foodtraceability.exception.BusinessException;
import com.foodtraceability.policy.DeletionPolicy;
import com.foodtraceability.repository.MaterialRepository;
import com.foodtraceability.service.MaterialService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MaterialServiceImpl implements MaterialService {
    private static final Logger log = LoggerFactory.getLogger(MaterialServiceImpl.class);

    private final MaterialRepository repository;
    private final DeletionPolicy deletionPolicy;

    public MaterialServiceImpl(MaterialRepository repository, DeletionPolicy deletionPolicy) {
        this.repository = repository;
        this.deletionPolicy = deletionPolicy;
    }

    @Override
    @Transactional
    public Material createMaterial(MaterialDTO dto) {
        if (repository.existsByName(dto.getName())) {
            throw new BusinessException("原料品种已存在: " + dto.getName());
        }
        Material entity = new Material();
        BeanUtils.copyProperties(dto, entity);
        entity.setIsActive(true);
        Material saved = repository.save(entity);
        log.info("[原料品种] 创建 - ID: {}, 名称: {}", saved.getId(), saved.getName());
        return saved;
    }

    @Override
    @Transactional
    public Material updateMaterial(Long id, MaterialDTO dto) {
        Material entity = repository.findById(id)
                .orElseThrow(() -> new BusinessException("原料品种不存在: " + id));
        entity.changeName(dto.getName());
        entity.setActiveStatus(dto.getIsActive());
        Material saved = repository.save(entity);
        log.info("[原料品种] 更新 - ID: {}, 名称: {}", saved.getId(), saved.getName());
        return saved;
    }

    @Override
    @Transactional
    public void deactivateMaterial(Long id) {
        Material entity = repository.findById(id)
                .orElseThrow(() -> new BusinessException("原料品种不存在: " + id));
        entity.deactivate();
        repository.save(entity);
        log.info("[原料品种] 停用 - ID: {}", id);
    }

    @Override
    @Transactional
    public void activateMaterial(Long id) {
        Material entity = repository.findById(id)
                .orElseThrow(() -> new BusinessException("原料品种不存在: " + id));
        entity.activate();
        repository.save(entity);
        log.info("[原料品种] 启用 - ID: {}", id);
    }

    @Override
    @Transactional
    public void deleteMaterial(Long id) {
        Material entity = repository.findById(id)
                .orElseThrow(() -> new BusinessException("原料品种不存在: " + id));
        deletionPolicy.deleteMaterial(entity);
        log.info("[原料品种] 删除 - ID: {}", id);
    }

    @Override
    public Material getMaterialById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new BusinessException("原料品种不存在: " + id));
    }

    @Override
    public List<Material> listAllActiveMaterials() {
        return repository.findByIsActiveTrue();
    }

    @Override
    public List<Material> listAllMaterials() {
        return repository.findAll();
    }
}
