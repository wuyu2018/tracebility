package com.foodtraceability.service;

import com.foodtraceability.domain.DomainException;
import com.foodtraceability.domain.valueobject.*;
import com.foodtraceability.dto.TraceInfoDTO;
import com.foodtraceability.entity.Product;
import com.foodtraceability.entity.ProductionBatch;
import com.foodtraceability.entity.SecurityCode;
import com.foodtraceability.repository.SecurityCodeRepository;
import com.foodtraceability.service.domain.TraceabilityDomainService;
import com.foodtraceability.service.impl.TraceabilityApplicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraceabilityApplicationServiceTest {

    @Mock
    private SecurityCodeRepository securityCodeRepository;

    @Mock
    private TraceabilityDomainService traceabilityDomainService;

    @InjectMocks
    private TraceabilityApplicationService service;

    private SecurityCode testSecurityCode;
    private Product testProduct;
    private ProductionBatch testBatch;
    private TraceInfo testTraceInfo;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("测试产品");

        testBatch = new ProductionBatch();
        testBatch.setId(1L);
        testBatch.setBatchNumber("B202604250001");
        testBatch.setProduct(testProduct);

        testSecurityCode = new SecurityCode();
        testSecurityCode.setId(1L);
        testSecurityCode.setCode("SC001");
        testSecurityCode.setBatch(testBatch);
        testSecurityCode.setStatus(SecurityCode.STATUS_ACTIVE);
        testSecurityCode.setFirstScanTime(LocalDateTime.now());
        testSecurityCode.setScanCount(1);

        ProductInfo productInfo = new ProductInfo(1L, "测试产品", "规格A", "12个月",
                "http://example.com", "13800000000", "test@example.com", "SC001");
        BatchInfo batchInfo = new BatchInfo(1L, "B202604250001", LocalDate.now(), "12个月", LocalDateTime.now());

        testTraceInfo = new TraceInfo(productInfo, batchInfo, List.of(), null, null, null);
    }

    @Test
    void testGetTraceInfoByCode_Success() {
        when(securityCodeRepository.findByCode("SC001")).thenReturn(Optional.of(testSecurityCode));
        when(securityCodeRepository.save(any(SecurityCode.class))).thenReturn(testSecurityCode);
        when(traceabilityDomainService.buildFullTraceInfo(eq("SC001"), eq(false))).thenReturn(testTraceInfo);

        Optional<TraceInfoDTO> result = service.getTraceInfoByCode("SC001");

        assertTrue(result.isPresent());
        assertEquals("测试产品", result.get().getProduct().getName());
        verify(securityCodeRepository).save(testSecurityCode);
    }

    @Test
    void testGetTraceInfoByCode_NotFound() {
        when(securityCodeRepository.findByCode("INVALID")).thenReturn(Optional.empty());

        Optional<TraceInfoDTO> result = service.getTraceInfoByCode("INVALID");

        assertTrue(result.isEmpty());
    }

    @Test
    void testGetTraceInfoByBatchNumber_Success() {
        when(traceabilityDomainService.buildTraceInfoByBatchNumber(eq("B202604250001"), eq(false))).thenReturn(testTraceInfo);

        Optional<TraceInfoDTO> result = service.getTraceInfoByBatchNumber("B202604250001");

        assertTrue(result.isPresent());
        assertEquals("B202604250001", result.get().getBatch().getBatchNumber());
    }

    @Test
    void testGetTraceInfoByCodeForAdmin_Success() {
        when(securityCodeRepository.findByCode("SC001")).thenReturn(Optional.of(testSecurityCode));
        when(traceabilityDomainService.buildFullTraceInfo(eq("SC001"), eq(true))).thenReturn(testTraceInfo);

        Optional<TraceInfoDTO> result = service.getTraceInfoByCodeForAdmin("SC001");

        assertTrue(result.isPresent());
        verify(traceabilityDomainService).buildFullTraceInfo("SC001", true);
    }

    @Test
    void testGetTraceInfoByCodeForAdmin_NotFound() {
        when(securityCodeRepository.findByCode("INVALID")).thenReturn(Optional.empty());

        Optional<TraceInfoDTO> result = service.getTraceInfoByCodeForAdmin("INVALID");

        assertTrue(result.isEmpty());
    }
}
