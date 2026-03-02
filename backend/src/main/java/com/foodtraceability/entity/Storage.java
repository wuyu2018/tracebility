package com.foodtraceability.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "storage")
public class Storage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "storage_time")
    private LocalDateTime storageTime;

    @Column(name = "outbound_time")
    private LocalDateTime outboundTime;

    @Column
    private Double quantity;

    @Column(length = 20)
    private String unit;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public LocalDateTime getStorageTime() { return storageTime; }
    public void setStorageTime(LocalDateTime storageTime) { this.storageTime = storageTime; }
    public LocalDateTime getOutboundTime() { return outboundTime; }
    public void setOutboundTime(LocalDateTime outboundTime) { this.outboundTime = outboundTime; }
    public Double getQuantity() { return quantity; }
    public void setQuantity(Double quantity) { this.quantity = quantity; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
}
