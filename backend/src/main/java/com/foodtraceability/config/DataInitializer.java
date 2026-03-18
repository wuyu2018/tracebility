package com.foodtraceability.config;

import com.foodtraceability.entity.Admin;
import com.foodtraceability.repository.AdminRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initAdmin(AdminRepository adminRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (adminRepository.findByUsername("admin").isEmpty()) {
                Admin admin = new Admin();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin123"));
                adminRepository.save(admin);
                System.out.println("Admin user created with password: admin123");
            }
        };
    }
}
