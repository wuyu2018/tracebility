package com.foodtraceability.service.impl;

import com.foodtraceability.dto.AdminLoginDTO;
import com.foodtraceability.dto.LoginResponseDTO;
import com.foodtraceability.entity.Admin;
import com.foodtraceability.exception.BusinessException;
import com.foodtraceability.repository.AdminRepository;
import com.foodtraceability.service.AdminService;
import com.foodtraceability.service.LoginAttemptService;
import com.foodtraceability.util.CaptchaStorage;
import com.foodtraceability.util.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private CaptchaStorage captchaStorage;

    @Autowired
    private LoginAttemptService loginAttemptService;

    @Override
    public LoginResponseDTO login(AdminLoginDTO loginDTO) {
        String username = loginDTO.getUsername();
        String password = loginDTO.getPassword();
        String captcha = loginDTO.getCaptcha();

        // 检查账号是否被锁定
        if (loginAttemptService.isLocked(username)) {
            long remainingSeconds = loginAttemptService.getRemainingLockTime(username);
            throw new BusinessException(String.format(
                "账号已被锁定，请 %d 分钟后再试", 
                remainingSeconds / 60 + 1
            ));
        }

        if (captcha == null || captcha.isEmpty()) {
            throw new BusinessException("验证码不能为空");
        }

        String expectedCaptcha = captchaStorage.getCaptcha(username);
        if (expectedCaptcha == null || !expectedCaptcha.equalsIgnoreCase(captcha)) {
            // 验证码错误也算失败
            loginAttemptService.loginFailed(username);
            throw new BusinessException("验证码错误");
        }

        Optional<Admin> adminOptional = adminRepository.findByUsername(username);
        if (!adminOptional.isPresent()) {
            loginAttemptService.loginFailed(username);
            throw new BusinessException("账号或密码错误");
        }

        Admin admin = adminOptional.get();

        if (!passwordEncoder.matches(password, admin.getPassword())) {
            loginAttemptService.loginFailed(username);
            throw new BusinessException("账号或密码错误");
        }

        // 登录成功，清除失败记录
        loginAttemptService.loginSucceeded(username);
        
        String token = jwtTokenProvider.generateTokenByUsername(username);

        LoginResponseDTO response = new LoginResponseDTO();
        response.setUsername(admin.getUsername());
        response.setToken(token);
        response.setTokenType("Bearer");
        response.setExpiresIn(jwtTokenProvider.getExpirationTime() / 1000);

        return response;
    }

    @Override
    public Admin findByUsername(String username) {
        return adminRepository.findByUsername(username).orElse(null);
    }

    @Override
    public Admin createAdmin(String username, String password) {
        Optional<Admin> existingAdmin = adminRepository.findByUsername(username);
        if (existingAdmin.isPresent()) {
            throw new BusinessException("管理员已存在");
        }

        Admin admin = new Admin();
        admin.setUsername(username);
        admin.setPassword(passwordEncoder.encode(password));
        return adminRepository.save(admin);
    }

    @Override
    public void verifyCurrentPassword(String username, String currentPassword) {
        if (username == null || username.isBlank()) {
            throw new BusinessException("用户名不能为空");
        }
        if (currentPassword == null || currentPassword.isBlank()) {
            throw new BusinessException("当前密码不能为空");
        }

        Optional<Admin> adminOptional = adminRepository.findByUsername(username);
        if (!adminOptional.isPresent()) {
            throw new BusinessException("管理员不存在");
        }

        Admin admin = adminOptional.get();
        if (!passwordEncoder.matches(currentPassword, admin.getPassword())) {
            throw new BusinessException("当前密码验证失败");
        }
    }
}
