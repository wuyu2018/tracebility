package com.foodtraceability.controller;

import com.foodtraceability.dto.AdminLoginDTO;
import com.foodtraceability.dto.LoginResponseDTO;
import com.foodtraceability.exception.BusinessException;
import com.foodtraceability.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AdminLoginDTO loginDTO) {
        try {
            LoginResponseDTO response = adminService.login(loginDTO);
            return ResponseEntity.ok(response);
        } catch (BusinessException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }
}