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
    @JoinColumn(name = "material_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Material material;

    @Column(name = "batch_number", length = 50)
    private String batchNumber;

    @Column(name = "supplier_name", length = 100)
    private String supplierName;

    @Column(name = "producer_name", length = 100)
    private String producerName;

    @Column(name = "producer_address", length = 255)
    private String producerAddress;

    @Column(name = "purchase_date")
    private LocalDateTime purchaseDate;

    @Column
    private Double quantity;

    @Column(length = 20)
    private String unit;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    public void softDelete() {
        this.isDeleted = true;
    }

    public boolean isDeleted() {
        return Boolean.TRUE.equals(this.isDeleted);
    }
}
