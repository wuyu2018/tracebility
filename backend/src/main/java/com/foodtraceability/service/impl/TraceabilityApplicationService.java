package com.foodtraceability.service.impl;

import com.foodtraceability.domain.DomainException;
import com.foodtraceability.domain.valueobject.TraceInfo;
import com.foodtraceability.dto.TraceInfoDTO;
import com.foodtraceability.dto.TraceInfoConverter;
import com.foodtraceability.entity.SecurityCode;
import com.foodtraceability.repository.SecurityCodeRepository;
import com.foodtraceability.service.TraceabilityService;
import com.foodtraceability.service.domain.TraceabilityDomainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class TraceabilityApplicationService implements TraceabilityService {

    private final SecurityCodeRepository securityCodeRepository;
    private final TraceabilityDomainService traceabilityDomainService;

    @Autowired
    public TraceabilityApplicationService(
            SecurityCodeRepository securityCodeRepository,
            TraceabilityDomainService traceabilityDomainService) {
        this.securityCodeRepository = securityCodeRepository;
        this.traceabilityDomainService = traceabilityDomainService;
    }

    @Override
    @Transactional
    public Optional<TraceInfoDTO> getTraceInfoByCode(String code) {
        Optional<SecurityCode> securityCodeOpt = securityCodeRepository.findByCode(code);

        if (securityCodeOpt.isEmpty()) {
            return Optional.empty();
        }

        SecurityCode securityCode = securityCodeOpt.get();
        securityCode.recordQueryAndActivateIfNeeded();
        securityCodeRepository.save(securityCode);

        TraceInfo traceInfo = traceabilityDomainService.buildFullTraceInfo(code, false);

        TraceInfoDTO dto = TraceInfoConverter.toDTO(
            traceInfo,
            securityCode.getStatus(),
            securityCode.getFirstScanTime(),
            securityCode.getQueryCount(),
            false
        );

        return Optional.of(dto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TraceInfoDTO> getTraceInfoByBatchNumber(String batchNumber) {
        TraceInfo traceInfo = traceabilityDomainService.buildTraceInfoByBatchNumber(batchNumber, false);

        TraceInfoDTO dto = TraceInfoConverter.toDTO(
            traceInfo,
            "未扫码",
            null,
            0,
            false
        );

        return Optional.of(dto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TraceInfoDTO> getTraceInfoByCodeForAdmin(String code) {
        Optional<SecurityCode> securityCodeOpt = securityCodeRepository.findByCode(code);

        if (securityCodeOpt.isEmpty()) {
            return Optional.empty();
        }

        SecurityCode securityCode = securityCodeOpt.get();
        TraceInfo traceInfo = traceabilityDomainService.buildFullTraceInfo(code, true);

        TraceInfoDTO dto = TraceInfoConverter.toDTO(
            traceInfo,
            securityCode.getStatus(),
            securityCode.getFirstScanTime(),
            securityCode.getQueryCount(),
            true
        );

        return Optional.of(dto);
    }
}
