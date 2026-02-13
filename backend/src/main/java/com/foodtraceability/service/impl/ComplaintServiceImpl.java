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
        // 创建新的投诉实体
        Complaint complaint = new Complaint();
        complaint.setProductId(complaintDTO.getProductId());
        complaint.setComplaintReason(complaintDTO.getComplaintReason());
        complaint.setComplaintTime(LocalDateTime.now());

        // 保存到数据库
        Complaint savedComplaint = complaintRepository.save(complaint);

        // 转换为DTO返回
        return convertToDTO(savedComplaint);
    }

    @Override
    @Transactional
    public ComplaintDTO updateComplaintReason(Long id, String complaintReason ) {
        // 查找现有的投诉记录
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("投诉记录不存在，ID: " + id));

        // 更新投诉信息
        complaint.setComplaintReason(complaintReason);
        complaint.setComplaintTime(LocalDateTime.now());

        // 保存更新
        Complaint updatedComplaint = complaintRepository.save(complaint);

        // 转换为DTO返回
        return convertToDTO(updatedComplaint);
    }

    /**
     * 将实体对象转换为DTO对象
     */
    private ComplaintDTO convertToDTO(Complaint complaint) {
        ComplaintDTO dto = new ComplaintDTO();
        dto.setId(complaint.getId());
        dto.setProductId(complaint.getProductId());
        dto.setComplaintReason(complaint.getComplaintReason());
        dto.setComplaintTime(complaint.getComplaintTime());
        return dto;
    }
}