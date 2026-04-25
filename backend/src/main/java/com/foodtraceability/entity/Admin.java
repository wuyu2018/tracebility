package com.foodtraceability.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@Entity
@Table(name = "admin")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", length = 50, nullable = false, unique = true)
    private String username;

    @Column(name = "password", length = 100, nullable = false)
    private String password;

    @Column(length = 100)
    private String email;

    @Column(name = "is_locked")
    private Boolean isLocked = false;

    @Column(name = "login_attempts")
    private Integer loginAttempts = 0;

    @Column(name = "last_login_time")
    private LocalDateTime lastLoginTime;

    public boolean matchPassword(String rawPassword, PasswordEncoder encoder) {
        return encoder.matches(rawPassword, this.password);
    }

    public static Admin create(String username, String encodedPassword, String email) {
        Admin admin = new Admin();
        admin.setUsername(username);
        admin.setPassword(encodedPassword);
        admin.setEmail(email);
        admin.setIsLocked(false);
        admin.setLoginAttempts(0);
        return admin;
    }

    public void markAsLocked() {
        this.isLocked = true;
    }

    public void resetLoginAttempts() {
        this.loginAttempts = 0;
        this.isLocked = false;
    }

    public void incrementLoginAttempts() {
        this.loginAttempts++;
        if (this.loginAttempts >= 5) {
            this.isLocked = true;
        }
    }

    public boolean isLocked() {
        return Boolean.TRUE.equals(this.isLocked);
    }

    public void updateLastLoginTime() {
        this.lastLoginTime = LocalDateTime.now();
    }
}
