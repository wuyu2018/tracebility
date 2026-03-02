package com.foodtraceability.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "transport_sale")
public class TransportSale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "environment_temperature")
    private Double environmentTemperature;

    @Column(name = "product_temperature")
    private Double productTemperature;

    @Column
    private LocalDateTime time;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public Double getEnvironmentTemperature() { return environmentTemperature; }
    public void setEnvironmentTemperature(Double environmentTemperature) { this.environmentTemperature = environmentTemperature; }
    public Double getProductTemperature() { return productTemperature; }
    public void setProductTemperature(Double productTemperature) { this.productTemperature = productTemperature; }
    public LocalDateTime getTime() { return time; }
    public void setTime(LocalDateTime time) { this.time = time; }
}
