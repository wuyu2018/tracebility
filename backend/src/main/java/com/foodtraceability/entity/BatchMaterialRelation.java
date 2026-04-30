package com.foodtraceability.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.foodtraceability.domain.DomainEvent;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "batch_material_relation")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class BatchMaterialRelation {

    @EmbeddedId
    private BatchMaterialRelationId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("batchId")
    @JoinColumn(name = "batch_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private ProductionBatch batch;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("materialPurchaseId")
    @JoinColumn(name = "material_purchase_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private MaterialPurchase materialPurchase;

    @Transient
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    public static BatchMaterialRelation create(ProductionBatch batch, MaterialPurchase materialPurchase) {
        BatchMaterialRelation relation = new BatchMaterialRelation();
        relation.id = new BatchMaterialRelationId(batch.getId(), materialPurchase.getId());
        relation.batch = batch;
        relation.materialPurchase = materialPurchase;
        return relation;
    }

    public boolean isValid() {
        return this.batch != null && this.materialPurchase != null;
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
