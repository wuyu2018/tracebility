package com.foodtraceability.service;

import com.foodtraceability.domain.DomainException;
import com.foodtraceability.dto.ComplaintDTO;
import com.foodtraceability.entity.Complaint;
import com.foodtraceability.repository.ComplaintRepository;
import com.foodtraceability.service.impl.ComplaintApplicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ComplaintApplicationServiceTest {

    @Mock
    private ComplaintRepository complaintRepository;

    @InjectMocks
    private ComplaintApplicationService service;

    private Complaint testComplaint;

    @BeforeEach
    void setUp() {
        testComplaint = new Complaint();
        testComplaint.setId(1L);
        testComplaint.setProductName("测试产品");
        testComplaint.setComplaintReason("质量问题");
        testComplaint.setComplaintTime(LocalDateTime.now());
        testComplaint.setIsProcessed(false);
    }

    @Test
    void testCreateComplaint() {
        ComplaintDTO dto = new ComplaintDTO();
        dto.setProductName("新产品");
        dto.setComplaintReason("质量问题");

        when(complaintRepository.save(any(Complaint.class))).thenAnswer(invocation -> {
            Complaint c = invocation.getArgument(0);
            c.setId(1L);
            return c;
        });

        ComplaintDTO result = service.createComplaint(dto);

        assertNotNull(result);
        assertEquals("新产品", result.getProductName());
        verify(complaintRepository).save(any(Complaint.class));
    }

    @Test
    void testCreateComplaint_WithAntiFakeCode() {
        ComplaintDTO dto = new ComplaintDTO();
        dto.setProductName("新产品");
        dto.setComplaintReason("质量问题");
        dto.setAntiFakeCode("SC123456");

        when(complaintRepository.save(any(Complaint.class))).thenAnswer(invocation -> {
            Complaint c = invocation.getArgument(0);
            c.setId(1L);
            return c;
        });

        ComplaintDTO result = service.createComplaint(dto);

        assertNotNull(result);
        verify(complaintRepository).save(any(Complaint.class));
    }

    @Test
    void testCreateComplaint_WithBatchNumber() {
        ComplaintDTO dto = new ComplaintDTO();
        dto.setProductName("新产品");
        dto.setComplaintReason("质量问题");
        dto.setBatchNumber("B202604250001");

        when(complaintRepository.save(any(Complaint.class))).thenAnswer(invocation -> {
            Complaint c = invocation.getArgument(0);
            c.setId(1L);
            return c;
        });

        ComplaintDTO result = service.createComplaint(dto);

        assertNotNull(result);
        verify(complaintRepository).save(any(Complaint.class));
    }

    @Test
    void testCreateComplaint_NullDTO() {
        assertThrows(DomainException.class, () -> {
            service.createComplaint(null);
        });
    }

    @Test
    void testUpdateComplaintReason() {
        when(complaintRepository.findById(1L)).thenReturn(Optional.of(testComplaint));
        when(complaintRepository.save(any(Complaint.class))).thenReturn(testComplaint);

        ComplaintDTO result = service.updateComplaintReason(1L, "新问题原因");

        assertNotNull(result);
        assertEquals("新问题原因", testComplaint.getComplaintReason());
        verify(complaintRepository).save(testComplaint);
    }

    @Test
    void testUpdateComplaintReason_NotFound() {
        when(complaintRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(DomainException.class, () -> {
            service.updateComplaintReason(999L, "新问题原因");
        });
    }

    @Test
    void testUpdateComplaintReason_InvalidId() {
        assertThrows(DomainException.class, () -> {
            service.updateComplaintReason(null, "新问题原因");
        });

        assertThrows(DomainException.class, () -> {
            service.updateComplaintReason(-1L, "新问题原因");
        });
    }
}
