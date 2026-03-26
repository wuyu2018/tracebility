package com.foodtraceability.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "security_code", indexes = {
    @Index(name = "idx_security_code", columnList = "code", unique = true)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class SecurityCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, unique = true, length = 64)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private ProductionBatch batch;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "未激活";

    @Column(name = "first_scan_time")
    private LocalDateTime firstScanTime;

    @Column(name = "scan_count")
    private Integer scanCount = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (scanCount == null) {
            scanCount = 0;
        }
        if (status == null) {
            status = "未激活";
        }
    }
}