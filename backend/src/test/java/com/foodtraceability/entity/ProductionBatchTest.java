package com.foodtraceability.entity;

import com.foodtraceability.domain.valueobject.TraceInfo;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProductionBatchTest {

    @Test
    void testCreate() {
        Product product = new Product();
        product.setId(1L);
        product.setName("测试产品");

        ProductionBatch batch = ProductionBatch.create(
                product,
                "B202604250001",
                LocalDate.now(),
                "12个月"
        );

        assertEquals(product, batch.getProduct());
        assertEquals("B202604250001", batch.getBatchNumber());
        assertEquals(LocalDate.now(), batch.getProductionDate());
        assertEquals("12个月", batch.getShelfLife());
        assertFalse(batch.isDeleted());
    }

    @Test
    void testSoftDelete() {
        ProductionBatch batch = new ProductionBatch();
        batch.setIsDeleted(false);

        batch.softDelete();

        assertTrue(batch.isDeleted());
    }

    @Test
    void testAssociateStorage() {
        ProductionBatch batch = new ProductionBatch();
        Storage storage = new Storage();
        storage.setId(1L);

        batch.associateStorage(storage);

        assertEquals(1L, batch.getStorageId());
    }

    @Test
    void testAssociateTransportSale() {
        ProductionBatch batch = new ProductionBatch();
        TransportSale transportSale = new TransportSale();
        transportSale.setId(1L);

        batch.associateTransportSale(transportSale);

        assertEquals(1L, batch.getTransportSaleId());
    }

    @Test
    void testCanBeDeleted() {
        ProductionBatch batch = new ProductionBatch();
        batch.setIsDeleted(false);

        assertTrue(batch.canBeDeleted());

        batch.softDelete();

        assertFalse(batch.canBeDeleted());
    }

    @Test
    void testBuildTraceInfo() {
        Product product = new Product();
        product.setId(1L);
        product.setName("测试产品");
        product.setSpecification("规格A");
        product.setShelfLife("12个月");
        product.setImageUrl("http://example.com/image.jpg");
        product.setContactPhone("13800000000");
        product.setContactEmail("test@example.com");
        product.setAntiFakeCode("SC123456");

        ProductionBatch batch = new ProductionBatch();
        batch.setId(1L);
        batch.setBatchNumber("B202604250001");
        batch.setProductionDate(LocalDate.now());
        batch.setShelfLife("12个月");
        batch.setProduct(product);
        batch.setCreatedAt(java.time.LocalDateTime.now());

        SecurityCode securityCode = new SecurityCode();
        securityCode.setCode("SC123456");
        securityCode.setStatus(SecurityCode.STATUS_ACTIVE);

        List<MaterialPurchase> materials = List.of();
        Inspection inspection = null;
        Storage storage = null;
        TransportSale transportSale = null;

        TraceInfo traceInfo = batch.buildTraceInfo(
                securityCode,
                materials,
                inspection,
                storage,
                transportSale,
                false
        );

        assertNotNull(traceInfo);
        assertNotNull(traceInfo.getProduct());
        assertNotNull(traceInfo.getBatch());
        assertEquals("测试产品", traceInfo.getProduct().getName());
        assertEquals("B202604250001", traceInfo.getBatch().getBatchNumber());
    }

    @Test
    void testDomainEvents() {
        ProductionBatch batch = new ProductionBatch();

        assertNotNull(batch.getDomainEvents());
        assertTrue(batch.getDomainEvents().isEmpty());
    }
}
