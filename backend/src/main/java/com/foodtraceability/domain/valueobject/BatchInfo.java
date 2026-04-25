package com.foodtraceability.domain.valueobject;

import com.foodtraceability.entity.ProductionBatch;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class BatchInfo {

    private final Long id;
    private final String batchNumber;
    private final LocalDate productionDate;
    private final String shelfLife;
    private final LocalDateTime createdAt;

    public static BatchInfo from(ProductionBatch batch) {
        if (batch == null) {
            return null;
        }
        return new BatchInfo(
            batch.getId(),
            batch.getBatchNumber(),
            batch.getProductionDate(),
            batch.getShelfLife(),
            batch.getCreatedAt()
        );
    }
}
