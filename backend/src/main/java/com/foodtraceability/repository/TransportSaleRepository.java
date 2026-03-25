package com.foodtraceability.repository;

import com.foodtraceability.entity.TransportSale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransportSaleRepository extends JpaRepository<TransportSale, Long> {
}