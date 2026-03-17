package com.foodtraceability.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "inspection")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Inspection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_name", nullable = false, length = 100)
    private String productName;

    @Column(name = "batch_number", length = 50)
    private String batchNumber;

    @Column(name = "sample_name", length = 100)
    private String sampleName;

    @Column(name = "sample_quantity")
    private Integer sampleQuantity;

    @Column(name = "sample_specification", length = 100)
    private String sampleSpecification;
}
