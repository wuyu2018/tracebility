package com.foodtraceability.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "blockchain_retry_task", indexes = {
    @Index(name = "idx_brt_status_next_retry", columnList = "status, next_retry_time"),
    @Index(name = "idx_brt_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
public class BlockchainRetryTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "chain_type", nullable = false, length = 20)
    private String chainType;

    @Column(name = "batch_id")
    private Long batchId;

    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Column(name = "action", nullable = false, length = 20)
    private String action;

    @Column(name = "data_snapshot", columnDefinition = "TEXT")
    private String dataSnapshot;

    @Column(name = "operator_id")
    private Long operatorId;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Column(name = "last_error_message", length = 500)
    private String lastErrorMessage;

    @Column(name = "retry_count", nullable = false)
    private int retryCount = 0;

    @Column(name = "max_retries", nullable = false)
    private int maxRetries = 5;

    @Column(name = "next_retry_time", nullable = false)
    private LocalDateTime nextRetryTime;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "PENDING";

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_retried_at")
    private LocalDateTime lastRetriedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) status = "PENDING";
        if (nextRetryTime == null) nextRetryTime = LocalDateTime.now();
    }
}
