package com.foodtraceability.entity;

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
}
