package com.foodtraceability.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "material_purchase")
public class MaterialPurchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "material_name", nullable = false, length = 100)
    private String materialName;

    @Column(name = "producer_name", length = 100)
    private String producerName;

    @Column(name = "producer_address", length = 255)
    private String producerAddress;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getMaterialName() { return materialName; }
    public void setMaterialName(String materialName) { this.materialName = materialName; }
    public String getProducerName() { return producerName; }
    public void setProducerName(String producerName) { this.producerName = producerName; }
    public String getProducerAddress() { return producerAddress; }
    public void setProducerAddress(String producerAddress) { this.producerAddress = producerAddress; }
}
