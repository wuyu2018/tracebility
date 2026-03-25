package com.foodtraceability.dto;

import lombok.Data;

@Data
public class SecurityCodeRequest {
    private Long batchId;
    private Integer quantity;
}