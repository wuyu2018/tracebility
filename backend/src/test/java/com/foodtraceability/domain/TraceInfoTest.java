package com.foodtraceability.domain;

import com.foodtraceability.domain.valueobject.TraceInfo;
import com.foodtraceability.domain.valueobject.ProductInfo;
import com.foodtraceability.domain.valueobject.BatchInfo;
import com.foodtraceability.domain.valueobject.MaterialInfo;
import com.foodtraceability.entity.Product;
import com.foodtraceability.entity.ProductionBatch;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TraceInfoTest {

    @Test
    void testCreate_WithAllData() {
        Product product = new Product();
        product.setId(1L);
        product.setName("测试产品");

        ProductionBatch batch = new ProductionBatch();
        batch.setId(1L);
        batch.setBatchNumber("B202604250001");
        batch.setProductionDate(LocalDate.now());
        batch.setCreatedAt(LocalDateTime.now());

        List<MaterialInfo> materials = List.of(
                new MaterialInfo("原材料A", "M001", "供应商A", "生产商A")
        );

        TraceInfo traceInfo = TraceInfo.create(
                product,
                batch,
                List.of(),
                null,
                null,
                null,
                false
        );

        assertNotNull(traceInfo.getProduct());
        assertNotNull(traceInfo.getBatch());
        assertEquals("测试产品", traceInfo.getProduct().getName());
    }

    @Test
    void testCreate_WithNullMaterials() {
        Product product = new Product();
        product.setId(1L);
        product.setName("测试产品");

        ProductionBatch batch = new ProductionBatch();
        batch.setId(1L);
        batch.setBatchNumber("B202604250001");
        batch.setProductionDate(LocalDate.now());
        batch.setCreatedAt(LocalDateTime.now());

        TraceInfo traceInfo = TraceInfo.create(
                product,
                batch,
                null,
                null,
                null,
                null,
                false
        );

        assertNotNull(traceInfo.getMaterials());
        assertTrue(traceInfo.getMaterials().isEmpty());
    }
}
