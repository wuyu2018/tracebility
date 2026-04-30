package com.foodtraceability.entity;

import com.foodtraceability.exception.BusinessException;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "complaint")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Complaint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "security_code_id")
    private SecurityCode securityCode;

    @Column(name = "complaint_reason", length = 500)
    private String complaintReason;

    @Column(name = "complaint_time")
    private LocalDateTime complaintTime;

    @Column(name = "is_processed")
    private Boolean isProcessed = false;

    public static Complaint create(SecurityCode securityCode, String complaintReason) {
        validateComplaintReason(complaintReason);

        Complaint complaint = new Complaint();
        complaint.setSecurityCode(securityCode);
        complaint.setComplaintReason(complaintReason);
        complaint.setComplaintTime(LocalDateTime.now());
        complaint.setIsProcessed(false);
        return complaint;
    }

    public void updateReason(String newReason) {
        validateComplaintReason(newReason);
        this.complaintReason = newReason;
    }

    public void markAsProcessed() {
        this.isProcessed = true;
    }

    public boolean isProcessed() {
        return Boolean.TRUE.equals(this.isProcessed);
    }

    private static void validateComplaintReason(String complaintReason) {
        if (complaintReason == null || complaintReason.isBlank()) {
            throw new BusinessException("投诉原因不能为空");
        }
    }
}
