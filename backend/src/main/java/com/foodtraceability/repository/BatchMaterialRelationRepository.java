package com.foodtraceability.repository;

import com.foodtraceability.entity.BatchMaterialRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BatchMaterialRelationRepository extends JpaRepository<BatchMaterialRelation, Long> {
    List<BatchMaterialRelation> findByBatchId(Long batchId);
}