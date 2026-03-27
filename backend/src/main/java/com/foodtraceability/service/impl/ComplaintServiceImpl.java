package com.foodtraceability.service.impl;

import com.foodtraceability.dto.ComplaintDTO;
import com.foodtraceability.entity.Complaint;
import com.foodtraceability.entity.Product;
import com.foodtraceability.exception.BusinessException;
import com.foodtraceability.repository.ComplaintRepository;
import com.foodtraceability.service.ComplaintService;
import com.foodtraceability.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ComplaintServiceImpl implements ComplaintService {

    private static final Logger log = LoggerFactory.getLogger(ComplaintServiceImpl.class);

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private ProductService productService;

    @Override
    public ComplaintDTO createComplaint(ComplaintDTO complaintDTO) {
        if (complaintDTO == null) {
            throw new BusinessException("投诉信息不能为空");
        }

        if (complaintDTO.getProductId() == null) {
            throw new BusinessException("请选择要投诉的产品");
        }

        if (complaintDTO.getComplaintReason() == null || complaintDTO.getComplaintReason().isBlank()) {
            throw new BusinessException("投诉原因不能为空");
        }

        try {
            Product product = productService.getProductById(complaintDTO.getProductId());

            Complaint complaint = new Complaint();
            complaint.setProductName(product.getName());
            complaint.setAntiFakeCode(product.getAntiFakeCode());
            complaint.setComplaintReason(complaintDTO.getComplaintReason());
            complaint.setComplaintTime(LocalDateTime.now());

            Complaint savedComplaint = complaintRepository.save(complaint);
            log.info("[投诉创建] 投诉已保存 - ID: {}, 产品: {}",
                savedComplaint.getId(), savedComplaint.getProductName());

            ComplaintDTO resultDTO = new ComplaintDTO();
            resultDTO.setId(savedComplaint.getId());
            resultDTO.setProductId(complaintDTO.getProductId());
            resultDTO.setProductName(savedComplaint.getProductName());
            resultDTO.setAntiFakeCode(savedComplaint.getAntiFakeCode());
            resultDTO.setComplaintReason(savedComplaint.getComplaintReason());
            resultDTO.setComplaintTime(savedComplaint.getComplaintTime());

            return resultDTO;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("[投诉创建] 保存投诉失败 - 产品ID: {}, 错误: {}",
                complaintDTO.getProductId(), e.getMessage(), e);
            throw new BusinessException("投诉保存失败，请稍后重试");
        }
    }

    @Override
    public ComplaintDTO updateComplaintReason(Long id, String complaintReason) {
        if (id == null || id <= 0) {
            throw new BusinessException("投诉ID不能为空或无效");
        }

        if (complaintReason == null || complaintReason.isBlank()) {
            throw new BusinessException("投诉原因不能为空");
        }

        try {
            Optional<Complaint> complaintOptional = complaintRepository.findById(id);
            if (!complaintOptional.isPresent()) {
                throw new BusinessException("投诉记录不存在");
            }

            Complaint complaint = complaintOptional.get();
            complaint.setComplaintReason(complaintReason);

            Complaint updatedComplaint = complaintRepository.save(complaint);
            log.info("[投诉更新] 投诉原因已更新 - ID: {}, 产品: {}",
                updatedComplaint.getId(), updatedComplaint.getProductName());

            ComplaintDTO resultDTO = new ComplaintDTO();
            resultDTO.setId(updatedComplaint.getId());
            resultDTO.setProductName(updatedComplaint.getProductName());
            resultDTO.setComplaintReason(updatedComplaint.getComplaintReason());
            resultDTO.setComplaintTime(updatedComplaint.getComplaintTime());

            return resultDTO;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("[投诉更新] 更新投诉失败 - ID: {}, 错误: {}", id, e.getMessage(), e);
            throw new BusinessException("投诉更新失败，请稍后重试");
        }
    }
}
