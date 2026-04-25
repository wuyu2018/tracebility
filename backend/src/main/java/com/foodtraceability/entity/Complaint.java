package com.foodtraceability.entity;

import com.foodtraceability.exception.BusinessException;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
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

    @Column(name = "anti_fake_code", length = 64)
    private String antiFakeCode;

    @Column(name = "complaint_reason", length = 500)
    private String complaintReason;

    @Column(name = "complaint_time")
    private LocalDateTime complaintTime;

    @Column(name = "product_name", length = 100)
    private String productName;

    @Column(name = "batch_number", length = 50)
    private String batchNumber;

    public static Complaint create(String productName, String complaintReason) {
        validateProductName(productName);
        validateComplaintReason(complaintReason);

        Complaint complaint = new Complaint();
        complaint.setProductName(productName);
        complaint.setComplaintReason(complaintReason);
        complaint.setComplaintTime(LocalDateTime.now());
        return complaint;
    }

    public void updateReason(String newReason) {
        validateComplaintReason(newReason);
        this.complaintReason = newReason;
    }

    private static void validateProductName(String productName) {
        if (productName == null || productName.isBlank()) {
            throw new BusinessException("请选择要投诉的产品");
        }
    }

    private static void validateComplaintReason(String complaintReason) {
        if (complaintReason == null || complaintReason.isBlank()) {
            throw new BusinessException("投诉原因不能为空");
        }
    }
}