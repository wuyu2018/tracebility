package com.foodtraceability.service.impl;

import com.foodtraceability.domain.DomainException;
import com.foodtraceability.dto.ComplaintDTO;
import com.foodtraceability.entity.Complaint;
import com.foodtraceability.repository.ComplaintRepository;
import com.foodtraceability.repository.SecurityCodeRepository;
import com.foodtraceability.service.ComplaintService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ComplaintApplicationService implements ComplaintService {

    private static final Logger log = LoggerFactory.getLogger(ComplaintApplicationService.class);

    private final ComplaintRepository complaintRepository;
    private final SecurityCodeRepository securityCodeRepository;

    @Autowired
    public ComplaintApplicationService(ComplaintRepository complaintRepository) {
        this.complaintRepository = complaintRepository;
        this.securityCodeRepository = securityCodeRepository;
    }

    @Override
    @Transactional
    public ComplaintDTO createComplaint(ComplaintDTO complaintDTO) {
        if (complaintDTO == null) {
            throw new DomainException("投诉信息不能为空");
        }

        SecurityCode securityCode = securityCodeRepository.findByCode(complaintDTO.getAntiFakeCode())
                .orElseThrow(() -> new BusinessException("防伪码不存在: " + complaintDTO.getAntiFakeCode()));

        Complaint complaint = Complaint.create(securityCode, complaintDTO.getComplaintReason());

        if (complaintDTO.getAntiFakeCode() != null || complaintDTO.getBatchNumber() != null) {
            String codeOrBatch = complaintDTO.getAntiFakeCode() != null
                    ? complaintDTO.getAntiFakeCode()
                    : complaintDTO.getBatchNumber();
            complaint.linkToProduct(codeOrBatch);
        }

        Complaint savedComplaint = complaintRepository.save(complaint);
        log.info("[投诉创建] 投诉已保存 - ID: {}, 防伪码: {}",
                savedComplaint.getId(), complaintDTO.getAntiFakeCode());

        return toDTO(savedComplaint);
    }

    @Override
    @Transactional
    public ComplaintDTO updateComplaintReason(Long id, String complaintReason) {
        if (id == null || id <= 0) {
            throw new DomainException("投诉ID不能为空或无效");
        }

        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new DomainException("投诉记录不存在"));

        complaint.updateReason(complaintReason);
        Complaint updatedComplaint = complaintRepository.save(complaint);

        log.info("[投诉更新] 投诉原因已更新 - ID: {}", updatedComplaint.getId());

        return toDTO(updatedComplaint);
    }

    private ComplaintDTO toDTO(Complaint complaint) {
        ComplaintDTO dto = new ComplaintDTO();
        dto.setId(complaint.getId());
        dto.setComplaintReason(complaint.getComplaintReason());
        dto.setComplaintTime(complaint.getComplaintTime());
        dto.setBatchNumber(complaint.getBatchNumber());
        dto.setAntiFakeCode(complaint.getAntiFakeCode());
        dto.setIsProcessed(complaint.isProcessed());
        return dto;
    }
}
