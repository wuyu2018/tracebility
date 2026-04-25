package com.foodtraceability.domain.event;

import com.foodtraceability.domain.DomainEvent;

public class ProductDeletedEvent extends DomainEvent {

    private final Long productId;
    private final Long[] batchIds;

    public ProductDeletedEvent(Long productId, Long[] batchIds) {
        this.productId = productId;
        this.batchIds = batchIds;
    }

    public Long getProductId() {
        return productId;
    }

    public Long[] getBatchIds() {
        return batchIds;
    }
}
