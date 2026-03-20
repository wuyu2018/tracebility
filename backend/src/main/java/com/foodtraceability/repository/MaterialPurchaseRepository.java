package com.foodtraceability.repository;

import com.foodtraceability.entity.MaterialPurchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MaterialPurchaseRepository extends JpaRepository<MaterialPurchase, Long> {
    List<MaterialPurchase> findByProductNameAndBatchNumber(String productName, String batchNumber);
    List<MaterialPurchase> findByProductName(String productName);
    void deleteByProductNameAndBatchNumber(String productName, String batchNumber);
}
