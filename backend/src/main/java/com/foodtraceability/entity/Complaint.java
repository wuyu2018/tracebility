package com.foodtraceability.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "complaint")
public class Complaint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "complaint_reason", length = 500)
    private String complaintReason;

    @Column(name = "complaint_time")
    private LocalDateTime complaintTime;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getComplaintReason() { return complaintReason; }
    public void setComplaintReason(String complaintReason) { this.complaintReason = complaintReason; }
    public LocalDateTime getComplaintTime() { return complaintTime; }
    public void setComplaintTime(LocalDateTime complaintTime) { this.complaintTime = complaintTime; }
}
