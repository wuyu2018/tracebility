package com.foodtraceability.service.impl;

import com.foodtraceability.dto.AdminLoginDTO;
import com.foodtraceability.dto.LoginResponseDTO;
import com.foodtraceability.entity.Admin;
import com.foodtraceability.exception.BusinessException;
import com.foodtraceability.repository.AdminRepository;
import com.foodtraceability.service.AdminService;
import com.foodtraceability.util.CaptchaStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;


import java.util.Optional;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public LoginResponseDTO login(AdminLoginDTO loginDTO) {
        String username = loginDTO.getUsername();
        String password = loginDTO.getPassword();
        String captcha = loginDTO.getCaptcha();

        if (captcha == null || captcha.isEmpty()) {
            throw new BusinessException("验证码不能为空");
        }

        String expectedCaptcha = CaptchaStorage.getCaptcha(username);
        if (expectedCaptcha == null || !expectedCaptcha.equalsIgnoreCase(captcha)) {
            throw new BusinessException("验证码错误");
        }

        Optional<Admin> adminOptional = adminRepository.findByUsername(username);
        if (!adminOptional.isPresent()) {
            throw new BusinessException("账号或密码错误");
        }

        Admin admin = adminOptional.get();

        if (!passwordEncoder.matches(password, admin.getPassword())) {
            throw new BusinessException("账号或密码错误");
        }

        LoginResponseDTO response = new LoginResponseDTO();
        response.setUsername(admin.getUsername());

        return response;
    }

    @Override
    public Admin findByUsername(String username) {
        return adminRepository.findByUsername(username).orElse(null);
    }
}