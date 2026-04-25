package com.foodtraceability.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.foodtraceability.domain.DomainEvent;
import com.foodtraceability.domain.valueobject.*;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "production_batch", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"product_id", "batch_number"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ProductionBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "batch_number", nullable = false, length = 50)
    private String batchNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Product product;

    @Column(name = "production_date", nullable = false)
    private LocalDate productionDate;

    @Column(name = "shelf_life", length = 50)
    private String shelfLife;

    @Column(name = "quantity")
    private Double quantity;

    @Column(length = 20)
    private String unit;

    @Column(name = "storage_id")
    private Long storageId;

    @Column(name = "transport_sale_id")
    private Long transportSaleId;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Transient
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public boolean isDeleted() {
        return Boolean.TRUE.equals(this.isDeleted);
    }

    public void softDelete() {
        this.isDeleted = true;
    }

    public void associateStorage(Storage storage) {
        this.storageId = storage.getId();
    }

    public void associateTransportSale(TransportSale transportSale) {
        this.transportSaleId = transportSale.getId();
    }

    public boolean hasSecurityCodes() {
        return false;
    }

    public boolean canBeDeleted() {
        return !this.isDeleted;
    }

    public TraceInfo buildTraceInfo(
            SecurityCode securityCode,
            List<MaterialPurchase> materials,
            Inspection inspection,
            Storage storage,
            TransportSale transportSale,
            boolean forAdmin) {
        return TraceInfo.create(
            this.product,
            this,
            materials,
            inspection,
            storage,
            transportSale,
            forAdmin
        );
    }

    public static ProductionBatch create(
            Product product,
            String batchNumber,
            LocalDate productionDate,
            String shelfLife) {
        ProductionBatch batch = new ProductionBatch();
        batch.product = product;
        batch.batchNumber = batchNumber;
        batch.productionDate = productionDate;
        batch.shelfLife = shelfLife;
        batch.isDeleted = false;
        return batch;
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
