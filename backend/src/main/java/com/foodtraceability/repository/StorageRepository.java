package com.foodtraceability.repository;

import com.foodtraceability.entity.Storage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StorageRepository extends JpaRepository<Storage, Long> {
    List<Storage> findByAntiFakeCodeAndBatchNumber(String antiFakeCode, String batchNumber);
    List<Storage> findByAntiFakeCode(String antiFakeCode);
    void deleteByAntiFakeCodeAndBatchNumber(String antiFakeCode, String batchNumber);
}
