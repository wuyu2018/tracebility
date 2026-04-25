package com.foodtraceability.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.foodtraceability.domain.DomainEvent;
import com.foodtraceability.domain.valueobject.InspectionInfo;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "inspection")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Inspection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private ProductionBatch batch;

    @Column(name = "sample_name", length = 100)
    private String sampleName;

    @Column(name = "sample_quantity")
    private Integer sampleQuantity;

    @Column(name = "sample_specification", length = 100)
    private String sampleSpecification;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Transient
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    public static Inspection create(ProductionBatch batch, String sampleName, Integer sampleQuantity, String sampleSpecification, String imageUrl) {
        Inspection inspection = new Inspection();
        inspection.batch = batch;
        inspection.sampleName = sampleName;
        inspection.sampleQuantity = sampleQuantity;
        inspection.sampleSpecification = sampleSpecification;
        inspection.imageUrl = imageUrl;
        return inspection;
    }

    public InspectionInfo toInspectionInfo() {
        return InspectionInfo.from(this);
    }

    public boolean isValid() {
        return this.batch != null && this.sampleName != null && !this.sampleName.isBlank();
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
