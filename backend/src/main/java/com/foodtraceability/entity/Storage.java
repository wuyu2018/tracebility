package com.foodtraceability.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.foodtraceability.domain.DomainEvent;
import com.foodtraceability.domain.valueobject.StorageInfo;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "storage")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Storage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private ProductionBatch batch;

    @Column(name = "storage_time")
    private LocalDateTime storageTime;

    @Column(name = "outbound_time")
    private LocalDateTime outboundTime;

    @Column
    private Double quantity;

    @Column(length = 20)
    private String unit;

    @Column(name = "warehouse_location", length = 100)
    private String warehouseLocation;

    @Transient
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    public void associateBatch(ProductionBatch batch) {
        this.batch = batch;
    }

    public static Storage create(ProductionBatch batch, LocalDateTime storageTime, String warehouseLocation, Double quantity, String unit) {
        Storage storage = new Storage();
        storage.batch = batch;
        storage.storageTime = storageTime;
        storage.warehouseLocation = warehouseLocation;
        storage.quantity = quantity;
        storage.unit = unit;
        return storage;
    }

    public StorageInfo toStorageInfo() {
        return StorageInfo.from(this);
    }

    public boolean isValid() {
        return this.batch != null;
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
