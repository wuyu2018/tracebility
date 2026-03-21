package com.foodtraceability.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseInfoDTO {

    private Long productId;
    private String name;
    private String specification;
    private String batchNumber;
    private String antiFakeCode;
    private String qrCodeUrl;
    private String imageUrl;
    private String contactPhone;
    private String contactEmail;
    private LocalDateTime lastQueriedTime;
}
