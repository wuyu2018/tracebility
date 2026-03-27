package com.foodtraceability.repository;

import com.foodtraceability.entity.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {
    List<Complaint> findByProductName(String productName);
    void deleteByProductName(String productName);
}
