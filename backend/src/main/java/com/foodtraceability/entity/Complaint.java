package com.foodtraceability.entity;

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
}