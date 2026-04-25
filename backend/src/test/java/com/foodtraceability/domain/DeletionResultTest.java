package com.foodtraceability.domain;

import com.foodtraceability.domain.valueobject.ProductInfo;
import com.foodtraceability.entity.Product;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DeletionResultTest {

    @Test
    void testCreate() {
        Long productId = 1L;
        Long[] batchIds = {10L, 20L, 30L};

        DeletionResult result = DeletionResult.create(productId, batchIds);

        assertEquals(productId, result.getProductId());
        assertEquals(3, result.getBatchCount());
        assertEquals(3, result.getBatchIds().size());
        assertEquals(10L, result.getBatchIds().get(0));
        assertEquals(20L, result.getBatchIds().get(1));
        assertEquals(30L, result.getBatchIds().get(2));
    }

    @Test
    void testGetDeletedBatchIds() {
        Long productId = 1L;
        Long[] batchIds = {10L, 20L};

        DeletionResult result = DeletionResult.create(productId, batchIds);

        Long[] deletedBatchIds = result.getDeletedBatchIds();

        assertEquals(2, deletedBatchIds.length);
        assertEquals(10L, deletedBatchIds[0]);
        assertEquals(20L, deletedBatchIds[1]);
    }

    @Test
    void testCreateWithEmptyBatchIds() {
        Long productId = 1L;
        Long[] batchIds = {};

        DeletionResult result = DeletionResult.create(productId, batchIds);

        assertEquals(0, result.getBatchCount());
        assertTrue(result.getBatchIds().isEmpty());
    }
}
