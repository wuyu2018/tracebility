package com.foodtraceability.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

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
}