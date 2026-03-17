package com.foodtraceability.repository;

import com.foodtraceability.entity.TransportSale;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TransportSaleRepository extends JpaRepository<TransportSale, Long> {
    List<TransportSale> findByProductNameAndBatchNumber(String productName, String batchNumber);
    List<TransportSale> findByProductName(String productName);
}
