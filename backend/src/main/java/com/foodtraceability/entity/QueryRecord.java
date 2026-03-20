package com.foodtraceability.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "query_record")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QueryRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "anti_fake_code", nullable = false, length = 20)
    private String antiFakeCode;

    @Column(name = "batch_number", length = 50)
    private String batchNumber;

    @Column(name = "query_time", nullable = false)
    private LocalDateTime queryTime;

    @Column(name = "query_type", length = 20)
    private String queryType;

    @Column(name = "is_queried_before", nullable = false)
    private Boolean isQueriedBefore = false;

    @Column(name = "is_backed_up", nullable = false)
    private Boolean isBackedUp = false;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;
}
