package com.foodtraceability.util;

import com.foodtraceability.entity.Admin;
import com.foodtraceability.repository.AdminRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {

    private final AdminRepository adminRepository;

    public SecurityUtils(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    public Long getCurrentCompanyId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        if (isSuperAdmin(auth)) {
            return null;
        }
        String username = auth.getName();
        if (username == null || "anonymousUser".equals(username)) {
            return null;
        }
        Admin admin = adminRepository.findByUsername(username).orElse(null);
        if (admin != null && admin.getCompanyId() != null) {
            return admin.getCompanyId();
        }
        return null;
    }

    public boolean isSuperAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return isSuperAdmin(auth);
    }

    private boolean isSuperAdmin(Authentication auth) {
        if (auth == null) return false;
        for (GrantedAuthority authority : auth.getAuthorities()) {
            if ("ROLE_SUPER_ADMIN".equals(authority.getAuthority())) {
                return true;
            }
        }
        return false;
    }
}
