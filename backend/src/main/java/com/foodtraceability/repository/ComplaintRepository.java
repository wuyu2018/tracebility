package com.foodtraceability.repository;

import com.foodtraceability.entity.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ComplaintRepository extends JpaRepository<Complaint, Long> {
    List<Complaint> findByProductId(Long productId);
}
