package com.foodtraceability.service;

import com.foodtraceability.dto.AdminLoginDTO;
import com.foodtraceability.dto.LoginResponseDTO;
import com.foodtraceability.entity.Admin;

public interface AdminService {

    LoginResponseDTO login(AdminLoginDTO loginDTO);

    Admin findByUsername(String username);
}