package com.foodtraceability.service.impl;

import com.foodtraceability.dto.ComplaintDTO;
import com.foodtraceability.entity.Complaint;
import com.foodtraceability.entity.SecurityCode;
import com.foodtraceability.repository.ComplaintRepository;
import com.foodtraceability.repository.SecurityCodeRepository;
import com.foodtraceability.service.ComplaintService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ComplaintServiceImpl implements ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final SecurityCodeRepository securityCodeRepository;

    public ComplaintServiceImpl(ComplaintRepository complaintRepository,
                                SecurityCodeRepository securityCodeRepository) {
        this.complaintRepository = complaintRepository;
        this.securityCodeRepository = securityCodeRepository;
    }

    @Override
    @Transactional
    public ComplaintDTO createComplaint(ComplaintDTO complaintDTO) {
        Complaint complaint = new Complaint();
        complaint.setAntiFakeCode(complaintDTO.getAntiFakeCode());
        complaint.setComplaintReason(complaintDTO.getComplaintReason());
        complaint.setComplaintTime(LocalDateTime.now());

        String productName = null;
        String batchNumber = null;

        if (complaintDTO.getAntiFakeCode() != null && !complaintDTO.getAntiFakeCode().isEmpty()) {
            var securityCodeOpt = securityCodeRepository.findByCode(complaintDTO.getAntiFakeCode());
            if (securityCodeOpt.isPresent()) {
                SecurityCode securityCode = securityCodeOpt.get();
                productName = securityCode.getBatch().getProduct().getName();
                batchNumber = securityCode.getBatch().getBatchNumber();
            }
        }

        complaint.setProductName(productName);
        complaint.setBatchNumber(batchNumber);

        Complaint savedComplaint = complaintRepository.save(complaint);

        ComplaintDTO result = new ComplaintDTO();
        result.setId(savedComplaint.getId());
        result.setAntiFakeCode(savedComplaint.getAntiFakeCode());
        result.setComplaintReason(savedComplaint.getComplaintReason());
        result.setComplaintTime(savedComplaint.getComplaintTime());
        result.setProductName(savedComplaint.getProductName());
        result.setBatchNumber(savedComplaint.getBatchNumber());

        return result;
    }

    @Override
    @Transactional
    public ComplaintDTO updateComplaintReason(Long id, String complaintReason) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("投诉不存在"));
        complaint.setComplaintReason(complaintReason);
        Complaint updated = complaintRepository.save(complaint);

        ComplaintDTO dto = new ComplaintDTO();
        dto.setId(updated.getId());
        dto.setAntiFakeCode(updated.getAntiFakeCode());
        dto.setComplaintReason(updated.getComplaintReason());
        dto.setComplaintTime(updated.getComplaintTime());
        dto.setProductName(updated.getProductName());
        dto.setBatchNumber(updated.getBatchNumber());
        return dto;
    }
}
