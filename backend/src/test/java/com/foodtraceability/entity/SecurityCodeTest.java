package com.foodtraceability.entity;

import com.foodtraceability.domain.event.SecurityCodeActivatedEvent;
import com.foodtraceability.domain.event.RepeatQueryDetectedEvent;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class SecurityCodeTest {

    @Test
    void testGenerateUniqueCode() {
        String code1 = SecurityCode.generateUniqueCode();
        String code2 = SecurityCode.generateUniqueCode();

        assertNotNull(code1);
        assertTrue(code1.startsWith("SC"));
        assertNotEquals(code1, code2);
    }

    @Test
    void testCreate() {
        ProductionBatch batch = new ProductionBatch();
        batch.setId(1L);

        SecurityCode code = SecurityCode.create(batch);

        assertNotNull(code.getCode());
        assertTrue(code.getCode().startsWith("SC"));
        assertEquals(batch, code.getBatch());
        assertEquals(SecurityCode.STATUS_INACTIVE, code.getStatus());
        assertEquals(0, code.getScanCount());
    }

    @Test
    void testIsActivated() {
        SecurityCode code = new SecurityCode();

        assertFalse(code.isActivated());

        code.setStatus(SecurityCode.STATUS_ACTIVE);

        assertTrue(code.isActivated());
    }

    @Test
    void testIsFirstQuery() {
        SecurityCode code = new SecurityCode();

        assertTrue(code.isFirstQuery());

        code.setScanCount(1);

        assertFalse(code.isFirstQuery());
    }

    @Test
    void testIsRepeatedQuery() {
        SecurityCode code = new SecurityCode();

        assertFalse(code.isRepeatedQuery());

        code.setScanCount(1);

        assertFalse(code.isRepeatedQuery());

        code.setScanCount(2);

        assertTrue(code.isRepeatedQuery());
    }

    @Test
    void testActivate() {
        SecurityCode code = new SecurityCode();
        code.setStatus(SecurityCode.STATUS_INACTIVE);
        code.setScanCount(0);

        code.activate();

        assertEquals(SecurityCode.STATUS_ACTIVE, code.getStatus());
        assertNotNull(code.getFirstScanTime());
        assertEquals(1, code.getScanCount());
        assertFalse(code.getDomainEvents().isEmpty());
        assertTrue(code.getDomainEvents().get(0) instanceof SecurityCodeActivatedEvent);
    }

    @Test
    void testActivateOnlyOnce() {
        SecurityCode code = new SecurityCode();
        code.setStatus(SecurityCode.STATUS_INACTIVE);

        code.activate();
        LocalDateTime firstScanTime = code.getFirstScanTime();
        int firstScanCount = code.getScanCount();

        code.activate();

        assertEquals(firstScanTime, code.getFirstScanTime());
        assertEquals(firstScanCount, code.getScanCount());
    }

    @Test
    void testRecordQuery() {
        SecurityCode code = new SecurityCode();
        code.setScanCount(0);

        code.recordQuery();

        assertEquals(1, code.getScanCount());

        code.recordQuery();

        assertEquals(2, code.getScanCount());
    }

    @Test
    void testRecordQueryAndActivateIfNeeded_FirstQuery() {
        SecurityCode code = new SecurityCode();
        code.setScanCount(0);
        code.setStatus(SecurityCode.STATUS_INACTIVE);

        code.recordQueryAndActivateIfNeeded();

        assertEquals(SecurityCode.STATUS_ACTIVE, code.getStatus());
        assertEquals(1, code.getScanCount());
        assertNotNull(code.getFirstScanTime());
    }

    @Test
    void testRecordQueryAndActivateIfNeeded_SubsequentQuery() {
        SecurityCode code = new SecurityCode();
        code.setScanCount(1);
        code.setStatus(SecurityCode.STATUS_ACTIVE);

        code.recordQueryAndActivateIfNeeded();

        assertEquals(SecurityCode.STATUS_ACTIVE, code.getStatus());
        assertEquals(2, code.getScanCount());
    }

    @Test
    void testRecordQueryAndActivateIfNeeded_RepeatQueryTriggersEvent() {
        SecurityCode code = new SecurityCode();
        code.setScanCount(2);
        code.setStatus(SecurityCode.STATUS_ACTIVE);

        code.recordQueryAndActivateIfNeeded();

        assertEquals(3, code.getScanCount());
        assertFalse(code.getDomainEvents().isEmpty());
        assertTrue(code.getDomainEvents().stream()
                .anyMatch(e -> e instanceof RepeatQueryDetectedEvent));
    }

    @Test
    void testGetQueryCount() {
        SecurityCode code = new SecurityCode();

        assertEquals(0, code.getQueryCount());

        code.setScanCount(null);

        assertEquals(0, code.getQueryCount());
    }
}
