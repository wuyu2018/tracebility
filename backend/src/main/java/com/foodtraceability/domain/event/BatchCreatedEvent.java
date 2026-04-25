package com.foodtraceability.domain.event;

import com.foodtraceability.domain.DomainEvent;

public class BatchCreatedEvent extends DomainEvent {

    private final Long batchId;
    private final String batchNumber;
    private final Long productId;

    public BatchCreatedEvent(Long batchId, String batchNumber, Long productId) {
        this.batchId = batchId;
        this.batchNumber = batchNumber;
        this.productId = productId;
    }

    public Long getBatchId() {
        return batchId;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public Long getProductId() {
        return productId;
    }
}
