package com.foodtraceability.service.impl;

import com.foodtraceability.dto.ComplaintDTO;
import com.foodtraceability.dto.PurchaseInfoDTO;
import com.foodtraceability.entity.Complaint;
import com.foodtraceability.repository.ComplaintRepository;
import com.foodtraceability.service.ComplaintService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ComplaintServiceImpl implements ComplaintService {

    private final ComplaintRepository complaintRepository;

    public ComplaintServiceImpl(ComplaintRepository complaintRepository) {
        this.complaintRepository = complaintRepository;
    }

    @Override
    @Transactional
    public ComplaintDTO createComplaint(ComplaintDTO complaintDTO) {

        Complaint complaint = new Complaint();
        complaint.setProductId(complaintDTO.getProductId());
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
        dto.setProductId(complaint.getProductId());
        dto.setComplaintReason(complaint.getComplaintReason());
        dto.setComplaintTime(complaint.getComplaintTime());
        return dto;
    }
}