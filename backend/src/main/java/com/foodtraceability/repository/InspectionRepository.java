package com.foodtraceability.repository;

import com.foodtraceability.entity.Inspection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface InspectionRepository extends JpaRepository<Inspection, Long> {
    List<Inspection> findByAntiFakeCodeAndBatchNumber(String antiFakeCode, String batchNumber);
    List<Inspection> findByAntiFakeCode(String antiFakeCode);
    void deleteByAntiFakeCodeAndBatchNumber(String antiFakeCode, String batchNumber);
}
