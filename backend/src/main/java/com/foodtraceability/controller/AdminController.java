package com.foodtraceability.controller;

import com.foodtraceability.dto.AdminLoginDTO;
import com.foodtraceability.dto.LoginResponseDTO;
import com.foodtraceability.entity.Admin;
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
        String captcha = request.get("captcha");
        if (username == null || captcha == null) {
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

    @PostMapping("/admin/register")
    public ResponseEntity<?> registerAdmin(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");
        String currentPassword = request.get("currentPassword");
        String currentAdminUsername = request.get("currentAdminUsername");

        if (username == null || username.isBlank()) {
            return ResponseEntity.badRequest().body("用户名不能为空");
        }
        if (password == null || password.isBlank()) {
            return ResponseEntity.badRequest().body("密码不能为空");
        }
        if (password.length() < 8) {
            return ResponseEntity.badRequest().body("密码长度不能少于8位");
        }
        if (!password.matches("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{8,}$")) {
            return ResponseEntity.badRequest().body("密码必须包含字母、数字和特殊字符");
        }
        if (!username.matches("^[a-zA-Z0-9]{4,20}$")) {
            return ResponseEntity.badRequest().body("用户名必须为4-20位字母或数字组合");
        }
        if (currentPassword == null || currentPassword.isBlank()) {
            return ResponseEntity.badRequest().body("请输入当前管理员密码进行身份验证");
        }
        if (currentAdminUsername == null || currentAdminUsername.isBlank()) {
            return ResponseEntity.badRequest().body("无法确定当前管理员身份");
        }

        try {
            adminService.verifyCurrentPassword(currentAdminUsername, currentPassword);
            
            Admin admin = adminService.createAdmin(username, password);
            log.info("[管理员注册] 创建成功 - 操作管理员: {}, 新管理员: {}", currentAdminUsername, username);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("message", "管理员创建成功", "username", admin.getUsername()));
        } catch (BusinessException e) {
            log.warn("[管理员注册] 创建失败 - 操作管理员: {}, 原因: {}", currentAdminUsername, e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }
}
