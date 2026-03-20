package com.foodtraceability.repository;

import com.foodtraceability.entity.QueryRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface QueryRecordRepository extends JpaRepository<QueryRecord, Long> {
    Optional<QueryRecord> findByAntiFakeCodeAndIsDeletedFalse(String antiFakeCode);
    List<QueryRecord> findByIsDeletedFalseAndIsBackedUpFalse();
    List<QueryRecord> findByIsDeletedFalseAndQueryTimeBefore(LocalDateTime time);
}
