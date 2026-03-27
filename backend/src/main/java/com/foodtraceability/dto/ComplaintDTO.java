package com.foodtraceability.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComplaintDTO {
    private Long id;

    @NotBlank(message = "产品名称不能为空")
    private String productName;

    @NotBlank(message = "投诉原因不能为空")
    private String complaintReason;

    private LocalDateTime complaintTime;
}