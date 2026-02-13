package com.foodtraceability.controller;

import com.foodtraceability.dto.ComplaintInfoDTO;
import com.foodtraceability.service.GetAllComplaintInfoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*", allowedHeaders = "*")

public class GetAllComplaintInfoController {
    private final GetAllComplaintInfoService getAllComplaintInfo;

    public GetAllComplaintInfoController( GetAllComplaintInfoService getAllComplaintInfo ) {
        this.getAllComplaintInfo = getAllComplaintInfo;
    }

    @GetMapping("/getAllComplaintInfo")
    public ResponseEntity<?> getAllComplaintInfo( ComplaintInfoDTO complaintInfoDTO ) {
        try {
            // 调用服务层获取数据
            List<ComplaintInfoDTO.ComplaintInfo> result = getAllComplaintInfo.getAllComplaintInfo();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("获取投诉信息失败: " + e.getMessage());
        }
    }
}