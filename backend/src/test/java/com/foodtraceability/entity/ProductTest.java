package com.foodtraceability.entity;

import com.foodtraceability.domain.event.SecurityCodeActivatedEvent;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ProductTest {

    @Test
    void testSoftDelete() {
        Product product = new Product();
        product.setName("测试产品");
        product.setIsDeleted(false);

        product.softDelete();

        assertTrue(product.isDeleted());
    }

    @Test
    void testAssignAndClearQrCode() {
        Product product = new Product();
        product.setName("测试产品");

        product.assignQrCode("SC123456", "/qrcode/1");

        assertEquals("SC123456", product.getAntiFakeCode());
        assertEquals("/qrcode/1", product.getQrCodeUrl());

        product.clearQrCode();

        assertNull(product.getAntiFakeCode());
        assertNull(product.getQrCodeUrl());
    }

    @Test
    void testHasAntiFakeCode() {
        Product product = new Product();

        assertFalse(product.hasAntiFakeCode());

        product.setAntiFakeCode("SC123456");

        assertTrue(product.hasAntiFakeCode());
    }

    @Test
    void testCanBeDeleted() {
        Product product = new Product();
        product.setIsDeleted(false);

        assertTrue(product.canBeDeleted());

        product.softDelete();

        assertFalse(product.canBeDeleted());
    }

    @Test
    void testCreateWithAntiFakeCode() {
        Product product = Product.createWithAntiFakeCode(
                "测试产品",
                "规格A",
                "12个月",
                "http://example.com/image.jpg",
                "13800000000",
                "test@example.com"
        );

        assertEquals("测试产品", product.getName());
        assertEquals("规格A", product.getSpecification());
        assertEquals("12个月", product.getShelfLife());
        assertEquals("http://example.com/image.jpg", product.getImageUrl());
        assertEquals("13800000000", product.getContactPhone());
        assertEquals("test@example.com", product.getContactEmail());
        assertFalse(product.isDeleted());
    }

    @Test
    void testDomainEvents() {
        Product product = new Product();

        assertNotNull(product.getDomainEvents());
        assertTrue(product.getDomainEvents().isEmpty());
    }
}
