package com.foodtraceability.controller;

import com.foodtraceability.dto.AdminLoginDTO;
import com.foodtraceability.dto.LoginResponseDTO;
import com.foodtraceability.exception.BusinessException;
import com.foodtraceability.service.AdminService;
import com.foodtraceability.util.CaptchaStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class AdminController {

    @Autowired
    private AdminService adminService;

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
        try {
            LoginResponseDTO response = adminService.login(loginDTO);
            CaptchaStorage.removeCaptcha(loginDTO.getUsername());
            return ResponseEntity.ok(response);
        } catch (BusinessException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }
}