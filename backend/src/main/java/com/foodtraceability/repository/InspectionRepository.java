package com.foodtraceability.repository;

import com.foodtraceability.entity.Inspection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface InspectionRepository extends JpaRepository<Inspection, Long> {
    List<Inspection> findByProductNameAndBatchNumber(String productName, String batchNumber);
    List<Inspection> findByProductName(String productName);
    void deleteByProductNameAndBatchNumber(String productName, String batchNumber);
}
