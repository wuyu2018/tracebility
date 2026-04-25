package com.foodtraceability.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "inspection")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Inspection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private ProductionBatch batch;

    @Column(name = "sample_name", length = 100)
    private String sampleName;

    @Column(name = "sample_quantity")
    private Integer sampleQuantity;

    @Column(name = "sample_specification", length = 100)
    private String sampleSpecification;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    public static Inspection create(ProductionBatch batch, String sampleName, Integer sampleQuantity, String sampleSpecification) {
        Inspection inspection = new Inspection();
        inspection.batch = batch;
        inspection.sampleName = sampleName;
        inspection.sampleQuantity = sampleQuantity;
        inspection.sampleSpecification = sampleSpecification;
        return inspection;
    }
}