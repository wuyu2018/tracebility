package com.foodtraceability.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "product")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "anti_fake_code", unique = true, nullable = false, length = 20)
    private String antiFakeCode;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 50)
    private String specification;

    @Column(name = "batch_number", length = 50)
    private String batchNumber;

    @Column(name = "production_date")
    private LocalDate productionDate;

    @Column(name = "shelf_life", length = 50)
    private String shelfLife;

    @Column(name = "image_url", length = 255)
    private String imageUrl;

    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    @Column(name = "contact_email", length = 100)
    private String contactEmail;

    @Column(name = "qr_code_url", length = 5000)
    private String qrCodeUrl;

    @Column(name = "last_queried_time")
    private LocalDateTime lastQueriedTime;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;
}


