package com.foodtraceability.repository;

import com.foodtraceability.entity.BlockchainAnchor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BlockchainAnchorRepository extends JpaRepository<BlockchainAnchor, Long> {

    List<BlockchainAnchor> findByChainTypeAndBatchIdOrderByAnchorDateDesc(String chainType, Long batchId);
}
