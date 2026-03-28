package com.foodtraceability.repository;

import com.foodtraceability.entity.Storage;
import com.foodtraceability.entity.ProductionBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StorageRepository extends JpaRepository<Storage, Long> {
    List<Storage> findByBatch(ProductionBatch batch);
}