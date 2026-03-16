package com.foodtraceability.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "transport_sale")
@Data
@NoArgsConstructor
@AllArgsConstructor
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
}
