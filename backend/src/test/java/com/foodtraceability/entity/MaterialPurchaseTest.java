package com.foodtraceability.entity;

import com.foodtraceability.domain.valueobject.MaterialInfo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MaterialPurchaseTest {

    @Test
    void testCreate() {
        Product product = new Product();
        product.setId(1L);

        MaterialPurchase material = MaterialPurchase.create(product, "原材料A", "M20260425001");

        assertEquals(product, material.getProduct());
        assertEquals("原材料A", material.getMaterialName());
        assertEquals("M20260425001", material.getBatchNumber());
        assertFalse(material.isDeleted());
    }

    @Test
    void testSoftDelete() {
        MaterialPurchase material = new MaterialPurchase();
        material.setIsDeleted(false);

        material.softDelete();

        assertTrue(material.isDeleted());
    }

    @Test
    void testCanBeDeleted() {
        MaterialPurchase material = new MaterialPurchase();
        material.setIsDeleted(false);

        assertTrue(material.canBeDeleted());

        material.softDelete();

        assertFalse(material.canBeDeleted());
    }

    @Test
    void testUpdateSupplier() {
        MaterialPurchase material = new MaterialPurchase();

        material.updateSupplier("供应商A", "生产商A", "地址A");

        assertEquals("供应商A", material.getSupplierName());
        assertEquals("生产商A", material.getProducerName());
        assertEquals("地址A", material.getProducerAddress());
    }

    @Test
    void testUpdateQuantity() {
        MaterialPurchase material = new MaterialPurchase();

        material.updateQuantity(100.0, "kg");

        assertEquals(100.0, material.getQuantity());
        assertEquals("kg", material.getUnit());
    }

    @Test
    void testToMaterialInfo() {
        MaterialPurchase material = new MaterialPurchase();
        material.setMaterialName("原材料A");
        material.setBatchNumber("M20260425001");
        material.setSupplierName("供应商A");
        material.setProducerName("生产商A");

        MaterialInfo info = material.toMaterialInfo();

        assertNotNull(info);
        assertEquals("原材料A", info.getMaterialName());
        assertEquals("M20260425001", info.getBatchNumber());
        assertEquals("供应商A", info.getSupplierName());
        assertEquals("生产商A", info.getProducerName());
    }
}
