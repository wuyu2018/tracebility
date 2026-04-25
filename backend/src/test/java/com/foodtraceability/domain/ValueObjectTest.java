package com.foodtraceability.domain;

import com.foodtraceability.domain.valueobject.ProductInfo;
import com.foodtraceability.entity.Product;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValueObjectTest {

    @Test
    void testProductInfoEquality() {
        ProductInfo info1 = new ProductInfo(1L, "产品A", "规格A", "12个月",
                "http://example.com", "13800000000", "test@example.com", "SC001");
        ProductInfo info2 = new ProductInfo(1L, "产品A", "规格A", "12个月",
                "http://example.com", "13800000000", "test@example.com", "SC001");
        ProductInfo info3 = new ProductInfo(2L, "产品B", "规格B", "6个月",
                "http://example2.com", "13900000000", "test2@example.com", "SC002");

        assertEquals(info1, info2);
        assertNotEquals(info1, info3);
        assertEquals(info1.hashCode(), info2.hashCode());
    }

    @Test
    void testProductInfo_FromNullProduct() {
        ProductInfo info = ProductInfo.from(null);

        assertNull(info);
    }

    @Test
    void testProductInfo_FromProduct() {
        Product product = new Product();
        product.setId(1L);
        product.setName("测试产品");
        product.setSpecification("规格A");
        product.setShelfLife("12个月");
        product.setImageUrl("http://example.com");
        product.setContactPhone("13800000000");
        product.setContactEmail("test@example.com");
        product.setAntiFakeCode("SC001");

        ProductInfo info = ProductInfo.from(product);

        assertEquals(1L, info.getId());
        assertEquals("测试产品", info.getName());
        assertEquals("规格A", info.getSpecification());
        assertEquals("12个月", info.getShelfLife());
        assertEquals("http://example.com", info.getImageUrl());
        assertEquals("13800000000", info.getContactPhone());
        assertEquals("test@example.com", info.getContactEmail());
        assertEquals("SC001", info.getAntiFakeCode());
    }
}
