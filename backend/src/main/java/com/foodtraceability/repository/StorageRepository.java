package com.foodtraceability.repository;

import com.foodtraceability.entity.Storage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StorageRepository extends JpaRepository<Storage, Long> {
    List<Storage> findByProductNameAndBatchNumber(String productName, String batchNumber);
    List<Storage> findByProductName(String productName);
    void deleteByProductNameAndBatchNumber(String productName, String batchNumber);
}
