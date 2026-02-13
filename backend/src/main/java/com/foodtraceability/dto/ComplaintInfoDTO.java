package com.foodtraceability.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;

public class ComplaintInfoDTO {

    private List<ComplaintInfo> complaintInfos;

    public ComplaintInfoDTO() {}

    public ComplaintInfoDTO(List<ComplaintInfo> complaintInfos) {
        this.complaintInfos = complaintInfos;
    }

    public List<ComplaintInfo> getComplaintInfos() {
        return complaintInfos;
    }

    public void setComplaintInfos(List<ComplaintInfo> complaintInfos) {
        this.complaintInfos = complaintInfos;
    }

    public static class ComplaintInfo {
        private Long id;

        @JsonProperty("productId")
        private Long productId;

        private String complaintReason;
        private LocalDateTime complaintTime;

        public ComplaintInfo() {}

        public ComplaintInfo(Long id, Long productId, String complaintReason, LocalDateTime complaintTime) {
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

        public Long getProductId() {  // 注意：P大写
            return productId;
        }

        public void setProductId(Long productId) {  // 注意：P大写
            this.productId = productId;  // 修复：正确的赋值
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
}