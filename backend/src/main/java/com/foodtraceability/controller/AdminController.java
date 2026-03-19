package com.foodtraceability.controller;

import com.foodtraceability.dto.AdminLoginDTO;
import com.foodtraceability.dto.LoginResponseDTO;
import com.foodtraceability.exception.BusinessException;
import com.foodtraceability.service.AdminService;
import com.foodtraceability.util.CaptchaStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/captcha")
    public ResponseEntity<?> storeCaptcha(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        log.debug("[验证码] 存储验证码 - 用户: {}", username);
        String captcha = request.get("captcha");
        if (username == null || captcha == null) {
            log.warn("[验证码] 参数错误 - username或captcha为空");
            return ResponseEntity.badRequest().body("username和captcha不能为空");
        }
        CaptchaStorage.setCaptcha(username, captcha);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AdminLoginDTO loginDTO) {
        log.info("[管理员登录] 登录尝试 - 用户名: {}", loginDTO.getUsername());
        long startTime = System.currentTimeMillis();
        
        try {
            LoginResponseDTO response = adminService.login(loginDTO);
            CaptchaStorage.removeCaptcha(loginDTO.getUsername());
            long duration = System.currentTimeMillis() - startTime;
            log.info("[管理员登录] 登录成功 - 用户名: {}, 耗时: {}ms", loginDTO.getUsername(), duration);
            return ResponseEntity.ok(response);
        } catch (BusinessException e) {
            long duration = System.currentTimeMillis() - startTime;
            log.warn("[管理员登录] 登录失败 - 用户名: {}, 原因: {}, 耗时: {}ms", 
                loginDTO.getUsername(), e.getMessage(), duration);
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }
}
