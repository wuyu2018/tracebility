package com.foodtraceability.service.impl;

import com.foodtraceability.dto.InsertDataDTO.InspectionDTO;
import com.foodtraceability.entity.Inspection;
import com.foodtraceability.repository.InspectionRepository;
import com.foodtraceability.service.InspectionService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InspectionServiceImpl implements InspectionService {
    @Autowired
    private InspectionRepository repository;

    @Override
    @Transactional
    public Inspection createInspection(InspectionDTO dto) {
        Inspection entity = new Inspection();
        BeanUtils.copyProperties(dto, entity);
        return repository.save(entity);
    }
}