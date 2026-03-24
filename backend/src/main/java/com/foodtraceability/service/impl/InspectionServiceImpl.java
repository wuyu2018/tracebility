package com.foodtraceability.service.impl;

import com.foodtraceability.dto.InsertDataDTO.InspectionDTO;
import com.foodtraceability.entity.Inspection;
import com.foodtraceability.repository.InspectionRepository;
import com.foodtraceability.repository.ProductRepository;
import com.foodtraceability.service.InspectionService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InspectionServiceImpl implements InspectionService {
    @Autowired
    private InspectionRepository repository;

    @Autowired
    private ProductRepository productRepository;

    @Override
    @Transactional
    public Inspection createInspection(InspectionDTO dto) {
        if (!productRepository.existsByAntiFakeCode(dto.getAntiFakeCode())) {
            throw new IllegalArgumentException("防伪码不存在，请先录入产品信息");
        }
        Inspection entity = new Inspection();
        BeanUtils.copyProperties(dto, entity);
        return repository.save(entity);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
        Inspection entity = new Inspection();
        BeanUtils.copyProperties(dto, entity);
        return repository.save(entity);
    }
}