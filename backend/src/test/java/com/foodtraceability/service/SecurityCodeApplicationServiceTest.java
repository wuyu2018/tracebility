package com.foodtraceability.service;

import com.foodtraceability.domain.DomainException;
import com.foodtraceability.dto.SecurityCodeGenerateResponse;
import com.foodtraceability.entity.Product;
import com.foodtraceability.entity.ProductionBatch;
import com.foodtraceability.entity.SecurityCode;
import com.foodtraceability.repository.ProductRepository;
import com.foodtraceability.repository.ProductionBatchRepository;
import com.foodtraceability.repository.SecurityCodeRepository;
import com.foodtraceability.service.impl.SecurityCodeApplicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityCodeApplicationServiceTest {

    @Mock
    private SecurityCodeRepository codeRepository;

    @Mock
    private ProductionBatchRepository batchRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private SecurityCodeApplicationService service;

    private ProductionBatch testBatch;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("测试产品");

        testBatch = new ProductionBatch();
        testBatch.setId(1L);
        testBatch.setBatchNumber("B202604250001");
        testBatch.setProduct(testProduct);
    }

    @Test
    void testGenerateCodes() {
        when(batchRepository.findById(1L)).thenReturn(Optional.of(testBatch));
        when(codeRepository.save(any(SecurityCode.class))).thenAnswer(invocation -> {
            SecurityCode code = invocation.getArgument(0);
            code.setId(1L);
            return code;
        });
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        SecurityCodeGenerateResponse response = service.generateCodes(1L, 3);

        assertNotNull(response);
        assertEquals(3, response.getCount());
        assertEquals(3, response.getCodes().size());
        verify(codeRepository, times(3)).save(any(SecurityCode.class));
        verify(productRepository).save(testProduct);
    }

    @Test
    void testGenerateCodes_BatchNotFound() {
        when(batchRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(DomainException.class, () -> {
            service.generateCodes(999L, 3);
        });
    }

    @Test
    void testGetCodesByBatchId() {
        SecurityCode code = new SecurityCode();
        code.setId(1L);
        code.setCode("SC001");
        code.setBatch(testBatch);
        code.setStatus(SecurityCode.STATUS_INACTIVE);

        when(codeRepository.findByBatchId(1L)).thenReturn(List.of(code));

        List<?> result = service.getCodesByBatchId(1L);

        assertEquals(1, result.size());
    }

    @Test
    void testGetCodeByCode() {
        SecurityCode code = new SecurityCode();
        code.setId(1L);
        code.setCode("SC001");
        code.setBatch(testBatch);
        code.setStatus(SecurityCode.STATUS_INACTIVE);

        when(codeRepository.findByCode("SC001")).thenReturn(Optional.of(code));

        var result = service.getCodeByCode("SC001");

        assertNotNull(result);
        assertEquals("SC001", result.getCode());
    }

    @Test
    void testGetCodeByCode_NotFound() {
        when(codeRepository.findByCode("INVALID")).thenReturn(Optional.empty());

        assertThrows(DomainException.class, () -> {
            service.getCodeByCode("INVALID");
        });
    }
}
