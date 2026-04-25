package com.foodtraceability.domain;

import com.foodtraceability.domain.event.*;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DomainEventTest {

    @Test
    void testProductDeletedEvent() {
        Long productId = 1L;
        Long[] batchIds = {10L, 20L, 30L};

        ProductDeletedEvent event = new ProductDeletedEvent(productId, batchIds);

        assertEquals(productId, event.getProductId());
        assertEquals(3, event.getBatchIds().length);
        assertNotNull(event.getEventId());
        assertNotNull(event.getOccurredOn());
    }

    @Test
    void testSecurityCodeActivatedEvent() {
        Long codeId = 1L;
        String code = "SC123456";

        SecurityCodeActivatedEvent event = new SecurityCodeActivatedEvent(codeId, code);

        assertEquals(codeId, event.getSecurityCodeId());
        assertEquals(code, event.getCode());
        assertNotNull(event.getEventId());
        assertNotNull(event.getOccurredOn());
    }

    @Test
    void testRepeatQueryDetectedEvent() {
        Long codeId = 1L;
        String code = "SC123456";
        Integer scanCount = 5;

        RepeatQueryDetectedEvent event = new RepeatQueryDetectedEvent(codeId, code, scanCount);

        assertEquals(codeId, event.getSecurityCodeId());
        assertEquals(code, event.getCode());
        assertEquals(scanCount, event.getScanCount());
        assertNotNull(event.getEventId());
        assertNotNull(event.getOccurredOn());
    }

    @Test
    void testBatchCreatedEvent() {
        Long batchId = 1L;
        String batchNumber = "B202604250001";
        Long productId = 10L;

        BatchCreatedEvent event = new BatchCreatedEvent(batchId, batchNumber, productId);

        assertEquals(batchId, event.getBatchId());
        assertEquals(batchNumber, event.getBatchNumber());
        assertEquals(productId, event.getProductId());
        assertNotNull(event.getEventId());
        assertNotNull(event.getOccurredOn());
    }

    @Test
    void testEventIdIsUnique() {
        ProductDeletedEvent event1 = new ProductDeletedEvent(1L, new Long[]{1L});
        ProductDeletedEvent event2 = new ProductDeletedEvent(1L, new Long[]{1L});

        assertNotEquals(event1.getEventId(), event2.getEventId());
    }
}
