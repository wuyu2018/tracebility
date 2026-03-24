package com.foodtraceability.repository;

import com.foodtraceability.entity.MaterialPurchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MaterialPurchaseRepository extends JpaRepository<MaterialPurchase, Long> {
    List<MaterialPurchase> findByAntiFakeCodeAndBatchNumber(String antiFakeCode, String batchNumber);
    List<MaterialPurchase> findByAntiFakeCode(String antiFakeCode);
    void deleteByAntiFakeCodeAndBatchNumber(String antiFakeCode, String batchNumber);
}
