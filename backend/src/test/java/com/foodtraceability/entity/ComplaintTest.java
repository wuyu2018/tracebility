package com.foodtraceability.entity;

import com.foodtraceability.exception.BusinessException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ComplaintTest {

    @Test
    void testCreate() {
        Complaint complaint = Complaint.create("产品A", "质量问题");

        assertEquals("产品A", complaint.getProductName());
        assertEquals("质量问题", complaint.getComplaintReason());
        assertNotNull(complaint.getComplaintTime());
        assertFalse(complaint.isProcessed());
    }

    @Test
    void testCreateWithNullProductName() {
        assertThrows(BusinessException.class, () -> {
            Complaint.create(null, "质量问题");
        });
    }

    @Test
    void testCreateWithBlankProductName() {
        assertThrows(BusinessException.class, () -> {
            Complaint.create("   ", "质量问题");
        });
    }

    @Test
    void testCreateWithNullReason() {
        assertThrows(BusinessException.class, () -> {
            Complaint.create("产品A", null);
        });
    }

    @Test
    void testCreateWithBlankReason() {
        assertThrows(BusinessException.class, () -> {
            Complaint.create("产品A", "   ");
        });
    }

    @Test
    void testUpdateReason() {
        Complaint complaint = Complaint.create("产品A", "质量问题");

        complaint.updateReason("新质量问题");

        assertEquals("新质量问题", complaint.getComplaintReason());
    }

    @Test
    void testLinkToProduct_WithAntiFakeCode() {
        Complaint complaint = new Complaint();

        complaint.linkToProduct("SC123456");

        assertEquals("SC123456", complaint.getAntiFakeCode());
        assertNull(complaint.getBatchNumber());
    }

    @Test
    void testLinkToProduct_WithBatchNumber() {
        Complaint complaint = new Complaint();

        complaint.linkToProduct("B202604250001");

        assertNull(complaint.getAntiFakeCode());
        assertEquals("B202604250001", complaint.getBatchNumber());
    }

    @Test
    void testMarkAsProcessed() {
        Complaint complaint = Complaint.create("产品A", "质量问题");
        assertFalse(complaint.isProcessed());

        complaint.markAsProcessed();

        assertTrue(complaint.isProcessed());
    }
}
