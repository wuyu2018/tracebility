package com.foodtraceability.repository;

import com.foodtraceability.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByAntiFakeCode(String antiFakeCode);
    boolean existsByAntiFakeCode(String antiFakeCode);
}
