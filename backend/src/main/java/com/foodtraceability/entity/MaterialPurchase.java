package com.foodtraceability.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "material_purchase")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaterialPurchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_name", nullable = false, length = 100)
    private String productName;

    @Column(name = "batch_number", length = 50)
    private String batchNumber;

    @Column(name = "material_name", nullable = false, length = 100)
    private String materialName;

    @Column(name = "producer_name", length = 100)
    private String producerName;

    @Column(name = "producer_address", length = 255)
    private String producerAddress;
}
