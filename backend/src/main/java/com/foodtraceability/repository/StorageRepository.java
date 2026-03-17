package com.foodtraceability.repository;

import com.foodtraceability.entity.Storage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StorageRepository extends JpaRepository<Storage, Long> {
    List<Storage> findByProductNameAndBatchNumber(String productName, String batchNumber);
    List<Storage> findByProductName(String productName);
}
