package com.foodtraceability.repository;

import com.foodtraceability.entity.Inspection;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InspectionRepository extends JpaRepository<Inspection, Long> {
    List<Inspection> findByProductId(Long productId);
}
