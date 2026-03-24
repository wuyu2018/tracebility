package com.foodtraceability.service.impl;

import com.foodtraceability.dto.InsertDataDTO.StorageDTO;
import com.foodtraceability.entity.Storage;
import com.foodtraceability.repository.StorageRepository;
import com.foodtraceability.repository.ProductRepository;
import com.foodtraceability.service.StorageService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StorageServiceImpl implements StorageService {
    @Autowired
    private StorageRepository repository;

    @Autowired
    private ProductRepository productRepository;

    @Override
    @Transactional
    public Storage createStorage(StorageDTO dto) {
        if (!productRepository.existsByAntiFakeCode(dto.getAntiFakeCode())) {
            throw new IllegalArgumentException("防伪码不存在，请先录入产品信息");
        }
        Storage entity = new Storage();
        BeanUtils.copyProperties(dto, entity);
        return repository.save(entity);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}