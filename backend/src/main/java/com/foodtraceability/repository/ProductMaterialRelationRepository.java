package com.foodtraceability.repository;

import com.foodtraceability.entity.ProductMaterialRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductMaterialRelationRepository extends JpaRepository<ProductMaterialRelation, Long> {
    List<ProductMaterialRelation> findByProductId(Long productId);

    Optional<ProductMaterialRelation> findByProductIdAndMaterialId(Long productId, Long materialId);

    boolean existsByProductIdAndMaterialId(Long productId, Long materialId);

    void deleteByProductId(Long productId);
}
