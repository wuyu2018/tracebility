package com.foodtraceability.service.impl;

import com.foodtraceability.dto.SecurityCodeDTO;
import com.foodtraceability.dto.SecurityCodeGenerateResponse;
import com.foodtraceability.entity.ProductionBatch;
import com.foodtraceability.entity.SecurityCode;
import com.foodtraceability.repository.ProductionBatchRepository;
import com.foodtraceability.repository.SecurityCodeRepository;
import com.foodtraceability.service.SecurityCodeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class SecurityCodeServiceImpl implements SecurityCodeService {
    @Autowired
    private SecurityCodeRepository codeRepository;

    @Autowired
    private ProductionBatchRepository batchRepository;

    @Override
    @Transactional
    public SecurityCodeGenerateResponse generateCodes(Long batchId, Integer quantity) {
        ProductionBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new RuntimeException("生产批次不存在"));

        List<String> codes = new ArrayList<>();

        for (int i = 0; i < quantity; i++) {
            String code = generateUniqueCode();
            SecurityCode securityCode = new SecurityCode();
            securityCode.setCode(code);
            securityCode.setBatch(batch);
            securityCode.setStatus("未激活");
            securityCode.setScanCount(0);
            codeRepository.save(securityCode);
            codes.add(code);
        }

        SecurityCodeGenerateResponse response = new SecurityCodeGenerateResponse();
        response.setCodes(codes);
        response.setCount(codes.size());
        return response;
    }

    @Override
    public List<SecurityCodeDTO> getCodesByBatchId(Long batchId) {
        return codeRepository.findByBatchId(batchId).stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    public SecurityCodeDTO getCodeByCode(String code) {
        return codeRepository.findByCode(code)
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("防伪码不存在"));
    }

    @Override
    public List<SecurityCodeDTO> exportCodes(Long batchId) {
        return getCodesByBatchId(batchId);
    }

    private String generateUniqueCode() {
        String snowflake = String.valueOf(System.currentTimeMillis());
        String random = String.format("%04d", (int) (Math.random() * 10000));
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return "SC" + snowflake + random + uuid;
    }

    private SecurityCodeDTO toDTO(SecurityCode entity) {
        SecurityCodeDTO dto = new SecurityCodeDTO();
        dto.setId(entity.getId());
        dto.setCode(entity.getCode());
        dto.setBatchId(entity.getBatch().getId());
        dto.setBatchNumber(entity.getBatch().getBatchNumber());
        dto.setStatus(entity.getStatus());
        dto.setFirstScanTime(entity.getFirstScanTime());
        dto.setScanCount(entity.getScanCount());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }
}