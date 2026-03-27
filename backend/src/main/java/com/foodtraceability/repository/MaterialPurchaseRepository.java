package com.foodtraceability.repository;

import com.foodtraceability.entity.MaterialPurchase;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MaterialPurchaseRepository extends JpaRepository<MaterialPurchase, Long> {
    @EntityGraph(attributePaths = {"product"})
    List<MaterialPurchase> findByIsDeletedFalse();

    @EntityGraph(attributePaths = {"product"})
    List<MaterialPurchase> findByProductIdAndIsDeletedFalse(Long productId);

    Optional<MaterialPurchase> findByBatchNumberAndIsDeletedFalse(String batchNumber);
}