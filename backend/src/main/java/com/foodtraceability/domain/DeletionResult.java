package com.foodtraceability.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
@AllArgsConstructor
public class DeletionResult {

    private final Long productId;
    private final List<Long> batchIds;
    private final int batchCount;

    private DeletionResult(Long productId, Long[] batchIds) {
        this.productId = productId;
        this.batchIds = Arrays.asList(batchIds);
        this.batchCount = batchIds.length;
    }

    public static DeletionResult create(Long productId, Long[] batchIds) {
        return new DeletionResult(productId, batchIds);
    }

    public Long[] getDeletedBatchIds() {
        return batchIds.toArray(new Long[0]);
    }
}
