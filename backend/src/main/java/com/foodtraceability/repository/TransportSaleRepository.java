package com.foodtraceability.repository;

import com.foodtraceability.entity.TransportSale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TransportSaleRepository extends JpaRepository<TransportSale, Long> {
    List<TransportSale> findByAntiFakeCodeAndBatchNumber(String antiFakeCode, String batchNumber);
    List<TransportSale> findByAntiFakeCode(String antiFakeCode);
    void deleteByAntiFakeCodeAndBatchNumber(String antiFakeCode, String batchNumber);
}
