package com.foodtraceability.repository;

import com.foodtraceability.entity.BlockchainRetryTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface BlockchainRetryTaskRepository extends JpaRepository<BlockchainRetryTask, Long> {

    List<BlockchainRetryTask> findByStatusAndNextRetryTimeBeforeOrderByCreatedAtAsc(
            String status, LocalDateTime nextRetryTime);

    List<BlockchainRetryTask> findByStatusOrderByCreatedAtAsc(String status);

    long countByStatus(String status);
}
