package com.foodtraceability.service.impl;

import com.foodtraceability.dto.ComplaintDTO;
import com.foodtraceability.entity.Complaint;
import com.foodtraceability.repository.ComplaintRepository;
import com.foodtraceability.repository.ProductRepository;
import com.foodtraceability.service.ComplaintService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ComplaintServiceImpl implements ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final ProductRepository productRepository;

    public ComplaintServiceImpl(ComplaintRepository complaintRepository, ProductRepository productRepository) {
        this.complaintRepository = complaintRepository;
        this.productRepository = productRepository;
    }

    @Override
    @Transactional
    public ComplaintDTO createComplaint(ComplaintDTO complaintDTO) {
        if (!productRepository.existsByAntiFakeCode(complaintDTO.getAntiFakeCode())) {
            throw new IllegalArgumentException("防伪码不存在，请先录入产品信息");
        }

        Complaint complaint = new Complaint();
        complaint.setAntiFakeCode(complaintDTO.getAntiFakeCode());
        complaint.setComplaintReason(complaintDTO.getComplaintReason());
        complaint.setComplaintTime(LocalDateTime.now());

        Complaint savedComplaint = complaintRepository.save(complaint);

        return convertToDTO(savedComplaint);
    }

    @Override
    @Transactional
    public ComplaintDTO updateComplaintReason(Long id, String complaintReason ) {

        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("投诉记录不存在，ID: " + id));

        complaint.setComplaintReason(complaintReason);
        complaint.setComplaintTime(LocalDateTime.now());

        Complaint updatedComplaint = complaintRepository.save(complaint);

        return convertToDTO(updatedComplaint);
    }

    private ComplaintDTO convertToDTO(Complaint complaint) {
        ComplaintDTO dto = new ComplaintDTO();
        dto.setId(complaint.getId());
        dto.setAntiFakeCode(complaint.getAntiFakeCode());
        dto.setComplaintReason(complaint.getComplaintReason());
        dto.setComplaintTime(complaint.getComplaintTime());
        return dto;
    }
}