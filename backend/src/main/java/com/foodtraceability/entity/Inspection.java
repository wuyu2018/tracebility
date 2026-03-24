package com.foodtraceability.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "inspection")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Inspection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "anti_fake_code", nullable = false, length = 20)
    private String antiFakeCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "anti_fake_code", referencedColumnName = "anti_fake_code", insertable = false, updatable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Product product;

    @Column(name = "batch_number", length = 50)
    private String batchNumber;

    @Column(name = "sample_name", length = 100)
    private String sampleName;

    @Column(name = "sample_quantity")
    private Integer sampleQuantity;

    @Column(name = "sample_specification", length = 100)
    private String sampleSpecification;
}
