package com.foodtraceability.entity;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

class AdminTest {

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Test
    void testMatchPassword() {
        Admin admin = new Admin();
        admin.setPassword(passwordEncoder.encode("password123"));

        assertTrue(admin.matchPassword("password123", passwordEncoder));
        assertFalse(admin.matchPassword("wrongpassword", passwordEncoder));
    }

    @Test
    void testCreate() {
        Admin admin = Admin.create("admin", "encodedPassword", "admin@example.com");

        assertEquals("admin", admin.getUsername());
        assertEquals("encodedPassword", admin.getPassword());
        assertEquals("admin@example.com", admin.getEmail());
        assertFalse(admin.isLocked());
        assertEquals(0, admin.getLoginAttempts());
    }

    @Test
    void testMarkAsLocked() {
        Admin admin = new Admin();
        admin.setIsLocked(false);

        admin.markAsLocked();

        assertTrue(admin.isLocked());
    }

    @Test
    void testResetLoginAttempts() {
        Admin admin = new Admin();
        admin.setLoginAttempts(5);
        admin.setIsLocked(true);

        admin.resetLoginAttempts();

        assertEquals(0, admin.getLoginAttempts());
        assertFalse(admin.isLocked());
    }

    @Test
    void testIncrementLoginAttempts() {
        Admin admin = new Admin();
        admin.setLoginAttempts(0);
        admin.setIsLocked(false);

        admin.incrementLoginAttempts();
        assertEquals(1, admin.getLoginAttempts());
        assertFalse(admin.isLocked());

        admin.incrementLoginAttempts();
        admin.incrementLoginAttempts();
        admin.incrementLoginAttempts();
        admin.incrementLoginAttempts();
        assertEquals(5, admin.getLoginAttempts());
        assertTrue(admin.isLocked());
    }

    @Test
    void testUpdateLastLoginTime() {
        Admin admin = new Admin();

        admin.updateLastLoginTime();

        assertNotNull(admin.getLastLoginTime());
    }
}
