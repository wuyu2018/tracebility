package com.foodtraceability.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "material_purchase")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class MaterialPurchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Product product;

    @Column(name = "material_name", nullable = false, length = 100)
    private String materialName;

    @Column(name = "batch_number", nullable = false, unique = true, length = 50)
    private String batchNumber;

    @Column(name = "supplier_name", length = 100)
    private String supplierName;

    @Column(name = "producer_name", length = 100)
    private String producerName;

    @Column(name = "producer_address", length = 255)
    private String producerAddress;

    @Column(name = "purchase_date")
    private LocalDateTime purchaseDate;

    @Column(name = "quantity")
    private Double quantity;

    @Column(length = 20)
    private String unit;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;
}