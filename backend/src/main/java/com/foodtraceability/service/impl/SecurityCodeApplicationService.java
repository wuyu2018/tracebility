package com.foodtraceability.service.impl;

import com.foodtraceability.domain.DomainException;
import com.foodtraceability.dto.SecurityCodeDTO;
import com.foodtraceability.dto.SecurityCodeGenerateResponse;
import com.foodtraceability.entity.Product;
import com.foodtraceability.entity.ProductionBatch;
import com.foodtraceability.entity.SecurityCode;
import com.foodtraceability.repository.ProductRepository;
import com.foodtraceability.repository.ProductionBatchRepository;
import com.foodtraceability.repository.SecurityCodeRepository;
import com.foodtraceability.service.SecurityCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SecurityCodeApplicationService implements SecurityCodeService {

    private final SecurityCodeRepository codeRepository;
    private final ProductionBatchRepository batchRepository;
    private final ProductRepository productRepository;

    @Autowired
    public SecurityCodeApplicationService(
            SecurityCodeRepository codeRepository,
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
                .orElseThrow(() -> new DomainException("生产批次不存在"));

        List<SecurityCode> codes = new ArrayList<>();
        for (int i = 0; i < quantity; i++) {
            SecurityCode code = SecurityCode.create(batch);
            codes.add(codeRepository.save(code));
        }

        if (!codes.isEmpty()) {
            Product product = batch.getProduct();
            SecurityCode firstCode = codes.get(0);
            product.assignQrCode(firstCode.getCode(), "/qrcode/" + product.getId());
            productRepository.save(product);
        }

        SecurityCodeGenerateResponse response = new SecurityCodeGenerateResponse();
        response.setCodes(codes.stream().map(SecurityCode::getCode).collect(Collectors.toList()));
        response.setCount(codes.size());

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SecurityCodeDTO> getCodesByBatchId(Long batchId) {
        List<SecurityCode> codes = codeRepository.findByBatchId(batchId);
        return codes.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SecurityCodeDTO getCodeByCode(String code) {
        SecurityCode securityCode = codeRepository.findByCode(code)
                .orElseThrow(() -> new DomainException("防伪码不存在"));
        return toDTO(securityCode);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SecurityCodeDTO> exportCodes(Long batchId) {
        return getCodesByBatchId(batchId);
    }

    private SecurityCodeDTO toDTO(SecurityCode securityCode) {
        SecurityCodeDTO dto = new SecurityCodeDTO();
        dto.setId(securityCode.getId());
        dto.setCode(securityCode.getCode());
        dto.setBatchId(securityCode.getBatch().getId());
        dto.setBatchNumber(securityCode.getBatch().getBatchNumber());
        dto.setStatus(securityCode.getStatus());
        dto.setFirstScanTime(securityCode.getFirstScanTime());
        dto.setScanCount(securityCode.getScanCount());
        dto.setCreatedAt(securityCode.getCreatedAt());
        return dto;
    }
}
