package com.foodtraceability.repository;

import com.foodtraceability.entity.MaterialPurchase;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MaterialPurchaseRepository extends JpaRepository<MaterialPurchase, Long> {
    List<MaterialPurchase> findByProductNameAndBatchNumber(String productName, String batchNumber);
    List<MaterialPurchase> findByProductName(String productName);
}
