package com.foodtraceability.service.impl;

import com.foodtraceability.dto.SecurityCodeDTO;
import com.foodtraceability.dto.SecurityCodeGenerateResponse;
import com.foodtraceability.entity.ProductionBatch;
import com.foodtraceability.entity.Product;
import com.foodtraceability.entity.SecurityCode;
import com.foodtraceability.repository.ProductionBatchRepository;
import com.foodtraceability.repository.ProductRepository;
import com.foodtraceability.repository.SecurityCodeRepository;
import com.foodtraceability.service.SecurityCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class SecurityCodeServiceImpl implements SecurityCodeService {
    private final SecurityCodeRepository codeRepository;
    private final ProductionBatchRepository batchRepository;
    private final ProductRepository productRepository;

    @Autowired
    public SecurityCodeServiceImpl(SecurityCodeRepository codeRepository,
                                 ProductionBatchRepository batchRepository,
                                 ProductRepository productRepository) {
        this.codeRepository = codeRepository;
        this.batchRepository = batchRepository;
        this.productRepository = productRepository;
    }

    @Override
    @Transactional
    public SecurityCodeGenerateResponse generateCodes(Long batchId, Integer quantity) {
        ProductionBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new RuntimeException("生产批次不存在"));

        List<String> codes = generateCodesForBatch(batch, quantity);
        assignFirstCodeToProduct(batch, codes);

        SecurityCodeGenerateResponse response = new SecurityCodeGenerateResponse();
        response.setCodes(codes);
        response.setCount(codes.size());
        return response;
    }

    private List<String> generateCodesForBatch(ProductionBatch batch, Integer quantity) {
        List<String> codes = new ArrayList<>();
        for (int i = 0; i < quantity; i++) {
            SecurityCode securityCode = SecurityCode.create(batch);
            codeRepository.save(securityCode);
            codes.add(securityCode.getCode());
        }
        return codes;
    }

    private void assignFirstCodeToProduct(ProductionBatch batch, List<String> codes) {
        if (codes.isEmpty()) {
            return;
        }
        Product product = batch.getProduct();
        if (product != null) {
            product.assignQrCode(codes.get(0), "/qrcode/" + product.getId());
            productRepository.save(product);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<SecurityCodeDTO> getCodesByBatchId(Long batchId) {
        return codeRepository.findByBatch_Id(batchId).stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SecurityCodeDTO getCodeByCode(String code) {
        return codeRepository.findByCode(code)
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("防伪码不存在"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SecurityCodeDTO> exportCodes(Long batchId) {
        return getCodesByBatchId(batchId);
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