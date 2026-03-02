package com.foodtraceability.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "inspection")
public class Inspection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "sample_name", length = 100)
    private String sampleName;

    @Column(name = "sample_quantity")
    private Integer sampleQuantity;

    @Column(name = "sample_specification", length = 100)
    private String sampleSpecification;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getSampleName() { return sampleName; }
    public void setSampleName(String sampleName) { this.sampleName = sampleName; }
    public Integer getSampleQuantity() { return sampleQuantity; }
    public void setSampleQuantity(Integer sampleQuantity) { this.sampleQuantity = sampleQuantity; }
    public String getSampleSpecification() { return sampleSpecification; }
    public void setSampleSpecification(String sampleSpecification) { this.sampleSpecification = sampleSpecification; }
}
