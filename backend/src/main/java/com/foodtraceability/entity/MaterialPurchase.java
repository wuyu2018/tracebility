package com.foodtraceability.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.foodtraceability.domain.DomainEvent;
import com.foodtraceability.domain.valueobject.MaterialInfo;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "material_purchase", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"product_id", "batch_number"})
})
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

    @Column(name = "batch_number", nullable = false, length = 50)
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

    @Transient
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    public static MaterialPurchase create(Product product, String materialName, String batchNumber) {
        MaterialPurchase material = new MaterialPurchase();
        material.product = product;
        material.materialName = materialName;
        material.batchNumber = batchNumber;
        material.isDeleted = false;
        return material;
    }

    public void softDelete() {
        this.isDeleted = true;
    }

    public boolean isDeleted() {
        return Boolean.TRUE.equals(this.isDeleted);
    }

    public boolean canBeDeleted() {
        return !this.isDeleted;
    }

    public MaterialInfo toMaterialInfo() {
        return MaterialInfo.from(this);
    }

    public void updateSupplier(String supplierName, String producerName, String producerAddress) {
        this.supplierName = supplierName;
        this.producerName = producerName;
        this.producerAddress = producerAddress;
    }

    public void updateQuantity(Double quantity, String unit) {
        this.quantity = quantity;
        this.unit = unit;
    }

    public void updateBasicInfo(String materialName, String batchNumber,
                                String supplierName, String producerName, String producerAddress,
                                LocalDateTime purchaseDate, Double quantity, String unit) {
        if (materialName != null) this.materialName = materialName;
        if (batchNumber != null) this.batchNumber = batchNumber;
        if (supplierName != null) this.supplierName = supplierName;
        if (producerName != null) this.producerName = producerName;
        if (producerAddress != null) this.producerAddress = producerAddress;
        if (purchaseDate != null) this.purchaseDate = purchaseDate;
        if (quantity != null) this.quantity = quantity;
        if (unit != null) this.unit = unit;
    }

    public List<DomainEvent> getDomainEvents() {
        return new ArrayList<>(domainEvents);
    }

    public void clearDomainEvents() {
        this.domainEvents.clear();
    }

    protected void addDomainEvent(DomainEvent event) {
        this.domainEvents.add(event);
    }
}
