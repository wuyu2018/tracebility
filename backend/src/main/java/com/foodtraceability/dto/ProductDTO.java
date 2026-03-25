package com.foodtraceability.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ProductDTO {
    private Long id;
    private String name;
    private String specification;
    private String shelfLife;
    private String imageUrl;
    private String contactPhone;
    private String contactEmail;
}