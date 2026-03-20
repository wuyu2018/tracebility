package com.foodtraceability.repository;

import com.foodtraceability.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByAntiFakeCode(String antiFakeCode);
    Optional<Product> findByAntiFakeCodeAndIsDeletedFalse(String antiFakeCode);
    Optional<Product> findByName(String name);
    boolean existsByAntiFakeCode(String antiFakeCode);
    
    List<Product> findByIsDeletedFalseAndLastQueriedTimeBefore(LocalDateTime dateTime);
    
    @Modifying
    @Query("UPDATE Product p SET p.lastQueriedTime = :queryTime WHERE p.antiFakeCode = :antiFakeCode")
    void updateLastQueriedTime(@Param("antiFakeCode") String antiFakeCode, @Param("queryTime") LocalDateTime queryTime);
}
