package com.foodtraceability.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class ComplaintDTO {
    private Long id;

    @NotNull(message = "产品ID不能为空")
    private Long productId;

    @NotBlank(message = "投诉原因不能为空")
    private String complaintReason;

    private LocalDateTime complaintTime;

    public ComplaintDTO() {}

    public ComplaintDTO(Long id, Long productId, String complaintReason, LocalDateTime complaintTime) {
        this.id = id;
        this.productId = productId;
        this.complaintReason = complaintReason;
        this.complaintTime = complaintTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getComplaintReason() {
        return complaintReason;
    }

    public void setComplaintReason(String complaintReason) {
        this.complaintReason = complaintReason;
    }

    public LocalDateTime getComplaintTime() {
        return complaintTime;
    }

    public void setComplaintTime(LocalDateTime complaintTime) {
        this.complaintTime = complaintTime;
    }
}