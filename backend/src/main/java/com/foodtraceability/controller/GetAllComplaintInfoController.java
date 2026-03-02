package com.foodtraceability.controller;

import com.foodtraceability.dto.ComplaintInfoDTO;
import com.foodtraceability.service.GetAllComplaintInfoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

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

    @DeleteMapping("/deleteComplaintInfo/{id}")
    public ResponseEntity<?> deleteComplaintInfo(@PathVariable Long id) {
        try {
            boolean deleted = getAllComplaintInfo.deleteComplaintInfo(id);

            if (deleted) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "删除成功");
                response.put("id", id);
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("投诉信息不存在，ID: " + id);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("删除投诉信息失败: " + e.getMessage());
        }
    }

    // 可选：批量删除
    @DeleteMapping("/deleteComplaintInfo/batch")
    public ResponseEntity<?> deleteComplaintInfoBatch(@RequestBody List<Long> ids) {
        try {
            int successCount = 0;
            int failCount = 0;

            for (Long id : ids) {
                try {
                    boolean deleted = getAllComplaintInfo.deleteComplaintInfo(id);
                    if (deleted) {
                        successCount++;
                    } else {
                        failCount++;
                    }
                } catch (Exception e) {
                    failCount++;
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("successCount", successCount);
            response.put("failCount", failCount);
            response.put("message", "批量删除完成，成功: " + successCount + "，失败: " + failCount);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("批量删除投诉信息失败: " + e.getMessage());
        }
    }
}