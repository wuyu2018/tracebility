package com.foodtraceability.repository;

import com.foodtraceability.entity.SecurityCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SecurityCodeRepository extends JpaRepository<SecurityCode, Long> {
    Optional<SecurityCode> findByCode(String code);
    List<SecurityCode> findByBatchId(Long batchId);
    long countByBatchId(Long batchId);

    @Query("SELECT sc FROM SecurityCode sc WHERE sc.batch.product.id = :productId")
    List<SecurityCode> findByProductId(@Param("productId") Long productId);
}
